/**
 * This file is part of
 * 
 * CRAFTY - Competition for Resources between Agent Functional TYpes
 *
 * Copyright (C) 2014 School of GeoScience, University of Edinburgh, Edinburgh, UK
 * 
 * CRAFTY is free software: You can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software 
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *  
 * CRAFTY is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * School of Geoscience, University of Edinburgh, Edinburgh, UK
 */
package org.volante.abm.schedule;


import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.volante.abm.agent.Agent;
import org.volante.abm.data.Cell;
import org.volante.abm.data.ModelData;
import org.volante.abm.data.Region;
import org.volante.abm.data.RegionSet;
import org.volante.abm.output.Outputs;
import org.volante.abm.schedule.ScheduleStatusEvent.ScheduleStage;


public class DefaultSchedule implements Schedule {

	static int						idCounter		= 0;

	protected int					id				= idCounter++;

	Logger							log				= Logger.getLogger(this.getClass());
	RegionSet						regions			= null;
	int								tick			= 0;
	int								targetTick		= 0;
	int								startTick		= 0;
	int								endTick			= Integer.MAX_VALUE;

	List<PrePreTickAction>			prePreTickActions	= new ArrayList<PrePreTickAction>();
	List<PreTickAction>				preTickActions	= new ArrayList<PreTickAction>();
	List<PostTickAction>			postTickActions	= new ArrayList<PostTickAction>();

	Outputs							output			= new Outputs();
	private RunInfo					info			= null;

	List<ScheduleStatusListener>	listeners		= new ArrayList<ScheduleStatusListener>();

	/*
	 * Constructors
	 */
	public DefaultSchedule() {
	}

	public DefaultSchedule(RegionSet regions) {
		this();
		this.regions = regions;
	}

	@Override
	public void initialise(ModelData data, RunInfo info, Region extent) throws Exception {
		this.info = info;
		output = info.getOutputs();
		info.setSchedule(this);
	}

	@Override
	public void tick() {
		log.info(this + ">\n********************\nStart of tick " + tick + "\n********************");
		fireScheduleStatus(new ScheduleStatusEvent(tick, ScheduleStage.PRE_TICK, true));
		info.getPersister().setContext("y", tick + "");

		prePreTickUpdates();

		// Reset the effective capital levels
		for (Cell c : regions.getAllCells()) {
			c.initEffectiveCapitals();
		}

		preTickUpdates();

		fireScheduleStatus(new ScheduleStatusEvent(tick,
				ScheduleStage.MAIN_LOOP, true));

		// Allow institutions to update capitals
		for (Region r : regions.getAllRegions()) {
			if (r.hasInstitutions()) {
				r.getInstitutions().updateCapitals();
			}
		}

		for (Agent a : regions.getAllAgents()) {
			a.tickStartUpdate();
		}
		
		if (this.getCurrentTick() > this.getStartTick()) {
			log.info("Update agents' competitiveness and consider giving up ...");
			for (Agent a : regions.getAllAgents()) {
				a.updateCompetitiveness();
				a.considerGivingUp();
			}

			// Remove any unneeded agents
			for (Region r : regions.getAllRegions()) {
				r.cleanupAgents();
			}

			// Allocate land
			log.info("Allocate unmanged cells ...");
			for (Region r : regions.getAllRegions()) {
				r.getAllocationModel().allocateLand(r);
			}
		}

		// Calculate supply
		log.info("Update agents' supply...");
		for (Agent a : regions.getAllAgents()) {
			a.updateSupply();
		}

		// Allow the demand model to update for global supply supply for each region
		for (Region r : regions.getAllRegions()) {
			r.getDemandModel().updateSupply();
		}

		for (Agent a : regions.getAllAgents()) {
			a.updateCompetitiveness();
			a.tickEndUpdate();
		}

		fireScheduleStatus(new ScheduleStatusEvent(tick, ScheduleStage.POST_TICK, true));
		postTickUpdates();

		output();
		log.info("\n********************\nEnd of tick " + tick + "\n********************");
		fireScheduleStatus(new ScheduleStatusEvent(tick, ScheduleStage.PAUSED, false));
		tick++;
	}

	@Override
	public void finish() {
		output.finished();
		fireScheduleStatus(new ScheduleStatusEvent(tick, ScheduleStage.FINISHING, true));
	}

	/*
	 * Run controls
	 */

	/**
	 * @see org.volante.abm.schedule.Schedule#runFromTo(int, int)
	 */
	@Override
	public void runFromTo(int start, int end) {
		log.info("Starting run for set number of ticks");
		log.info("Start: " + start + ", End: " + end);

		setStartTick(start);
		setEndTick(end);
		run();
		finish();
	}

	/**
	 * @see org.volante.abm.schedule.Schedule#runUntil(int)
	 */
	@Override
	public void runUntil(int target) {
		setTargetTick(target);
		while (tick <= targetTick) {
			tick();
		}
	}

	/**
	 * @see org.volante.abm.schedule.Schedule#run()
	 */
	@Override
	public void run() {
		while (tick <= endTick) {
			tick();
		}
	}

	/**
	 * @see org.volante.abm.schedule.Schedule#setTargetTick(int)
	 */
	@Override
	public void setTargetTick(int target) {
		this.targetTick = target;
	}

	/**
	 * @see org.volante.abm.schedule.Schedule#setEndTick(int)
	 */
	@Override
	public void setEndTick(int end) {
		this.endTick = end;
	}

	@Override
	public int getEndTick() {
		return endTick;
	}

	@Override
	public int getTargetTick() {
		return targetTick;
	}

	@Override
	public int getStartTick() {
		return startTick;
	}

	@Override
	public void setTargetToNextTick() {
		setTargetTick(tick);
	}

	/*
	 * Pre and post tick events and registering
	 */

	private void prePreTickUpdates() {
		log.info("Pre PreTick\t\t (DefaultSchedule ID " + id + ")");

		// copy to prevent concurrent modifications:
		List<PrePreTickAction> prePreTickActionsCopy = new ArrayList<PrePreTickAction>(
				prePreTickActions);

		for (PrePreTickAction p : prePreTickActionsCopy) {
			// <- LOGGING
			if (log.isDebugEnabled()) {
				log.debug("Do PrePreTick action " + p);
			}
			// LOGGING ->

			p.prePreTick();
		}
	}

	private void preTickUpdates() {
		log.info("Pre Tick\t\t (DefaultSchedule ID " + id + ")");

		// copy to prevent concurrent modifications:
		List<PreTickAction> preTickActionsCopy = new ArrayList<PreTickAction>(
				preTickActions);

		for (PreTickAction p : preTickActionsCopy) {
			// <- LOGGING
			if (log.isDebugEnabled()) {
				log.debug("Do PreTick action " + p);
			}
			// LOGGING ->

			p.preTick();
		}
	}

	private void postTickUpdates() {
		log.info("Post Tick\t\t (DefaultSchedule ID " + id + ")");

		// copy to prevent concurrent modifications:
		List<PostTickAction> postTickActionsCopy = new ArrayList<PostTickAction>(
				postTickActions);

		for (PostTickAction p : postTickActionsCopy) {
			p.postTick();
		}
	}

	@Override
	public void register(TickAction o) {
		if (o instanceof PrePreTickAction && !prePreTickActions.contains(o)) {
			prePreTickActions.add((PrePreTickAction) o);
		}
		if (o instanceof PreTickAction && !preTickActions.contains(o)) {
			preTickActions.add((PreTickAction) o);
		}
		if (o instanceof PostTickAction && !postTickActions.contains(o)) {
			postTickActions.add((PostTickAction) o);
		}
	}

	/**
	 * @see org.volante.abm.schedule.Schedule#unregister(org.volante.abm.schedule.TickAction)
	 */
	@Override
	public boolean unregister(TickAction o) {
		if (o instanceof PreTickAction) {
			return preTickActions.remove(o);
		}
		if (o instanceof PostTickAction) {
			return postTickActions.remove(o);
		}
		log.warn("The specified object is not a PreTickAction or PostTickAction!");
		return false;
	}

	private void output() {
		output.doOutput(regions);
	}

	/*
	 * Getters and setters
	 */

	/**
	 * @see org.volante.abm.schedule.Schedule#setStartTick(int)
	 */
	@Override
	public void setStartTick(int tick) {
		this.startTick = tick;
		this.tick = tick;
	}

	@Override
	public int getCurrentTick() {
		return tick;
	}

	@Override
	public void setRegions(RegionSet regions) {
		this.regions = regions;
	}

	void fireScheduleStatus(ScheduleStatusEvent e) {
		for (ScheduleStatusListener l : listeners) {
			l.scheduleStatus(e);
		}
	}

	@Override
	public void addStatusListener(ScheduleStatusListener l) {
		listeners.add(l);
	}
}
