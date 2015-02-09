/**
 * This file is part of
 * 
 * CRAFTY - Competition for Resources between Agent Functional TYpes
 *
 * Copyright (C) 2014 School of GeoScience, University of Edinburgh, Edinburgh, UK
 * 
 * CRAFTY is free software: You can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software 
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *  
 * CRAFTY is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * School of Geoscience, University of Edinburgh, Edinburgh, UK
 */
package org.volante.abm.example;


import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.volante.abm.agent.Agent;
import org.volante.abm.agent.DefaultAgent;


public class SimplePotentialAgentTest extends BasicTestsUtils {
	SimpleProductionModel	p1	= new SimpleProductionModel();

	@Test
	public void test() {
		SimplePotentialAgent p = new SimplePotentialAgent("TestAgent", modelData, p1, 5, 3);
		DefaultAgent ag = (DefaultAgent) p.createAgent(r1, c11, c12);
		assertEquals(p1, ag.getProductionFunction());
		assertEquals("TestAgent", ag.getID());
		assertEquals(5, ag.getGivingUp(), 0.0000001);
		assertEquals(3, ag.getGivingIn(), 0.0000001);
		assertEquals(ag, c11.getOwner());
		assertEquals(ag, c12.getOwner());
		assertEquals(Agent.NOT_MANAGED, c13.getOwner());
		checkSet("Ownership of new agent", ag.getCells(), c11, c12);
	}

	@Test
	public void testDeserealisation() throws Exception {
		SimplePotentialAgent p = runInfo.getPersister().readXML(SimplePotentialAgent.class,
				"xml/LowIntensityArableAgent.xml");
		p.initialise(modelData, runInfo, null);
		testLowIntensityArableAgent(p);
	}

	public static void testLowIntensityArableAgent(SimplePotentialAgent p) {
		SimpleProductionTest.testLowIntensityArableProduction((SimpleProductionModel) p.production);
		assertEquals(0.5, p.givingUp, 0.0001);
		assertEquals(1, p.givingIn, 0.0001);
		assertEquals("LowIntensityArable", p.id);
		assertEquals(1, p.serialID);
	}

	public static void testHighIntensityArableAgent(SimplePotentialAgent p) {
		SimpleProductionTest
				.testHighIntensityArableProduction((SimpleProductionModel) p.production);
		assertEquals(0.5, p.givingUp, 0.0001);
		assertEquals(1, p.givingIn, 0.0001);
		assertEquals("HighIntensityArable", p.id);
		assertEquals(2, p.serialID);
	}

	public static void testCommercialForestryAgent(SimplePotentialAgent p) {
		SimpleProductionTest.testCommercialForestryProduction((SimpleProductionModel) p.production);
		assertEquals(0.1, p.givingUp, 0.0001);
		assertEquals(3, p.givingIn, 0.0001);
		assertEquals("CommercialForestry", p.id);
		assertEquals(3, p.serialID);
	}

}
