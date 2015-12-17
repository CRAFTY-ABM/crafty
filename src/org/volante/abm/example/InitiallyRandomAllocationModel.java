/**
 * This file is part of
 * 
 * CRAFTY - Competition for Resources between Agent Functional TYpes
 *
 * Copyright (C) 2015 School of GeoScience, University of Edinburgh, Edinburgh, UK
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
 * 
 * Created by Sascha Holzhauer on 20 Nov 2015
 */
package org.volante.abm.example;


import static com.moseph.modelutils.Utilities.sample;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementMap;
import org.volante.abm.agent.Agent;
import org.volante.abm.agent.GeoAgent;
import org.volante.abm.agent.PotentialAgent;
import org.volante.abm.agent.SocialAgent;
import org.volante.abm.data.Cell;
import org.volante.abm.data.ModelData;
import org.volante.abm.data.Region;
import org.volante.abm.models.AllocationModel;
import org.volante.abm.models.utils.GivingInStatisticsMessenger;
import org.volante.abm.models.utils.TakeoverMessenger;
import org.volante.abm.models.utils.TakeoverObserver;
import org.volante.abm.output.GivingInStatisticsObserver;
import org.volante.abm.param.RandomPa;
import org.volante.abm.schedule.RunInfo;

/**
 * @author Sascha Holzhauer
 *
 */
public class InitiallyRandomAllocationModel implements AllocationModel, TakeoverMessenger, GivingInStatisticsMessenger {

	/**
	 * Logger
	 */
	static private Logger logger = Logger.getLogger(InitiallyRandomAllocationModel.class);

	@Element(required = true)
	protected AllocationModel regularAllocModel = null;

	
	@ElementMap(entry = "proportion", key = "aft", attribute = true, inline = true, required = false)
	protected Map<String, Double> aftProportions = new HashMap<>();
	
	Map<PotentialAgent, Double> aftScores = new HashMap<>();
	
	protected boolean networkNullErrorOccurred = false;

	/**
	 * @see org.volante.abm.serialization.Initialisable#initialise(org.volante.abm.data.ModelData,
	 *      org.volante.abm.schedule.RunInfo, org.volante.abm.data.Region)
	 */
	@Override
	public void initialise(ModelData data, RunInfo info, Region extent) throws Exception {
		this.regularAllocModel.initialise(data, info, extent);
		
		for (PotentialAgent pa : extent.getPotentialAgents()) {
			if (!aftProportions.containsKey(pa.getID())) {
				logger.warn("No proportion element for " + pa.getID()
						+ " given! Using default (1/<number of AFTs>).");
				aftScores.put(pa, 1.0 / extent.getPotentialAgents().size());
			} else {
				aftScores.put(pa, aftProportions.get(pa.getID()));
			}
		}
		if (aftProportions.containsKey(Agent.NOT_MANAGED_ID)) {
			aftScores.put(Agent.NOT_MANAGED.getType(), aftProportions.get(Agent.NOT_MANAGED_ID));
		}
	}

	/**
	 * @see org.volante.abm.models.AllocationModel#allocateLand(org.volante.abm.data.Region)
	 */
	@Override
	public void allocateLand(Region r) {
		if (r.getRinfo().getSchedule().getCurrentTick() == r.getRinfo().getSchedule().getStartTick() + 1) {
			this.allocateLandRandomly(r);
		} else {
			this.regularAllocModel.allocateLand(r);
		}
	}

	/**
	 * @param r
	 */
	protected void allocateLandRandomly(Region r) {
		for (Cell c : r.getAllCells()) {

			PotentialAgent pa =
					sample(aftScores, false, r.getRandom().getURService(), RandomPa.RANDOM_SEED_RUN_ALLOCATION.name());

			if (pa != null) {
				Agent agent = pa.createAgent(r);

				r.setInitialOwnership(agent, c);

				if (r.getNetworkService() != null && agent != Agent.NOT_MANAGED) {
					// <- LOGGING
					if (logger.isDebugEnabled()) {
						logger.debug("Linking agent " + agent);
					}
					// LOGGING ->

					if (r.getNetwork() != null) {
						if (r.getGeography() != null && agent instanceof GeoAgent) {
							((GeoAgent) agent).addToGeography();
						}
						r.getNetworkService().addAndLinkNode(r.getNetwork(), (SocialAgent) agent);
					} else {
						if (!networkNullErrorOccurred) {
							logger.warn("Network object not present during creation of new agent (subsequent error messages are suppressed)");
							networkNullErrorOccurred = true;
						}
					}
				}
			}
		}
	}

	/**
	 * @see org.volante.abm.models.AllocationModel#getDisplay()
	 */
	@Override
	public AllocationDisplay getDisplay() {
		return regularAllocModel.getDisplay();
	}

	/**
	 * @see org.volante.abm.models.utils.GivingInStatisticsMessenger#registerGivingInStatisticOberserver(org.volante.abm.output.GivingInStatisticsObserver)
	 */
	@Override
	public void registerGivingInStatisticOberserver(GivingInStatisticsObserver observer) {
		if (this.regularAllocModel instanceof GivingInStatisticsMessenger) {
			((GivingInStatisticsMessenger) this.regularAllocModel).registerGivingInStatisticOberserver(observer);
		}
	}

	/**
	 * @see org.volante.abm.models.utils.TakeoverMessenger#registerTakeoverOberserver(org.volante.abm.models.utils.TakeoverObserver)
	 */
	@Override
	public void registerTakeoverOberserver(TakeoverObserver observer) {
		if (this.regularAllocModel instanceof TakeoverMessenger) {
			((TakeoverMessenger) this.regularAllocModel).registerTakeoverOberserver(observer);
		}

	}
}
