package org.volante.abm.agent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.volante.abm.data.Region;
import org.volante.abm.example.BasicTestsUtils;
import org.volante.abm.schedule.Schedule;

public class DefaultAgentTest extends BasicTestsUtils
{

	/**
	 * Logger
	 */
	static private Logger	logger	= Logger.getLogger(DefaultAgentTest.class);

	DefaultAgent farmer = (DefaultAgent) farming.createAgent( r1, c11 );

	@Before
	public void setupAgent()
	{
		farmer = (DefaultAgent) farming.createAgent( r1, c11 );
	}

	@Test
	public void testProduction()
	{
		demandR1.setResidual( c11, services(1,1,1,1) );
		c11.setBaseCapitals( cellCapitalsA );
		farmer.updateSupply();
		assertEqualMaps( "Extensive farming production is correct", extensiveFarmingOnCA, farmer.supply( c11 ));
		farmer.updateCompetitiveness();
		assertEquals( extensiveFarmingOnCA.getTotal(), farmer.getCompetitiveness(), 0.0001 );
		c11.setBaseCapitals( cellCapitalsB );
		farmer.updateSupply();
		assertEqualMaps( "Extensive farming production is correct", extensiveFarmingOnCB, farmer.supply( c11 ));
		farmer.updateCompetitiveness();
		assertEquals( extensiveFarmingOnCB.getTotal(), farmer.getCompetitiveness(), 0.0001 );
		demandR1.setResidual( c11, services(2,2,2,2) );
		farmer.updateSupply();
		assertEqualMaps( "Extensive farming production is correct", extensiveFarmingOnCB, farmer.supply( c11 ));
		farmer.updateCompetitiveness();
		assertEquals( 2*extensiveFarmingOnCB.getTotal(), farmer.getCompetitiveness(), 0.0001 );
	}
	
	@Test
	public void testGivingUp()
	{
		demandR1.setResidual( c11, services(1,1,1,1) );
		c11.setBaseCapitals( cellCapitalsA );
		//Set the giving up threshold to a bit less than production
		farmer.setGivingUp( extensiveFarmingOnCA.getTotal() - 0.01 ); 
		farmer.updateSupply();
		farmer.updateCompetitiveness();
		farmer.considerGivingUp();
		//Should still be there
		assertTrue( r1.getAgents().contains( farmer ));
		
		//Set the giving up threshold to a bit more than production
		farmer.setGivingUp( extensiveFarmingOnCA.getTotal() + 0.01 ); //Set the giving up threshold to a bit less than production
		farmer.updateSupply();
		farmer.updateCompetitiveness();
		farmer.considerGivingUp();
		r1.cleanupAgents();
		//And now we should have given up
		assertFalse( r1.getAgents().contains( farmer ));
	}
	
	@Test
	public void testAging()
	{
		farmer.setAge( 20 );
		assertEquals( 20, farmer.getAge() );
		farmer.tickStartUpdate();
		assertEquals( 21, farmer.getAge() );
	}
	
	@Test
	public void testRealAging() throws Exception
	{
		Region r = setupBasicWorld( c11 );
		DefaultAgent farmer = (DefaultAgent) farming.createAgent( r, c11 );
		r.setOwnership( farmer, c11 );
		farmer.setGivingUp(-5); //Make sure he doesn't give up!
		farmer.setAge( 20 );
		for (Agent a : r.getAllAgents()) {
			logger.info("Agent: " + a );
		}
		
		Schedule s = runInfo.getSchedule();
		assertEquals( 20, farmer.getAge() );
		s.tick();
		assertEquals( 21, farmer.getAge() );
		s.tick();
		assertEquals( 22, farmer.getAge() );
		
		DefaultAgent f2 = (DefaultAgent) farming.createAgent( r1, c11 );
		f2.setGivingUp(-5);
		r.setOwnership( f2, c11 );
		f2.setAge( 10 );
		assertEquals( 10, f2.getAge() );
		s.tick();
		assertEquals( 11, f2.getAge() );
	}

}
