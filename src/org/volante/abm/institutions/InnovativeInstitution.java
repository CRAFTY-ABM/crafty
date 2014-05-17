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

import org.apache.log4j.Logger;
import org.simpleframework.xml.Element;
import org.volante.abm.agent.Agent;
import org.volante.abm.agent.InnovationAgent;
import org.volante.abm.data.ModelData;
import org.volante.abm.data.Region;
import org.volante.abm.decision.innovations.Innovation;
import org.volante.abm.example.socialInteraction.ProductivityInnovation;
import org.volante.abm.param.RandomPa;
import org.volante.abm.schedule.RunInfo;

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

	Innovation				innovation = null;
	
	@Override
	public void initialise(ModelData data, RunInfo info, Region extent) throws Exception {
		super.initialise(data, info, extent);
		innovation = new ProductivityInnovation("ProductivityService3",
				data.services.forName("Service3"), innovationEffect);
		innovation.initialise(extent);
	}

	@Element(required = false)
	protected int innovationReleaseTick = 0;

	/**
	 * 
	 */
	@Element(required = false)
	protected double	initialAwarenessProb	= 0.05;

	@Element(required = false)
	protected int		numInitialAdopters		= 0;

	@Element(required = false)
	protected double	innovationEffect		= 1.002;

	/**
	 * @see org.volante.abm.institutions.AbstractInstitution#update()
	 */
	@Override
	public void update() {
		// <- LOGGING
		logger.info("Update " + this);
		// LOGGING ->

		RandomEngine rEngine = region.getRandom().getURService()
				.getGenerator(RandomPa.RANDOM_SEED_RUN.name());

		if (info.getSchedule().getCurrentTick() == this.innovationReleaseTick) {
			// <- LOGGING
			logger.info("Make agents aware...");
			// LOGGING ->

			if (numInitialAdopters > 0) {
				ArrayList<InnovationAgent> innovationAgents = new ArrayList<InnovationAgent>();
				for (Agent agent : this.region.getAllAgents()) {
					// TODO parameterize / make generic
					if (agent instanceof InnovationAgent && agent.getID().equals("AT3")) {
						innovationAgents.add((InnovationAgent) agent);
					}
				}
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
				for (Agent agent : this.region.getAllAgents()) {
					if (agent instanceof InnovationAgent
							&& rEngine.nextDouble() <= this.initialAwarenessProb) {
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
		return "Innovative Insitution";
	}
}
