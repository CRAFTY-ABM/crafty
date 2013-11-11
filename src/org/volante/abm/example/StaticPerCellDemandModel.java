package org.volante.abm.example;

import java.util.*;

import org.apache.log4j.Logger;
import org.volante.abm.data.*;
import org.volante.abm.models.*;
import org.volante.abm.schedule.RunInfo;
import org.volante.abm.visualisation.StaticPerCellDemandDisplay;

import com.moseph.modelutils.fastdata.*;

public class StaticPerCellDemandModel implements DemandModel
{
	Map<Cell,DoubleMap<Service>> demand = new HashMap<Cell, DoubleMap<Service>>();
	Map<Cell,DoubleMap<Service>> residual = new HashMap<Cell, DoubleMap<Service>>();
	ModelData data = null;
	Regions region;
	Logger log = Logger.getLogger( getClass() );
	
	public void initialise( ModelData data, RunInfo info, Region r )
	{
		this.data = data;
		this.region = r;
		for( Cell c : r.getCells() )
		{
			demand.put( c, data.serviceMap() );
			residual.put( c, data.serviceMap() );
		}
	}

	public DoubleMap<Service> getDemand() { log.fatal("Regional demand not implemented in per cell demand model");return null; }
	public DoubleMap<Service> getSupply() { log.fatal("Regional supply not implemented in per cell demand model");return null; }
	public DoubleMap<Service> getMarginalUtilities() { log.fatal("Regional marginal utilities not implemented in per cell demand model");return null; }
	public DoubleMap<Service> getDemand( Cell c ) { return demand.get( c ); }
	public DoubleMap<Service> getResidualDemand( Cell c ) { return residual.get( c ); }
	/**
	 * Not implemented yet!
	 */
	public DoubleMap<Service> getResidualDemand() { return null; }

	public void agentChange( Cell c )
	{
		demand.get( c ).subtractInto( c.getSupply(), residual.get( c ) );
	}
	
	public void setResidual( Cell c, UnmodifiableNumberMap<Service> res )
	{
		res.copyInto( residual.get( c ) );
	}
	public void setDemand( Cell c, UnmodifiableNumberMap<Service> dem )
	{
		dem.copyInto( demand.get( c ) );
		updateSupply( c );
	}
	
	public void updateSupply( Cell c )
	{
		demand.get( c ).subtractInto( c.getSupply(), residual.get( c ) );
	}

	/**
	 * Do nothing
	 */
	public void updateSupply() 
	{ 
		for( Cell c : region.getAllCells() ) updateSupply( c );
	}

	public StaticPerCellDemandDisplay getDisplay() { return new StaticPerCellDemandDisplay( this ); }
}
