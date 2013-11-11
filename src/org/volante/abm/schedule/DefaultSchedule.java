package org.volante.abm.schedule;

import java.util.*;

import org.apache.log4j.Logger;
import org.volante.abm.agent.Agent;
import org.volante.abm.data.*;
import org.volante.abm.output.Outputs;
import org.volante.abm.schedule.ScheduleStatusEvent.ScheduleStage;

public class DefaultSchedule implements Schedule
{
	Logger log = Logger.getLogger( this.getClass() );
	RegionSet regions;
	int tick = 0;
	int targetTick;
	int endTick;
	
	List<PreTickAction> preTickActions = new ArrayList<PreTickAction>();
	List<PostTickAction> postTickActions = new ArrayList<PostTickAction>();
	
	Outputs output = new Outputs();
	private RunInfo info;
	
	List<ScheduleStatusListener> listeners = new ArrayList<ScheduleStatusListener>();
	
	/*
	 * Constructors
	 */
	public DefaultSchedule() 
	{
	}
	public DefaultSchedule( RegionSet regions )
	{
		this();
		this.regions = regions;
	}
	
	public void initialise( ModelData data, RunInfo info, Region extent ) throws Exception
	{
		this.info = info;
		output = info.outputs;
		info.setSchedule( this );
	}
	
	

	public void tick()
	{
		log.info("\n********************\nStart of tick " + tick + "\n********************");
		fireScheduleStatus( new ScheduleStatusEvent( tick, ScheduleStage.PRE_TICK, true ) );
		info.getPersister().setContext( "y", tick+"" );
		preTickUpdates();
		
		fireScheduleStatus( new ScheduleStatusEvent( tick, ScheduleStage.MAIN_LOOP, true ) );

		// Reset the effective capital levels
		for( Cell c : regions.getAllCells() ) c.initEffectiveCapitals();
		// Allow institutions to update capitals
		for( Region r : regions.getAllRegions() )
			if( r.hasInstitutions() ) r.getInstitutions().updateCapitals();

		//Recalculate agent competitiveness and give up
		for ( Agent a : regions.getAllAgents() )
		{
			a.tickStartUpdate();
			a.updateCompetitiveness();
			a.considerGivingUp();
		}

		//Remove any unneeded agents
		for ( Region r : regions.getAllRegions() )
			r.cleanupAgents();

		//Allocate land
		for ( Region r : regions.getAllRegions() )
			r.getAllocationModel().allocateLand( r );

		//Calculate supply
		for ( Agent a : regions.getAllAgents() ) {
			a.updateSupply();
		}

		//Allow the demand model to update for global supply supply for each region
		for ( Region r : regions.getAllRegions() )
			r.getDemandModel().updateSupply();

		//Calculate supply
		for ( Agent a : regions.getAllAgents() )
		{
			a.updateCompetitiveness();
			a.tickEndUpdate();
		}

		fireScheduleStatus( new ScheduleStatusEvent( tick, ScheduleStage.POST_TICK, true ) );
		postTickUpdates();
		
		for ( Agent a : regions.getAllAgents() )
		{
			a.updateCompetitiveness();
			a.tickEndUpdate();
		}
		
		output();
		log.info("\n********************\nEnd of tick " + tick + "\n********************");
		fireScheduleStatus( new ScheduleStatusEvent( tick, ScheduleStage.PAUSED, false ) );
		tick++;
	}
	
	public void finish()
	{
		output.finished();
	}
	
	/*
	 * Run controls
	 */
	
	public void runFromTo( int start, int end )
	{
		log.info("Starting run for set number of ticks");
		log.info( "Start: " + start + ", End: " + end );
		setStartTick( start );
		runUntil( end );
		finish();
	}
	public void runUntil( int target )
	{
		setTargetTick( target );
		while( tick <= targetTick ) tick();
	}
	
	public void setEndTick( int end ) { this.endTick = end; }
	public int getEndTick() { return endTick; }
	public void setTargetTick( int end ) { this.targetTick = end; }
	public int getTargetTick() { return targetTick; }
	public void incrementTick() 
	{ 
		setTargetTick( tick );
	}
	/*
	 * Pre and post tick events and registering
	 */

	private void preTickUpdates()
	{
		log.info("Pre Tick\t\t" + hashCode());
		for( PreTickAction p : preTickActions) p.preTick();
	}

	private void postTickUpdates()
	{
		log.info("Post Tick\t\t" + hashCode());
		for( PostTickAction p : postTickActions) p.postTick();
	}
	
	public void register( Object o )
	{
		if( o instanceof PreTickAction && ! preTickActions.contains( o ))
				preTickActions.add((PreTickAction) o);
		if( o instanceof PostTickAction && ! postTickActions.contains( o ))
				postTickActions.add((PostTickAction) o);
	}

	private void output()
	{
		output.doOutput( regions );
	}
	
	/*
	 * Getters and setters
	 */
	
	public void setStartTick( int tick ) { this.tick = tick; }
	public int getCurrentTick() { return tick; }
	public void setRegions( RegionSet regions ) { this.regions = regions; }

	void fireScheduleStatus( ScheduleStatusEvent e ) { for( ScheduleStatusListener l : listeners ) l.scheduleStatus( e ); }
	public void addStatusListener( ScheduleStatusListener l ) { listeners.add( l ); }
}
