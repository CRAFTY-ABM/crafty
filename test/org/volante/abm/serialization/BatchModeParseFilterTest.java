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
 * 
 * Created by Sascha Holzhauer on 16 Sep 2014
 */
package org.volante.abm.serialization;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.volante.abm.data.Region;
import org.volante.abm.example.BasicTestsUtils;
import org.volante.abm.example.SimplePotentialAgent;
import org.volante.abm.schedule.RunInfo;

/**
 * @author Sascha Holzhauer
 *
 */
public class BatchModeParseFilterTest extends BasicTestsUtils {

	static final String	XML_FILE	= "xml/BatchModeParseFilterTestingAgent.xml";

	protected RunInfo	rInfo;
	protected Region	region		= new Region();

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		rInfo = new RunInfo();
		rInfo.setCurrentRun(42);
	}

	@Test
	public void test() throws Exception {
		ABMPersister persister = ABMPersister.getInstance();
		SimplePotentialAgent agent = persister.readXML(SimplePotentialAgent.class, XML_FILE);
		agent.initialise(modelData, rInfo, region);
		assertEquals(0.3, agent.getGivingIn(), 0.01);
		assertEquals(0.5, agent.getGivingUp(), 0.01);
	}
}
