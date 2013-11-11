package org.volante.abm.example;

import org.simpleframework.xml.Attribute;
import org.volante.abm.data.*;
import org.volante.abm.models.*;
import org.volante.abm.schedule.RunInfo;
import org.volante.abm.visualisation.SimpleCompetitivenessDisplay;

import com.moseph.modelutils.fastdata.*;

/**
 * A simple model of competitiveness
 * @author dmrust
 *
 */
public class SimpleCompetitivenessModel implements CompetitivenessModel
{
	/**
	 * If set to true, then the current supply will be added back to the residual demand, so 
	 * competitiveness is calculated as if the cell is currently empty
	 */
	@Attribute(required=false)
	boolean removeCurrentLevel = false;
	
	/**
	 * If set to true, all negative demand (i.e. oversupply) is removed from the dot product
	 */
	@Attribute(required=false)
	boolean removeNegative = false;
	
	
	public double getCompetitveness( DemandModel demand, UnmodifiableNumberMap<Service> supply )
	{
		DoubleMap<Service> residual = demand.getResidualDemand().copy();
		return addUpMarginalUtilities( residual, supply );
	}
	
	public double getCompetitveness( DemandModel demand, UnmodifiableNumberMap<Service> supply, Cell cell )
	{
		DoubleMap<Service> residual = demand.getResidualDemand( cell ).copy();
		if( removeCurrentLevel ) cell.getSupply().addInto( residual );
		return addUpMarginalUtilities( residual, supply );
	}
	
	public double addUpMarginalUtilities( UnmodifiableNumberMap<Service> residual, UnmodifiableNumberMap<Service> supply )
	{
		if( ! removeNegative ) return supply.dotProduct( residual );
		DoubleMap<Service> res = (DoubleMap<Service>)residual;
		res.setMin( 0 );
		return supply.dotProduct( res );
	}


	public boolean isRemoveCurrentLevel() { return removeCurrentLevel; }
	public void setRemoveCurrentLevel( boolean removeCurrentLevel ) { this.removeCurrentLevel = removeCurrentLevel; }
	public boolean isRemoveNegative() { return removeNegative; }
	public void setRemoveNegative( boolean removeNegative ) { this.removeNegative = removeNegative; }
	public void initialise( ModelData data, RunInfo info, Region r ){}

	public CompetitivenessDisplay getDisplay() { return new SimpleCompetitivenessDisplay( this ); }
}
