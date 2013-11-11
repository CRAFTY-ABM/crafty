package org.volante.abm.example;

import java.io.IOException;
import java.util.*;

import org.apache.log4j.Logger;
import org.simpleframework.xml.Attribute;
import org.volante.abm.data.*;
import org.volante.abm.models.*;
import org.volante.abm.schedule.*;
import org.volante.abm.visualisation.RegionalDemandDisplay;

import com.moseph.modelutils.curve.*;
import com.moseph.modelutils.fastdata.*;

/**
 * Model demand entirely at a regional level. Demand is averaged across
 * all cells in the region.
 * 
 * When a cell changes, calculate new demand based on the difference from new to old
 * @author dmrust
 *
 */
public class RegionalDemandModel implements DemandModel, PreTickAction, PostTickAction
{
	Region region;
	@Attribute(required=false)
	boolean updateOnAgentChange = true;
	Map<Cell,DoubleMap<Service>> supply = new HashMap<Cell, DoubleMap<Service>>();
	DoubleMap<Service> totalSupply = null;
	DoubleMap<Service> residual = null;
	DoubleMap<Service> perCellResidual = null;
	DoubleMap<Service> demand = null;
	DoubleMap<Service> perCellDemand = null;
	RunInfo runInfo = null;
	ModelData modelData = null;
	
	@Attribute(required=false)
	String demandCSV = null;
	@Attribute(required=false)
	String yearCol = "Year";
	
	Logger log = Logger.getLogger( getClass() );
	
	Map<Service, Curve> demandCurves = new HashMap<Service, Curve>();
	
	public void initialise( ModelData data, RunInfo info, Region r ) throws Exception
	{
		this.region = r;
		this.runInfo = info;
		this.modelData = data;
		totalSupply = data.serviceMap();
		residual = data.serviceMap();
		perCellResidual = data.serviceMap();
		demand = data.serviceMap();
		perCellDemand = data.serviceMap();
		if( updateOnAgentChange )
			for( Cell c : r.getCells() )
				supply.put( c, data.serviceMap() );
		if( demandCSV != null ) loadDemandCurves();
	}

	public DoubleMap<Service> getResidualDemand() { return residual; }
	public DoubleMap<Service> getDemand() { return demand; }
	public DoubleMap<Service> getDemand( Cell c ) { return perCellDemand; }
	public DoubleMap<Service> getResidualDemand( Cell c ) { return perCellResidual; }
	public DoubleMap<Service> getSupply() { return totalSupply; }

	public void agentChange( Cell c )
	{
		if( updateOnAgentChange )
		{
			totalSupply.subtractInto( supply.get(c), totalSupply );
			c.getSupply().copyInto( supply.get(c) );
			c.getSupply().addInto( totalSupply );
			recalculateResidual();
		}
	}
	
	public void setDemand( UnmodifiableNumberMap<Service> dem )
	{
		dem.copyInto( demand );
		updateSupply();
	}

	/**
	 * 
	 */
	public void updateSupply() 
	{ 
		if( updateOnAgentChange )
		{
			for( Cell c : region.getCells() )
				c.getSupply().copyInto( supply.get(c) );
		}
		totalSupply.clear();
		for( Cell c : region.getCells() )
			c.getSupply().addInto( totalSupply );
		recalculateResidual();
			
		
	}
	public void recalculateResidual()
	{
		demand.multiplyInto( 1.0/supply.size(), perCellDemand );
		demand.subtractInto( totalSupply, residual );
		residual.multiplyInto( 1.0/supply.size(), perCellResidual );
	}

	public void preTick()
	{
		int tick = runInfo.getSchedule().getCurrentTick();
		log.info("Loading demand from tick: " + tick );
		for( Service s : demand.getKeys() )
			if( demandCurves.containsKey( s ))
				demand.put( s, demandCurves.get( s ).sample( tick ) );
		log.info("Demand: " + demand.prettyPrint() );
	}
	
	public void postTick()
	{
		log.info("Demand: " + demand.prettyPrint() );
		log.info("Supply: " + totalSupply.prettyPrint() );
		log.info("Residual: " + residual.prettyPrint() );
		log.info("Marginal Utilities: " + getMarginalUtilities().prettyPrint() );
	}
	
	//Generally shouldn't use the competition model directly as it ignores institutions, but it's OK here.
	@SuppressWarnings("deprecation") 
	public DoubleMap<Service> getMarginalUtilities()
	{
		DoubleMap<Service> utilities = modelData.serviceMap();
		CompetitivenessModel comp = region.getCompetitionModel();
		for( Service s : modelData.services )
		{
			DoubleMap<Service> serv = modelData.serviceMap() ;
			serv.clear();
			serv.put( s, 1 );
			if( comp instanceof CurveCompetitivenessModel ) ((CurveCompetitivenessModel)comp).getCompetitveness( this, serv, true );
			double score = comp.getCompetitveness( this, serv );
			System.out.println("Serv: " + serv.prettyPrint() + " -> " + score );
			utilities.put( s, score );
		}
		return utilities;
	}
	
	void loadDemandCurves() throws IOException
	{
		Map<String, LinearInterpolator> curves = runInfo.getPersister().csvVerticalToCurves( demandCSV, yearCol, modelData.services.names() );
		for( Service s : modelData.services )
			if( curves.containsKey( s.getName() ))
				demandCurves.put( s, curves.get(s.getName()));
	}

	public RegionalDemandDisplay getDisplay() { return new RegionalDemandDisplay( this ); }

}
