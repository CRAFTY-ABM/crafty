package org.volante.abm.data;

import org.volante.abm.agent.Agent;
import org.volante.abm.institutions.Institutions;
import org.volante.abm.schedule.RunInfo;
import org.volante.abm.serialization.Initialisable;

import com.moseph.modelutils.fastdata.*;

/**
 * A generic cell, with levels of baseCapitals, supply and demand for 
 * services and residual demand
 * 
 * A cell is can be owned by an agent, or by the null agent.
 * @author dmrust
 *
 */
public class Cell implements Initialisable
{
	/*
	 * Internal data
	 */
	DoubleMap<Capital> baseCapitals; //Current levels of baseCapitals
	DoubleMap<Capital> effectiveCapitals; //Current levels of baseCapitals
	//DoubleMap<Service> demand; //Current levels of spatialised demand
	DoubleMap<Service> supply; //Current levels of spatialised supply
	//DoubleMap<Service> residual; //Residual demand
	Agent owner = Agent.NOT_MANAGED;
	Region region;
	String id = null;
	int x = 0;
	int y = 0;
	boolean initialised = false;
	
	public Cell() {}
	public Cell(int x, int y) 
	{
		this.x= x;
		this.y= y;
		this.id = x+","+y;
	}
	
	/*
	 * Initialisation
	 */
	public void initialise( ModelData data, RunInfo info, Region region )
	{
		if( initialised ) return;
		this.region = region;
		initialised = true;
		baseCapitals = data.capitalMap();
		if( info.useInstitutions() )
			effectiveCapitals = data.capitalMap(); //Start with them being the same
		else
			effectiveCapitals = baseCapitals;
		supply = data.serviceMap();
	}
	
	/*
	 * Capitals
	 */
	public DoubleMap<Capital> getModifiableEffectiveCapitals() { return effectiveCapitals; }
	public UnmodifiableNumberMap<Capital> getEffectiveCapitals() { return effectiveCapitals; }
	
	public DoubleMap<Capital> getModifiableBaseCapitals() { return baseCapitals; }
	public UnmodifiableNumberMap<Capital> getBaseCapitals() { return baseCapitals; }
	
	public void setBaseCapitals( UnmodifiableNumberMap<Capital> c ) { baseCapitals.copyFrom( c ); }
	public void setEffectiveCapitals( UnmodifiableNumberMap<Capital> c ) { effectiveCapitals.copyFrom( c ); }
	public void initEffectiveCapitals() { effectiveCapitals.copyFrom( baseCapitals ); }
	
	/*
	 * Ownership
	 */
	public Agent getOwner() { return owner; }
	public void setOwner( Agent o ) { owner = o; }
	public String getOwnerID() { return ( owner == null ) ? "None" : owner.getID() ; }
	
	/*
	 * Supply and demand
	 */
	public void setSupply( UnmodifiableNumberMap<Service> s ) { supply.copyFrom( s ); }
	
	public UnmodifiableNumberMap<Service> getSupply() { return supply; }
	/**
	 * Allows for updating of the cell's supply without creating intermediate maps
	 * @return
	 */
	public DoubleMap<Service> getModifiableSupply() { return supply; }
	public String toString() { if( id != null ) return id; return super.toString(); }
	
	public void resetSupply() { supply.clear(); }
	
	
	public int getX()
	{
		return x;
	}
	public int getY()
	{
		return y;
	}
	public String getRegionID()
	{
		if( region == null ) return "Unknown";
		return region.getID();
	}
	public Region getRegion()
	{
		return region;
	}
	
	public boolean isInitialised() { return initialised; }
}
