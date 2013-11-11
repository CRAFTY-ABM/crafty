package org.volante.abm.example;

import static org.junit.Assert.*;


import org.junit.*;
import org.volante.abm.agent.DefaultAgent;
import org.volante.abm.data.Service;

import com.moseph.modelutils.curve.Curve;
import com.moseph.modelutils.fastdata.DoubleMap;
import static org.volante.abm.example.SimpleService.*;

public class SimpleRegionalDemandTest extends BasicTests
{
	RegionalDemandModel dem = new RegionalDemandModel();
	private DefaultAgent farmer;
	private DefaultAgent forester;

	@Before
	public void setupFunction() throws Exception
	{
		initCells();
		runInfo.setUseInstitutions( false );
		dem.initialise( modelData, runInfo, r1 ) ;
		r1.setDemandModel( dem );
		
		//Create a farmer, get it farming
		farmer = (DefaultAgent) farming.createAgent( r1 );
		farmer.initialise( modelData );
		
		//Create a farmer, get it farming
		forester = (DefaultAgent) forestry.createAgent( r1 );
		forester.initialise( modelData );
	}
	
	@Test
	public void testDemandExampleValues()
	{
		DoubleMap<Service> demand = services(9,9,9,9);
		DoubleMap<Service> cellDemand = services(1,1,1,1);
		//Setup demand for the whole 9 cell region
		dem.setDemand( demand );
		assertEqualMaps( cellDemand, dem.getDemand( c11 ) );
		
		r1.setOwnership( farmer, c11 );
		c11.setBaseCapitals( cellCapitalsA );
		c12.setBaseCapitals( cellCapitalsA );
		farmer.updateSupply();
		farmer.updateCompetitiveness();
		assertEqualMaps( cellCapitalsA, c11.getBaseCapitals() );
		assertEquals( c11.getBaseCapitals(), c11.getEffectiveCapitals() );
		assertEqualMaps( cellCapitalsA, c11.getEffectiveCapitals() );
		//For a "farmer" agent, on a cell with example baseCapitals A, production should be extensiveFarmingOnCA
		assertEqualMaps( "Extensive farming production is correct", extensiveFarmingOnCA, farmer.supply( c11 ));
		
		//Update demand model
		dem.updateSupply();
		
		//Check that we're getting the correct level of supply, on the cell and total
		assertEqualMaps( extensiveFarmingOnCA, dem.supply.get(c11) );
		assertEqualMaps( extensiveFarmingOnCA, dem.totalSupply );
		
		//Residual demand should be expected - production
		DoubleMap<Service> expRes = demand.duplicate();
		demand.subtractInto( extensiveFarmingOnCA, expRes );
		assertEqualMaps( expRes, dem.residual );
		
		DoubleMap<Service> expResCell = services(9,9,9,9);
		expRes.multiplyInto( 1.0/9, expResCell );
		assertEqualMaps( expResCell, dem.getResidualDemand( c11 ) );
		
		r1.setOwnership( forester, c11 );
		//Check that we're getting the correct level of supply, on the cell and total
		assertEqualMaps( forestryOnCA, dem.supply.get(c11) );
		assertEqualMaps( forestryOnCA, dem.totalSupply );
		
		//Residual demand should be expected - production
		demand.subtractInto( forestryOnCA, expRes );
		assertEqualMaps( expRes, dem.residual );
		
		expRes.multiplyInto( 1.0/9, expResCell );
		assertEqualMaps( expResCell, dem.getResidualDemand( c11 ) );
		
		r1.setOwnership( forester, c12 );
		assertEqualMaps( forestryOnCA, c12.getSupply() );
		//Check that we're getting the correct level of supply, on the cell and total
		DoubleMap<Service> expSup = services(0,0,0,0);
		forestryOnCA.multiplyInto( 2, expSup );
		assertEqualMaps( forestryOnCA, dem.supply.get(c11) );
		assertEqualMaps( forestryOnCA, dem.supply.get(c12) );
		assertEqualMaps( expSup, dem.totalSupply );
		
		demand.subtractInto( expSup, expRes );
		assertEqualMaps( expRes, dem.residual );
		expRes.multiplyInto( 1.0/9, expResCell );
		assertEqualMaps( expResCell, dem.getResidualDemand( c11 ) );
		
	}
	
	@Test
	public void testLoadingDemandCurves() throws Exception
	{
		RegionalDemandModel model = new RegionalDemandModel();
		model.demandCSV = "csv/Region1Demand.csv";
		
		model.initialise( modelData, runInfo, r1 );
		
		//Just a few spot checks to have the idea the curves are loaded...
		Curve foodDem = model.demandCurves.get(FOOD);
		assertNotNull( foodDem );
		assertEquals( 90, foodDem.sample( 2000 ), 0.0001 );
		assertEquals( 94, foodDem.sample( 2002 ), 0.0001 );
		assertEquals( 100, foodDem.sample( 2005 ), 0.0001 );
		
		for( Service s : modelData.services ) assertNotNull( model.demandCurves.get(s));
	}

}
