package org.volante.abm.data;

import java.util.*;


import org.volante.abm.agent.*;
import org.volante.abm.schedule.RunInfo;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

import static java.lang.Math.*;

/**
 * Generic set of regions, allowing for multi-scale representation
 * @author dmrust
 *
 */
public class RegionSet implements Regions
{
	protected Set<Regions> regions = new HashSet<Regions>();
	Extent extent = new Extent();
	String id = "Unknown";
	
	public RegionSet() {}
	
	public RegionSet( Region ... regions )
	{
		for( Region r : regions ) addRegion( r );
	}

	/*
	 * Initialisation
	 */
	public void initialise( ModelData data, RunInfo info, Region region ) throws Exception
	{ 
		for( Regions r : regions )
		{
			r.initialise( data, info, null ); 
			extent.update( r.getExtent() );
		}
	}
	
	
	/*
	 * Access methods. Uses Guava to concatenate iterables for sub-regions
	 */
	public Iterable<Region> getAllRegions()
	{
		return Iterables.concat( Iterables.transform( regions, new Function<Regions, Iterable<Region>>() {
				public Iterable<Region> apply( Regions r ) { return r.getAllRegions(); }
		} ) );
	}

	public Iterable<Agent> getAllAgents()
	{
		return Iterables.concat( Iterables.transform( regions, new Function<Regions, Iterable<Agent>>() {
				public Iterable<Agent> apply( Regions r ) { return r.getAllAgents(); }
		} ) );
	}

	public Iterable<Cell> getAllCells() 
	{ 
		return Iterables.concat( Iterables.transform( regions, new Function<Regions, Iterable<Cell>>() {
				public Iterable<Cell> apply( Regions r ) { return r.getAllCells(); }
		} ) );
	}
	
	public Iterable<PotentialAgent> getAllPotentialAgents()
	{
		return Iterables.concat( Iterables.transform( regions, new Function<Regions, Iterable<PotentialAgent>>() {
				public Iterable<PotentialAgent> apply( Regions r ) { return r.getAllPotentialAgents(); }
		} ) );
	}

	public void addRegion( Region r )
	{ 
		extent.update( r.getExtent() );
		regions.add(r);
	}

	public Collection<Regions> getRegions()
	{ return Collections.unmodifiableCollection( regions ); }

	public Extent getExtent() { return extent; }
	public String getID() { return id; }
	public void setID( String id ) { this.id = id; }
	public int getNumCells()
	{
		int c = 0;
		for( Regions r : regions ) c += r.getNumCells();
		return c;
	}

}
