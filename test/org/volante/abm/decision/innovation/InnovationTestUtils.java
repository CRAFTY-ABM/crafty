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
 * Created by Sascha Holzhauer on 3 Dec 2014
 */
package org.volante.abm.decision.innovation;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.volante.abm.agent.Agent;
import org.volante.abm.agent.InnovationAgent;
import org.volante.abm.agent.PotentialAgent;
import org.volante.abm.data.Cell;
import org.volante.abm.data.Region;
import org.volante.abm.data.Service;
import org.volante.abm.example.BasicTestsUtils;
import org.volante.abm.example.SocialVariantPotentialAgent;
import org.volante.abm.institutions.Institution;
import org.volante.abm.institutions.Institutions;
import org.volante.abm.institutions.innovation.Innovation;
import org.volante.abm.institutions.innovation.repeat.CsvProductivityInnovationRepComp;
import org.volante.abm.institutions.innovation.status.InnovationState;
import org.volante.abm.models.utils.ProductionWeightReporter;


/**
 * @author Sascha Holzhauer
 *
 */
public class InnovationTestUtils extends BasicTestsUtils {

	/**
	 * Enables setting relative to base during runtime.
	 * 
	 * @author Sascha Holzhauer
	 *
	 */
	public static class CsvProductivityInnovationRepTestComp extends
			CsvProductivityInnovationRepComp {

		public void setRelativeToPreviousTick(boolean relative) {
			this.considerFactorsRelativeToPreviousTick = relative;
		}
	}

	public static SocialVariantPotentialAgent innovativeForestry = new SocialVariantPotentialAgent(
			"Forestry",
			BasicTestsUtils.modelData,
			BasicTestsUtils.forestryProduction.copyWithNoise(modelData, null,
					null),
			BasicTestsUtils.forestryGivingUp,
			BasicTestsUtils.forestryGivingIn);

	public static SocialVariantPotentialAgent innovativeFarming = new SocialVariantPotentialAgent(
			"Farming",
			BasicTestsUtils.modelData,
			BasicTestsUtils.farmingProduction.copyWithNoise(modelData, null,
					null),
			BasicTestsUtils.farmingGivingUp,
			BasicTestsUtils.farmingGivingIn);

	public static Set<PotentialAgent> potentialAgents = new HashSet<PotentialAgent>(
			Arrays.asList(new PotentialAgent[] {
					innovativeForestry,
					innovativeFarming }));

	public InnovationAgent innoFarmingA = (InnovationAgent) innovativeFarming.createAgent(r1);
	public InnovationAgent innoForesterA = (InnovationAgent) innovativeForestry.createAgent(r1);

	protected static void checkInnovationState(Innovation innovation,
			Collection<InnovationAgent> agents,
			InnovationState status) {
		for (InnovationAgent agent : agents) {
			assertEquals("Check innovation status", status, agent.getState(innovation));
		}
	}

	protected void addInnovationAgentsToRegion1(int numberOfAgents, PotentialAgent pagent) {
		Cell[] cells = this.r1cells.toArray(new Cell[1]);

		if (numberOfAgents > cells.length) {
			throw new IllegalStateException("Only " + cells.length + " cells available, but " +
					numberOfAgents + " requested!");
		}
		for (int i = 0; i < numberOfAgents; i++) {
			this.r1.setOwnership(pagent.createAgent(r1, cells[i]), cells[i]);
		}
	}

	/**
	 * Register given institution at given region and create
	 * {@link Institutions} if not present at region.
	 * 
	 * @param institution
	 * @param region
	 */
	protected void registerInstitution(Institution institution, Region region) {
		Institutions institutions = region.getInstitutions();
		if (institutions == null) {
			institutions = new Institutions();
			try {
				institutions.initialise(modelData, runInfo, r1);
			} catch (Exception exception) {
				exception.printStackTrace();
			}
			runInfo.getSchedule().register(institutions);
		}
		institutions.addInstitution(institution);
		region.setInstitutions(institutions);
	}

	/**
	 * Checks that the agent's productivity for the given service is equals to
	 * the given expected one.
	 * 
	 * @param agent
	 * @param expectedProductivity
	 * @param service
	 */
	public void checkCapital(Agent agent, double expectedProductivity,
			Service service) {
		double actualProductivity;
		if (agent.getProductionModel() instanceof ProductionWeightReporter) {
			actualProductivity = ((ProductionWeightReporter) agent
					.getProductionModel()).getProductionWeights().getDouble(
					service);

			assertEquals("Check " + agent.getID() + "s productivity...",
					expectedProductivity, actualProductivity, 0.0001);
		} else {
			fail("Could not test productivity because agent's production model is not a ProductionWeightReporter!");
		}
	}

	public void checkCapitalChange(Agent agent, PotentialAgent pagent,
			double expectedProductivity, Service service) {
		if (pagent.getProduction() instanceof ProductionWeightReporter) {
			checkCapital(agent, expectedProductivity, service);
		} else {
			fail("Could not test productivity because potential agent's production model is not a ProductionWeightReporter!");
		}

	}
}
