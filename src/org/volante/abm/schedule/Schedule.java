package org.volante.abm.schedule;

import org.volante.abm.data.RegionSet;
import org.volante.abm.serialization.Initialisable;

public interface Schedule extends Initialisable
{
	public void tick();
	public void incrementTick();
	public int getCurrentTick();
	
	/**
	 * Registers the object in case any actions are going to be called on it
	 * e.g. before/after ticks.
	 * @param o
	 */
	public void register( Object o );
	public void setRegions( RegionSet set );
	
	public void setStartTick( int start );
	public void setTargetTick( int start );
	public int getTargetTick();
	public void setEndTick( int end );
	public int getEndTick();
	public void runUntil( int end );
	public void runFromTo( int start, int end );
	public void finish();
	
	public void addStatusListener( ScheduleStatusListener l );
}
