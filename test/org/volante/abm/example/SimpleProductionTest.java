package org.volante.abm.example;

import static java.lang.Math.*;
import static org.junit.Assert.*;

import org.junit.Test;
import org.volante.abm.data.*;
import static org.volante.abm.example.SimpleService.*;
import static org.volante.abm.example.SimpleCapital.*;

import com.moseph.modelutils.fastdata.DoubleMap;

public class SimpleProductionTest extends BasicTests
{
	DoubleMap<Service> production = new DoubleMap<Service>( simpleServices );
	DoubleMap<Service> expected = new DoubleMap<Service>( simpleServices );
	SimpleProductionModel fun = new SimpleProductionModel();
	
	/*
	 * Capitals:
	 * HUMAN(0), INFRASTRUCTURE(1), ECONOMIC(2), NATURAL_GRASSLAND(3),
	 * NATURAL_FOREST(4), NATURAL_CROPS(5), NATURE_VALUE(6)
	 * 
	 * Services:
	 * HOUSING(0), TIMBER(1), FOOD(2), RECREATION(3),
	 * 
	 */

	@Test
	public void testProduction()
	{
		//fun.initialise( modelData );
		checkProduction( "All zeros should be full production", 1, 1, 1, 1 );
		fun.setWeight( HUMAN, HOUSING, 1 );
		checkProduction( "Putting a 1 in the matrix stops production", 0, 1, 1, 1 );
		c11.setBaseCapitals( capitals( 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5 ) );
		checkProduction( "Giving all baseCapitals 0.5 allows production at 0.5", 0.5, 1, 1, 1 );
		fun.setWeight( NATURAL_CROPS, FOOD, 1 );
		checkProduction( "Setting Natural Crops weight changes food production", 0.5, 1, 0.5, 1 );
		fun.setWeight( NATURAL_CROPS, FOOD, 0.5 );
		checkProduction( "Setting Natural Crops weight changes food production", 0.5, 1, sqrt(0.5), 1 );
		fun.setWeight( FOOD, 5 );
		checkProduction( "Setting production weights", 0.5, 1, 5*sqrt(0.5), 1 );
	}
	
	@Test
	public void testProductionForExampleValues()
	{
		fun = new SimpleProductionModel( extensiveFarmingCapitalWeights, extensiveFarmingProductionWeights );
		checkProduction("Setting everything all at once and weighting it", cellCapitalsA, extensiveFarmingOnCA);
		checkProduction("Setting everything all at once and weighting it", cellCapitalsB, extensiveFarmingOnCB);
		fun = new SimpleProductionModel( forestryCapitalWeights, forestryProductionWeights );
		checkProduction("Setting everything all at once and weighting it", cellCapitalsA, forestryOnCA);
		checkProduction("Setting everything all at once and weighting it", cellCapitalsB, forestryOnCB);
	}
	
	@Test
	public void testDeserealisation() throws Exception
	{
		SimpleProductionModel model = runInfo.getPersister().readXML( SimpleProductionModel.class, "xml/LowIntensityArableProduction.xml" );
		model.initialise( modelData, runInfo, null );
		testLowIntensityArableProduction( model );
	}
	
	public static void testLowIntensityArableProduction( SimpleProductionModel model )
	{
		assertEqualMaps( services(0,0,4,0), model.productionWeights );
		assertEqualMaps( capitals(0,0,0,0,0,1,0), model.captialWeights.getRow( FOOD ) );
		assertEqualMaps( capitals(0,0,0,0,0,0,0), model.captialWeights.getRow( TIMBER ) );
		assertEqualMaps( capitals(0,0,0,0,0,0,0), model.captialWeights.getRow( HOUSING ) );
		assertEqualMaps( capitals(0,0,0,0,0,0,0), model.captialWeights.getRow( RECREATION ) );
	}
	
	public static void testHighIntensityArableProduction( SimpleProductionModel model )
	{
		assertEqualMaps( services(0,0,10,0), model.productionWeights );
		assertEqualMaps( capitals(0.5,0.5,0.5,0,0,1,0), model.captialWeights.getRow( FOOD ) );
		assertEqualMaps( capitals(0,0,0,0,0,0,0), model.captialWeights.getRow( TIMBER ) );
		assertEqualMaps( capitals(0,0,0,0,0,0,0), model.captialWeights.getRow( HOUSING ) );
		assertEqualMaps( capitals(0,0,0,0,0,0,0), model.captialWeights.getRow( RECREATION ) );
	}
	
	public static void testCommercialForestryProduction( SimpleProductionModel model )
	{
		assertEqualMaps( services(0,8,0,0), model.productionWeights );
		assertEqualMaps( capitals(0,0,0,0,0,0,0), model.captialWeights.getRow( FOOD ) );
		assertEqualMaps( capitals(0,0,0,0,1,0,0), model.captialWeights.getRow( TIMBER ) );
		assertEqualMaps( capitals(0,0,0,0,0,0,0), model.captialWeights.getRow( HOUSING ) );
		assertEqualMaps( capitals(0,0,0,0,0,0,0), model.captialWeights.getRow( RECREATION ) );
	}
	
	void checkProduction( String msg, double... vals )
	{
		checkProduction( msg, services( vals ));
	}
	
	void checkProduction( String msg, DoubleMap<Capital> cellCapitals, DoubleMap<Service> expected)
	{
		c11.setBaseCapitals( cellCapitals );
		fun.production( c11, production );
		assertEqualMaps( msg, expected, production );
	}
	void checkProduction( String msg, DoubleMap<Service> expected)
	{
		fun.production( c11, production );
		assertEqualMaps( msg, expected, production );
	}

}
