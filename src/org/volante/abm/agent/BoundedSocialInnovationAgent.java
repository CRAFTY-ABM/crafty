/**
 * This file is part of
 * 
 * CRAFTY - Competition for Resources between Agent Functional TYpes
 *
 * Copyright (C) 2015 School of GeoScience, University of Edinburgh, Edinburgh, UK
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
 * Created by Sascha Holzhauer on 15 Apr 2015
 */
package org.volante.abm.agent;

import org.apache.log4j.Logger;
import org.volante.abm.data.ModelData;
import org.volante.abm.data.Region;
import org.volante.abm.models.ProductionModel;
import org.volante.abm.param.RandomPa;

/**
 * @author Sascha Holzhauer
 *
 */
public class BoundedSocialInnovationAgent extends DefaultSocialInnovationAgent {

	protected double givingUpProbability = 1.0;

	/**
	 * Logger
	 */
	static private Logger logger = Logger
			.getLogger(BoundedSocialInnovationAgent.class);

	public BoundedSocialInnovationAgent() {
		super();
		numberAgents++;
	}

	/**
	 * @param id
	 *            agent id
	 * @param data
	 *            model data
	 */
	public BoundedSocialInnovationAgent(String id, ModelData data) {
		super(id, data);
		numberAgents++;
	}

	/**
	 * Mainly used for testing purposes
	 * 
	 * @param type
	 *            potential agent
	 * @param id
	 *            agent id
	 * @param data
	 *            model data
	 * @param r
	 *            region
	 * @param prod
	 *            production model
	 * @param givingUp
	 *            giving up threshold
	 * @param givingIn
	 *            giving in threshold
	 * @param givingUpProbability
	 */
	public BoundedSocialInnovationAgent(PotentialAgent type, String id,
			ModelData data, Region r, ProductionModel prod, double givingUp,
			double givingIn, double givingUpProbability) {
		super(type, id, data, r, prod, givingUp, givingIn);
		this.givingUpProbability = givingUpProbability;
		numberAgents++;
	}

	@Override
	public void considerGivingUp() {
		if (currentCompetitiveness < givingUp) {
			if (this.region.getRandom().getURService()
					.nextDouble(RandomPa.RANDOM_SEED_RUN_GIVINGUP.name()) < this.givingUpProbability) {
				giveUp();
			} else {
				// <- LOGGING
				if (logger.isDebugEnabled()) {
					logger.debug(this + "> GivingUp rejected!");
				}
				// LOGGING ->
			}
		}
	}
}
