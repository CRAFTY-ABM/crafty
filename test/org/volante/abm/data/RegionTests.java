package org.volante.abm.data;

import static org.junit.Assert.*;

import org.junit.Test;
import org.volante.abm.agent.Agent;
import org.volante.abm.example.BasicTestsUtils;

public class RegionTests extends BasicTestsUtils
{

	@Test
	public void testOwnership()
	{
		r1.setOwnership( a1, c11,c12 );
		assertEquals( a1, c11.getOwner() );
		assertEquals( a1, c12.getOwner() );
		assertEquals( Agent.NOT_MANAGED, c13.getOwner() );
		checkSet( r1.agents, a1);
		
		r1.setOwnership( a2,  c12, c13 );
		assertEquals( a1, c11.getOwner() );
		assertEquals( a2, c12.getOwner() );
		assertEquals( a2, c13.getOwner() );
		checkSet( r1.agents, a1, a2 );
		
		r1.setOwnership( a2,  c11 );
		assertEquals( a2, c11.getOwner() );
		assertEquals( a2, c12.getOwner() );
		assertEquals( a2, c13.getOwner() );
		checkSet( r1.agents, a2 );
	}

}
