package org.volante.abm.agent;

import java.util.*;

import org.volante.abm.data.*;
import org.volante.abm.models.ProductionModel;
import org.volante.abm.models.nullmodel.NullProductionModel;

import com.moseph.modelutils.fastdata.*;
/**
 * This is a default agent
 * @author jasper
 *
 */
public class DefaultAgent extends AbstractAgent
{
	/*
	 * Characteristic fields (define an agent)
	 */
	ProductionModel production = NullProductionModel.INSTANCE;
	double givingUp = -Double.MAX_VALUE;
	double givingIn = Double.MAX_VALUE;
	protected PotentialAgent type;
	public DefaultAgent() {}
	public DefaultAgent( String id, ModelData data )
	{
		this.id = id;
		initialise( data );
	}
	
	public DefaultAgent( PotentialAgent type, ModelData data, Region r, ProductionModel prod, double givingUp, double givingIn )
	{
		this.type = type;
		this.region = r;
		this.production = prod;
		this.givingUp = givingUp;
		this.givingIn = givingIn;
		initialise( data );
	}
	
	public DefaultAgent( PotentialAgent type, String id, ModelData data, Region r, ProductionModel prod, double givingUp, double givingIn )
	{
		this( type, data, r, prod, givingUp, givingIn );
		this.id = id;
	}
	
	public void initialise( ModelData data )
	{
		productivity = new DoubleMap<Service>( data.services );
	}

	public void updateSupply()
	{
		productivity.clear();
		for( Cell c : cells )
		{
			production.production( c, c.getModifiableSupply() );
			c.getSupply().addInto( productivity );
		}
	}
	
	public void considerGivingUp()
	{
		if( currentCompetitiveness < givingUp ) {
			giveUp();
		}
	}
	
	public boolean canTakeOver( Cell c, double incoming ) 
	{
		return incoming > (getCompetitiveness() + givingIn);
	}

	public UnmodifiableNumberMap<Service> supply( Cell c ) 
	{ 
		DoubleMap<Service> prod = productivity.duplicate();
		production.production( c, prod ); 
		return prod;
	}
	public void setProductionFunction( ProductionModel f ) { this.production = f; }
	public ProductionModel getProductionFunction() { return production; }
	public void setGivingUp( double g ) { this.givingUp = g; }
	public void setGivingIn( double g ) { this.givingIn = g; }
	public double getGivingUp() { return givingUp; }
	public double getGivingIn() { return givingIn; }
	public PotentialAgent getType() { return type; }
	
	public String infoString()
	{
		return "Giving up: " + givingUp + ", Giving in: " + givingIn + ", nCells: " + cells.size();
	}

}
