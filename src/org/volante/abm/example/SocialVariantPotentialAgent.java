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
 * Created by Sascha Holzhauer on 29 Sep 2014
 */
package org.volante.abm.example;


import org.volante.abm.agent.Agent;
import org.volante.abm.agent.DefaultAgent;
import org.volante.abm.agent.DefaultSocialInnovationAgent;
import org.volante.abm.data.Cell;
import org.volante.abm.data.ModelData;
import org.volante.abm.data.Region;
import org.volante.abm.models.ProductionModel;


/**
 * @author Sascha Holzhauer
 *
 */
public class SocialVariantPotentialAgent extends VariantPotentialAgent {

	/**
	 * @param id
	 * @param data
	 * @param production
	 * @param givingUp
	 * @param givingIn
	 */
	public SocialVariantPotentialAgent(String id, ModelData data, ProductionModel production,
			double givingUp, double givingIn) {
		super(id, data, production, givingUp, givingIn);
	}

	public Agent createAgent(Region region, Cell... cells) {
		return this.createAgent(region, this.id, cells);
		
	}

	public Agent createAgent(Region region, String id, Cell... cells)
	{
		DefaultAgent da = new DefaultSocialInnovationAgent(this, id, data, region, productionModel(
				production,
				region), givingUp(), givingIn());
		if (ageDistribution != null) {
			da.setAge((int) ageDistribution.sample());
		}
		region.setOwnership(da, cells);

		return da;
	}
}
