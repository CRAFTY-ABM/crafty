package org.volante.abm.example;

import static org.volante.abm.example.SimpleCapital.*;
import static org.volante.abm.example.SimpleService.*;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.simpleframework.xml.Attribute;
import org.volante.abm.data.*;
import org.volante.abm.models.ProductionModel;
import org.volante.abm.schedule.RunInfo;

import com.moseph.modelutils.distribution.Distribution;
import com.moseph.modelutils.fastdata.*;

import static java.lang.Math.*;

/**
 * Simple exponential multiplicative function, i.e.:
 * 
 * p_s = p_max * c_1 ^ w_1 * c_2 ^ w_2 *...*c_n ^ w_n
 * @author dmrust
 *
 */
public class SimpleProductionModel implements ProductionModel
{
	DoubleMatrix<Capital, Service> captialWeights = 
			new DoubleMatrix<Capital, Service>( simpleCapitals, simpleServices );
	DoubleMap<Service> productionWeights = new DoubleMap<Service>( simpleServices, 1 );
	
	@Attribute(required=false)
	String csvFile = null;
	
	
	public SimpleProductionModel() {}
	/**
	 * Takes an array of capital weights, in the form:
	 * {
	 * 	{ c1s1, c2s1 ... } //Weights for service 1
	 * 	{ c1s2, c2s2 ... } //Weights for service 2
	 *  ...
	 * i.e. first index is Services, second is baseCapitals
	 * @param weights
	 * @param productionWeights
	 */
	public SimpleProductionModel( double[][] weights, double[] productionWeights )
	{
		this.captialWeights.putT(weights);
		this.productionWeights.put( productionWeights );
	}
	
	public void initialise( ModelData data, RunInfo info, Region r ) throws Exception
	{
		if( csvFile != null )
			initWeightsFromCSV( data, info );
		else
		{
			captialWeights = new DoubleMatrix<Capital, Service>( data.capitals, data.services );
			productionWeights = new DoubleMap<Service>( data.services );
		}
	}
	
	void initWeightsFromCSV( ModelData data, RunInfo info ) throws Exception
	{
		captialWeights = info.getPersister().csvToMatrix( csvFile, data.capitals, data.services );
		productionWeights = info.getPersister().csvToDoubleMap( csvFile, data.services, "Production");
	}
	
	/**
	 * Sets the effect of a capital on provision of a service
	 * @param c
	 * @param s
	 * @param weight
	 */
	public void setWeight( Capital c, Service s, double weight )
	{
		captialWeights.put( c, s, weight );
	}
	/**
	 * Sets the maximum level for a service
	 * @param s
	 * @param weight
	 */
	public void setWeight( Service s, double weight )
	{
		productionWeights.put( s, weight );
	}
	
	public UnmodifiableNumberMap<Service> getProductionWeights() { return productionWeights; }
	public DoubleMatrix<Capital, Service> getCapitalWeights() { return captialWeights; }
	
	public void production( Cell cell, DoubleMap<Service> production )
	{
		UnmodifiableNumberMap<Capital> capitals = cell.getEffectiveCapitals();
		production( capitals, production );
	}
	
	public void production( UnmodifiableNumberMap<Capital> capitals, DoubleMap<Service> production)
	{
		for( Service s : captialWeights.rows() )
		{
			double val = 1;
			for( Capital c : captialWeights.cols() )
				val = val * pow( capitals.getDouble( c ), captialWeights.get( c, s ) ) ;
			production.putDouble( s, productionWeights.get(s) * val );
		}
	}
	
	public String toString()
	{
		return "Production Weights: " + productionWeights.prettyPrint() + "\nCapital Weights:"+captialWeights.toMap();
	}

	/**
	 * Creates a copy of this model, but with noise added to either the production weights
	 * or the importance weights. Eityher or both distributions can be null for zero noise
	 * @param data
	 * @param production
	 * @param importance
	 * @return
	 */
	public SimpleProductionModel copyWithNoise( ModelData data, Distribution production, Distribution importance )
	{
		SimpleProductionModel pout = new SimpleProductionModel();
		pout.captialWeights = captialWeights.duplicate();
		pout.productionWeights = productionWeights.duplicate();
		for( Service s : data.services )
		{
			if( production == null ) pout.setWeight( s, productionWeights.getDouble( s ) );
			else pout.setWeight( s, productionWeights.getDouble( s ) + production.sample() );
			
			for( Capital c : data.capitals )
			{
				if( importance == null ) pout.setWeight( c, s, captialWeights.get( c, s ) );
				else pout.setWeight( c, s, captialWeights.get( c, s ) + importance.sample() );
			}
		}
		return pout;
	}

}
	