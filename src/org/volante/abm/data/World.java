package org.volante.abm.data;

import java.util.*;

import org.volante.abm.schedule.RunInfo;

public class World extends RegionSet
{
	/*
	 * Constructors, adding initial regions for convenience
	 */
	public World() {}
	public World( Regions...initialRegions ) { regions.addAll( Arrays.asList( initialRegions )); }
	public World( Collection<Region> initialRegions ) { regions.addAll(  initialRegions ); }
	
	@Deprecated //Just to make testing code easier. Ignore exception as it's just for testing
	public World( ModelData data, RunInfo runInfo, Region...initialRegions ) 
	{ 
		regions.addAll( Arrays.asList( initialRegions ));
		try
		{
			initialise( data, runInfo, null );
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
}
