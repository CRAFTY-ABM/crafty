package org.volante.abm.example;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.volante.abm.agent.Agent;
import org.volante.abm.agent.DefaultAgent;
import org.volante.abm.agent.PotentialAgent;
import org.volante.abm.data.Cell;

public class SimpleAllocationTest extends BasicTestsUtils
{
	
	static final String	PROPORTION_ALLOCATION_XML	= "xml/SimpleProportionAllocation.xml";
	static final double	PROPORTION					= 0.3;
	
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
	
	@SuppressWarnings("deprecation")
	@Test
	public void testProportionalAllocation() {
		
		persister = runInfo.getPersister();
		try {
			this.allocation = persister.read(SimpleAllocationModel.class,
				persister.getFullPath(PROPORTION_ALLOCATION_XML, this.r1.getPeristerContextExtra()));
			this.allocation.initialise(modelData, runInfo, r1);
			r1.setAllocationModel(this.allocation);
		} catch (Exception exception) {
			exception.printStackTrace();
		}

		log.info("Test simple allocation of proportion of available cells...");

		assertEquals(potentialAgents, r1.getPotentialAgents());

		int numCellsTotal = r1.getNumCells();
		for (Cell c : r1.getAllCells()) {
			c.setBaseCapitals(cellCapitalsA);
			r1.setAvailable(c);
			demandR1.setResidual(c, services(0, 8, 0, 0));
		}
		assertEquals(numCellsTotal, r1.getAvailable().size());
		
		r1.getAllocationModel().allocateLand(r1);
		assertEquals((int) Math.ceil(numCellsTotal * (1 - PROPORTION)), r1.getAvailable().size());
	}
}
