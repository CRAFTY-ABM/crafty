package org.volante.abm.example;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.volante.abm.agent.Agent;
import org.volante.abm.data.Cell;
import org.volante.abm.models.AllocationModel;


public class InitiallyRandomAllocationTest extends BasicTestsUtils
{
	
	static final String PROPORTION_ALLOCATION_XML = "xml/InitiallyRandomAllocation.xml";
	static final double	PROPORTION					= 0.3;
	
	@Before
	public void setup() {
		persister = runInfo.getPersister();
		try {
			this.allocation =
					persister.read(AllocationModel.class,
							persister.getFullPath(PROPORTION_ALLOCATION_XML, this.r1.getPeristerContextExtra()));
			this.allocation.initialise(modelData, runInfo, r1);
			r1.setAllocationModel(this.allocation);
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}
	
	@Test
	public void testIntiallyRandomAllocation() throws Exception
	{

		log.info("Test initially random Allocation...");
		log.info(r1.getPotentialAgents());
		log.info(r2.getPotentialAgents());
		assertEquals( potentialAgents, r1.getPotentialAgents());
		allocation = persister.roundTripSerialise( allocation );
	
		assertTrue(r1.getCells().contains(c11));
	
		((InitiallyRandomAllocationModel) r1.getAllocationModel()).allocateLandRandomly(r1);

		int farmers = 0;
		int foresters = 0;
		for (Agent agent : r1.getAgents()) {
			if (agent.getType().getID().equals("farmer"))
				farmers++;
			if (agent.getType().getID().equals("forestry"))
				foresters++;
		}
		assertEquals(farmers, foresters);
	}

	@SuppressWarnings("deprecation")
	@Test
	public void testProportionalAllocation() {
		log.info("Test simple allocation of proportion of available cells...");

		this.runInfo.getSchedule().tick();
		this.runInfo.getSchedule().tick();
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
