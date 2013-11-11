package org.volante.abm.example;

import static org.junit.Assert.*;

import org.junit.Test;
import org.volante.abm.agent.*;

public class SimpleAllocationTest extends BasicTests
{
	@SuppressWarnings("deprecation")
	@Test
	public void testSimpleAllocation() throws Exception
	{
		System.out.println(r1.getPotentialAgents());
		System.out.println(r2.getPotentialAgents());
		assertEquals( potentialAgents, r1.getPotentialAgents() );
		allocation = persister.roundTripSerialise( allocation );
		r1.setAvailable( c11 );
		c11.setBaseCapitals( cellCapitalsA );
		assertNotNull( r1.getCompetitiveness( c11 ));
		PotentialAgent ag = r1.getPotentialAgents().iterator().next();
		assertNotNull( ag );
		print( r1.getCompetitiveness( c11 ), ag.getPotentialSupply( c11 ), c11 );
		
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
