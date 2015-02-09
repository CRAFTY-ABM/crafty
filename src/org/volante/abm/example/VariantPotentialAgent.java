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


import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.log4j.Logger;
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
	/**
	 * Logger
	 */
	static private Logger	logger					= Logger.getLogger(VariantPotentialAgent.class);

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

	/**
	 * 
	 */
	public VariantPotentialAgent() {
		super();
	}
	
	/**
	 * @param id
	 * @param data
	 * @param production
	 * @param givingUp
	 * @param givingIn
	 */
	public VariantPotentialAgent(String id, ModelData data, ProductionModel production,
			double givingUp, double givingIn) {
		super(id, data, production, givingUp, givingIn);
	}

	@Override
	public void initialise(ModelData data, RunInfo info, Region r) throws Exception {
		super.initialise(data, info, r);

		if (givingUpDistribution != null) {
			this.givingUpDistribution.init(r.getRandom().getURService(),
					RandomPa.RANDOM_SEED_INIT_AGENTS.name());
			// make sure that potential agent's GU value correspond to the normal distribution's
			// mean:
			if (this.givingUpDistribution instanceof NormalDistribution) {
				if (this.givingUp != ((NormalDistribution) this.givingUpDistribution).getMean()) {
					// <- LOGGING
					logger.warn("Distirbution mean did not correspond to potential agent's value for givingUp threshold: "
							+ "Set givingUp treshold to distribution mean!");
					// LOGGING ->
					this.givingUp = ((NormalDistribution) this.givingUpDistribution).getMean();
				}
			}
		}
		if (givingInDistribution != null) {
			this.givingInDistribution.init(r.getRandom().getURService(),
					RandomPa.RANDOM_SEED_INIT_AGENTS.name());
			// make sure that potential agent's GI value correspond to the normal distribution's
			// mean:
			if (this.givingInDistribution instanceof NormalDistribution) {
				if (this.givingIn != ((NormalDistribution) this.givingInDistribution).getMean()) {
					// <- LOGGING
					logger.warn("Distirbution mean did not correspond to potential agent's value for givingIn threshold: "
							+ "Set givingIn treshold to distribution mean!");
					// LOGGING ->
					this.givingIn = ((NormalDistribution) this.givingInDistribution).getMean();
				}
			}
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
	 * Returns a noisy version of the production model. Uses the
	 * serviceLevelNoise distribution to create variance in the optimal levels
	 * of service production, and capitalImportanceNoise to create variance in
	 * the importance of the capitals to this production.
	 * 
	 * Only works on SimpleProduction models at the moment.
	 * 
	 * @param production
	 * @param r
	 * @return production model
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
