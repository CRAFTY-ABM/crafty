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
 * Created by Sascha Holzhauer on 05.02.2014
 */
package org.volante.abm.institutions;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.simpleframework.xml.Element;
import org.volante.abm.agent.Agent;
import org.volante.abm.agent.InnovationAgent;
import org.volante.abm.data.ModelData;
import org.volante.abm.data.Region;
import org.volante.abm.decision.innovations.Innovation;
import org.volante.abm.param.RandomPa;
import org.volante.abm.schedule.RunInfo;
import org.volante.abm.serialization.BatchRunParser;

import cern.jet.random.engine.RandomEngine;

import com.moseph.modelutils.Utilities;


/**
 * @author Sascha Holzhauer
 *
 */
public class InnovativeInstitution extends AbstractInstitution {

	/**
	 * Logger
	 */
	static private Logger logger = Logger
			.getLogger(InnovativeInstitution.class);

	/**
	 * Innovation to release
	 */
	@Element(required = true)
	protected Innovation				innovation = null;

	/**
	 * Tick of release
	 */
	@Element(required = false)
	protected int innovationReleaseTick = 0;

	/**
	 * Probability of an agent to become initially aware. This is alternative to numInitialAdopters!
	 */
	@Element(required = false)
	protected String		initialAwarenessProb	= "0.05";

	/**
	 * Number of agents that become initially aware. This is alternative to numInitialAdopters!
	 */
	@Element(required = false)
	protected int		numInitialAdopters		= 0;

	/**
	 * Comma-separated list of AFT IDs that are allowed to adopt.
	 */
	@Element(required = false)
	protected String	affectedAFTs			= "all";

	protected Set<String>	affectedAftSet			= null;

	/**
	 * @see org.volante.abm.institutions.AbstractInstitution#initialise(org.volante.abm.data.ModelData, org.volante.abm.schedule.RunInfo, org.volante.abm.data.Region)
	 */
	@Override
	public void initialise(ModelData data, RunInfo info, Region extent) throws Exception {
		// <- LOGGING
		logger.info("Initialise " + this);
		// LOGGING ->

		super.initialise(data, info, extent);

		affectedAftSet = new HashSet<String>();

		for (String aft : affectedAFTs.split(",")) {
			aft = aft.trim();
			affectedAftSet.add(aft);
		}

		innovation.initialise(data, info, extent);
	}

	/**
	 * @see org.volante.abm.institutions.AbstractInstitution#update()
	 */
	@Override
	public void update() {
		// <- LOGGING
		logger.info("Update " + this);
		// LOGGING ->

		if (info.getSchedule().getCurrentTick() == this.innovationReleaseTick) {
			// <- LOGGING
			logger.info("Make agents aware...");
			// LOGGING ->

			ArrayList<InnovationAgent> innovationAgents = new ArrayList<InnovationAgent>();

			if (affectedAFTs.equals("all")) {
				for (Agent agent : this.region.getAllAgents()) {
					if (agent instanceof InnovationAgent) {
						innovationAgents.add((InnovationAgent) agent);
					}
				}
			} else {
				for (Agent agent : this.region.getAllAgents()) {
					if (agent instanceof InnovationAgent && affectedAftSet.contains(agent.getID())) {
						innovationAgents.add((InnovationAgent) agent);
					}
				}

				if (innovationAgents.size() == 0) {
					logger.warn("List of innovative agents is empty - no agents can be affected (affectedAFTs = " + affectedAFTs + ")!");
				}
			}

			if (numInitialAdopters > 0) {
				for (int i = 0; i < numInitialAdopters; i++) {
					if (innovationAgents.size() == 0) {
						logger.warn("Not enough agents to make aware about innovation "
								+ innovation + "!");
					}
					int index = Utilities.nextIntFromTo(0, innovationAgents.size() - 1,
							region.getRandom().getURService(), RandomPa.RANDOM_SEED_RUN.name());
					innovationAgents.get(index).makeAware(innovation);
					innovationAgents.get(index).makeTrial(innovation);
					innovationAgents.remove(index);
				}
			} else {
				RandomEngine rEngine = region.getRandom().getURService()
						.getGenerator(RandomPa.RANDOM_SEED_RUN.name());

				for (Agent agent : innovationAgents) {
					if (agent instanceof InnovationAgent
							&& rEngine.nextDouble() <= BatchRunParser.parseDouble(
									this.initialAwarenessProb, info)) {
						((InnovationAgent) agent).makeAware(innovation);
						((InnovationAgent) agent).makeTrial(innovation);
					}
				}
			}
		}
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Innovative Institution";
	}
}
