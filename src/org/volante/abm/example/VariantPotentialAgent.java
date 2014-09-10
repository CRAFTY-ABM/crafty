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
 */
package org.volante.abm.example;


import org.simpleframework.xml.Element;
import org.volante.abm.agent.Agent;
import org.volante.abm.agent.DefaultAgent;
import org.volante.abm.data.Cell;
import org.volante.abm.data.ModelData;
import org.volante.abm.data.Region;
import org.volante.abm.models.ProductionModel;
import org.volante.abm.param.RandomPa;
import org.volante.abm.schedule.RunInfo;

import com.moseph.modelutils.distribution.Distribution;


/**
 * RANU Adapt to RegionalRandom
 * 
 * @author Sascha Holzhauer
 * 
 */
public class VariantPotentialAgent extends SimplePotentialAgent
{
	@Element(required = false)
	Distribution	givingUpDistribution	= null;
	@Element(required = false)
	Distribution	givingInDistribution	= null;
	@Element(required = false)
	Distribution	ageDistribution			= null;

	// These only work with the SimpleProductionModel
	@Element(required = false)
	Distribution	serviceLevelNoise		= null;
	@Element(required = false)
	Distribution	capitalImportanceNoise	= null;

	@Override
	public void initialise(ModelData data, RunInfo info, Region r) throws Exception {
		super.initialise(data, info, r);

		if (givingUpDistribution != null) {
			this.givingUpDistribution.init(r.getRandom().getURService(),
					RandomPa.RANDOM_SEED_INIT_AGENTS.name());
		}
		if (givingInDistribution != null) {
			this.givingInDistribution.init(r.getRandom().getURService(),
					RandomPa.RANDOM_SEED_INIT_AGENTS.name());
		}
	}

	/**
	 * Override the standard agent creation to make agents with individual variation
	 */
	@Override
	public Agent createAgent(Region region, Cell... cells)
	{
		DefaultAgent da = new DefaultAgent(this, id, data, region, productionModel(production,
				region), givingUp(), givingIn());
		if (ageDistribution != null) {
			da.setAge((int) ageDistribution.sample());
		}
		region.setOwnership(da, cells);

		return da;
	}

	public double givingUp() {
		return givingUpDistribution == null ? givingUp : givingUpDistribution.sample();
	}

	public double givingIn() {
		return givingInDistribution == null ? givingIn : givingInDistribution.sample();
	}

	/**
	 * Returns a noisy version of the production model. Uses the serviceLevelNoise distribution to
	 * create variance in the optimal levels of service production, and capitalImportanceNoise to
	 * create variance in the importance of the captials to this production.
	 * 
	 * Only works on SimpleProduction models at the moment.
	 * 
	 * @param production
	 * @param r
	 * @return
	 */
	public ProductionModel productionModel(final ProductionModel production, final Region r)
	{
		if (!(production instanceof SimpleProductionModel)) {
			return production;
		}

		if (this.serviceLevelNoise != null) {
			this.serviceLevelNoise.init(r.getRandom().getURService(),
					RandomPa.RANDOM_SEED_INIT_AGENTS.name());
		}

		if (this.capitalImportanceNoise != null) {
			this.capitalImportanceNoise.init(r.getRandom().getURService(),
					RandomPa.RANDOM_SEED_INIT_AGENTS.name());
		}

		return ((SimpleProductionModel) production).copyWithNoise(data, serviceLevelNoise,
				capitalImportanceNoise);
	}
}
