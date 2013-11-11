package org.volante.abm.example;

import org.apache.log4j.Logger;
import org.simpleframework.xml.*;
import org.volante.abm.agent.*;
import org.volante.abm.data.*;
import org.volante.abm.models.ProductionModel;
import org.volante.abm.schedule.RunInfo;
import org.volante.abm.serialization.Initialisable;

import com.moseph.modelutils.fastdata.*;

public class SimplePotentialAgent implements PotentialAgent, Initialisable
{
	@Element
	ProductionModel production = new SimpleProductionModel();
	@Attribute
	double givingUp = -Double.MAX_VALUE;
	@Attribute
	double givingIn = -Double.MAX_VALUE;
	@Attribute
	String id = "PotentialAgent";
	@Attribute
	int serialID = UNKNOWN_SERIAL;
	ModelData data;
	RunInfo info;
	
	protected Logger log = Logger.getLogger( getClass() );
	
	public SimplePotentialAgent() {}
	
	public SimplePotentialAgent(String id, ModelData data, ProductionModel production, double givingUp, double givingIn )
	{
		this.id = id;
		this.production = production;
		this.givingUp = givingUp;
		this.givingIn = givingIn;
		this.data = data;
	}
	

	public DoubleMap<Service> getPotentialSupply( Cell cell )
	{
		DoubleMap<Service> map = data.serviceMap();
		production.production( cell, map );
		return map;
	}

	public Agent createAgent( Region region, Cell... cells )
	{
		DefaultAgent da = new DefaultAgent( this, id, data, region, production, givingUp, givingIn );
		region.setOwnership( da, cells );
		return da; 
	}

	public String getID() { return id; }
	public int getSerialID() { return serialID; }
	public double getGivingUp() { return givingUp; }
	public double getGivingIn() { return givingIn; }

	public void initialise( ModelData data, RunInfo info, Region r ) throws Exception
	{
		this.data = data;
		this.info = info;
		production.initialise( data, info, r );
		log.debug("Agent initialised: " + getID() );
		log.trace("Production: \n" + production);
	}
	
	public ProductionModel getProduction() { return production; }
	
	//public String toString() { return String.format( "SA: %s (@%X)", id, hashCode() ); }
	public String toString() { return String.format( "%s", id ); }
}

