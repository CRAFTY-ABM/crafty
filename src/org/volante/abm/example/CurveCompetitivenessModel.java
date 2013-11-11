package org.volante.abm.example;

import java.io.IOException;
import java.util.*;

import org.apache.log4j.Logger;
import org.simpleframework.xml.*;
import org.simpleframework.xml.core.Persist;
import org.volante.abm.data.*;
import org.volante.abm.models.*;
import org.volante.abm.schedule.RunInfo;
import org.volante.abm.serialization.ABMPersister;
import org.volante.abm.visualisation.CurveCompetitivenessDisplay;

import com.csvreader.CsvReader;
import com.moseph.modelutils.curve.*;
import com.moseph.modelutils.fastdata.*;

/**
 * A simple model of competitiveness
 * @author dmrust
 *
 */
public class CurveCompetitivenessModel implements CompetitivenessModel
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
	
	/**
	 * A set of curves which are loaded in
	 */
	@ElementMap(inline=true,entry="curve",attribute=true,required=false,key="service")
	Map<String, Curve> serialCurves = new HashMap<String, Curve>();
	
	/**
	 * If this points to a csv file with the columns "Service","Intercept","Slope"
	 * this will be loaded as a set of linear functions with the given parameters
	 */
	@Attribute(required=false)
	String linearCSV = null;
	@Attribute(required=false)
	String serviceColumn = "Service";
	@Attribute(required=false)
	String interceptColumn = "Intercept";
	@Attribute(required=false)
	String slopeColumn = "Slope";
	
	Map<Service, Curve> curves = new HashMap<Service, Curve>();
	
	Logger log = Logger.getLogger( getClass() );
	ModelData data;
	RunInfo info;
	Region region;
	
	public void initialise( ModelData data, RunInfo info, Region extent ) throws Exception
	{
		this.data = data;
		this.info = info;
		this.region = extent;
		if( linearCSV != null ) loadLinearCSV( linearCSV );
		for( String s : serialCurves.keySet() )
		{
			Service service = data.services.forName( s );
			Curve c = serialCurves.get( s );
			if( service != null ) curves.put( service, c );
			else log.error( "Invalid Service: " + s + " got: " + data.services );
			log.info("Loaded curve for " + service + ": " + c);
		}
	}
	
	
	
	public double getCompetitveness( DemandModel demand, UnmodifiableNumberMap<Service> supply )
	{
		return getCompetitveness( demand, supply, false );
	}
	public double getCompetitveness( DemandModel demand, UnmodifiableNumberMap<Service> supply, boolean showWorking )
	{
		DoubleMap<Service> residual = demand.getResidualDemand().copy();
		residual.multiplyInto( 1.0/region.getNumCells(), residual );
		if( showWorking ) log.info("Using residual: " + residual.prettyPrint() );
		return addUpMarginalUtilities( residual, supply, showWorking );
	}
	
	public double getCompetitveness( DemandModel demand, UnmodifiableNumberMap<Service> supply, Cell cell )
	{
		DoubleMap<Service> residual = demand.getResidualDemand().copy();
		if( removeCurrentLevel ) cell.getSupply().addInto( residual );
		residual.multiplyInto( 1.0/region.getCells().size(), residual );
		return addUpMarginalUtilities( residual, supply );
	}
	
	public double addUpMarginalUtilities( UnmodifiableNumberMap<Service> residualDemand, UnmodifiableNumberMap<Service> supply )
	{
		return addUpMarginalUtilities( residualDemand, supply, false );
	}
	public double addUpMarginalUtilities( UnmodifiableNumberMap<Service> residualDemand, UnmodifiableNumberMap<Service> supply, boolean showWorking )
	{
		double sum = 0;
		for( Service s : supply.getKeySet() )
		{
			Curve c = curves.get( s ); /* Gets the curve parameters for this service	*/
			if( c == null ) log.fatal( "Missing curve for: " + s.getName() + " got: " + curves.keySet() );
			double res = residualDemand.getDouble( s );
			double marginal = c.sample( res ); /* Get the corresponding 'value' (y-value) for this level of unmet demand	*/
			double amount = supply.getDouble(s);
			if( removeNegative && marginal < 0 ) marginal = 0;
			double comp = marginal*amount;
			if( showWorking )
			{
				log.debug( String.format("Service: %10s, Residual: %5f, Marginal: %5f, Amount: %5f", s.getName(), res, marginal, amount ) );
				log.debug( "Curve: "+c.toString() );
			}
			sum += comp;
		}	
		return sum;
	}
	
	public boolean isRemoveCurrentLevel() { return removeCurrentLevel; }
	public void setRemoveCurrentLevel( boolean removeCurrentLevel ) { this.removeCurrentLevel = removeCurrentLevel; }
	public boolean isRemoveNegative() { return removeNegative; }
	public void setRemoveNegative( boolean removeNegative ) { this.removeNegative = removeNegative; }
	
	public void loadLinearCSV( String csvFile ) throws IOException
	{
		ABMPersister persister = info.getPersister();
		if( ! persister.csvFileOK( getClass(), csvFile, serviceColumn, interceptColumn, slopeColumn ) )
			return;
		CsvReader reader = info.getPersister().getCSVReader( csvFile );
		while( reader.readRecord() )
		{
			Service service = data.services.forName( reader.get(serviceColumn) );
			double intercept = Double.parseDouble( reader.get(interceptColumn) );
			double slope = Double.parseDouble( reader.get(slopeColumn) );
			curves.put( service, new LinearFunction( intercept, slope ));
		}
	}
	
	@Persist
	public void onWrite()
	{
		serialCurves.clear();
		for( Service s : curves.keySet() ) serialCurves.put( s.getName(), curves.get( s ));
	}
	
	public Map<Service, Curve> getCurves() { return curves; }
	public void setCurve( Service s, Curve c ) { curves.put( s, c ); }
	
	public CurveCompetitivenessDisplay getDisplay() { return new CurveCompetitivenessDisplay( this ); }
}
