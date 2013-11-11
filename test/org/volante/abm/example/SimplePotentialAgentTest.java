package org.volante.abm.example;

import static org.junit.Assert.*;

import java.util.*;

import org.junit.Test;
import org.volante.abm.agent.*;
import org.volante.abm.data.Cell;

public class SimplePotentialAgentTest extends BasicTests
{
	SimpleProductionModel p1 = new SimpleProductionModel();

	@Test
	public void test()
	{
		SimplePotentialAgent p = new SimplePotentialAgent("TestAgent",modelData,p1, 5, 3);
		DefaultAgent ag = (DefaultAgent)p.createAgent( r1, c11, c12 );
		assertEquals( p1, ag.getProductionFunction() );
		assertEquals( "TestAgent", ag.getID() );
		assertEquals( 5, ag.getGivingUp(), 0.0000001 );
		assertEquals( 3, ag.getGivingIn(), 0.0000001 );
		assertEquals( ag, c11.getOwner() );
		assertEquals( ag, c12.getOwner() );
		assertEquals( Agent.NOT_MANAGED, c13.getOwner() );
		checkSet( "Ownership of new agent", ag.getCells(), c11, c12 );
	}
	
	@Test
	public void testDeserealisation() throws Exception
	{
		SimplePotentialAgent p = runInfo.getPersister().readXML( SimplePotentialAgent.class, "xml/LowIntensityArableAgent.xml" );
		p.initialise( modelData, runInfo, null );
		testLowIntensityArableAgent( p );
	}
	
	public static void testLowIntensityArableAgent( SimplePotentialAgent p )
	{
		SimpleProductionTest.testLowIntensityArableProduction( (SimpleProductionModel) p.production );
		assertEquals( 0.5, p.givingUp, 0.0001 );
		assertEquals( 1, p.givingIn, 0.0001 );
		assertEquals( "LowIntensityArable", p.id );
		assertEquals( 1, p.serialID );
	}
	
	public static void testHighIntensityArableAgent( SimplePotentialAgent p )
	{
		SimpleProductionTest.testHighIntensityArableProduction( (SimpleProductionModel) p.production );
		assertEquals( 0.5, p.givingUp, 0.0001 );
		assertEquals( 1, p.givingIn, 0.0001 );
		assertEquals( "HighIntensityArable", p.id );
		assertEquals( 2, p.serialID );
	}
	
	public static void testCommercialForestryAgent( SimplePotentialAgent p )
	{
		SimpleProductionTest.testCommercialForestryProduction( (SimpleProductionModel) p.production );
		assertEquals( 0.1, p.givingUp, 0.0001 );
		assertEquals( 3, p.givingIn, 0.0001 );
		assertEquals( "CommercialForestry", p.id );
		assertEquals( 3, p.serialID );
	}

}
