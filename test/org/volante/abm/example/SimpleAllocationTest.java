package org.volante.abm.example;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.volante.abm.agent.Agent;
import org.volante.abm.agent.DefaultAgent;
import org.volante.abm.agent.PotentialAgent;

public class SimpleAllocationTest extends BasicTestsUtils
{
	@SuppressWarnings("deprecation")
	@Test
	public void testSimpleAllocation() throws Exception
	{
		log.info("Test simple Allocation...");
		log.info(r1.getPotentialAgents());
		log.info(r2.getPotentialAgents());
		assertEquals( potentialAgents, r1.getPotentialAgents() );
		allocation = persister.roundTripSerialise( allocation );
		r1.setAvailable( c11 );
		c11.setBaseCapitals( cellCapitalsA );
		assertNotNull( r1.getCompetitiveness( c11 ));
		PotentialAgent ag = r1.getPotentialAgents().iterator().next();
		assertNotNull( ag );
		print( r1.getCompetitiveness( c11 ), ag.getPotentialSupply( c11 ), c11 );
		
		assertTrue(r1.getCells().contains(c11));
		assertTrue(demandR1.demand.containsKey(c11));
		assertEquals(demandR1, r1.getDemandModel());

		demandR1.setResidual( c11, services(5, 0, 5, 0) );
		r1.getAllocationModel().allocateLand( r1 );
		assertEquals( farming.getID(), c11.getOwner().getID() ); //Make sure that demand for food gives a farmer
		print(c11.getOwner().getID());
		
		demandR1.setResidual( c11, services(0, 0, 0, 0) );
		((DefaultAgent)c11.getOwner()).setGivingUp( 1 );
		c11.getOwner().updateCompetitiveness();
		c11.getOwner().considerGivingUp();
		
		assertEquals(Agent.NOT_MANAGED,c11.getOwner());
		
		demandR1.setResidual( c11, services(0, 8, 0, 0) );
		r1.getAllocationModel().allocateLand( r1 );
		assertEquals( forestry.getID(), c11.getOwner().getID() ); //Make sure that demand for food gives a farmer
	}
}
