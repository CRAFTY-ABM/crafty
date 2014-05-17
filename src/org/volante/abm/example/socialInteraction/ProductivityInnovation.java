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
 * Created by Sascha Holzhauer on 06.03.2014
 */
package org.volante.abm.example.socialInteraction;


import org.apache.log4j.Logger;
import org.volante.abm.agent.InnovationAgent;
import org.volante.abm.agent.SocialAgent;
import org.volante.abm.data.Service;
import org.volante.abm.decision.innovations.Innovation;
import org.volante.abm.decision.innovations.bo.InnovationBo;
import org.volante.abm.example.SimpleProductionModel;
import org.volante.abm.models.ProductionModel;

/**
 * @author Sascha Holzhauer
 *
 */
public class ProductivityInnovation extends Innovation {

	/**
	 * Logger
	 */
	static private Logger	logger	= Logger.getLogger(ProductivityInnovation.class);

	Service	service;

	double					effect	= 1.002;

	public ProductivityInnovation(String identifier, Service service, double effect) {
		super(identifier);
		this.service = service;
		this.effect = effect;
	}

	@Override
	public InnovationBo getWaitingBo(SocialAgent agent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void perform(InnovationAgent agent) {
		ProductionModel pModel = agent.getProductionModel();
		if (pModel instanceof SimpleProductionModel) {
			((SimpleProductionModel) pModel)
					.setWeight(
							service,
							((SimpleProductionModel) pModel).getProductionWeights().getDouble(
									service) * effect);
		}
	}

	@Override
	public void unperform(InnovationAgent agent) {
		ProductionModel pModel = agent.getProductionModel();
		if (pModel instanceof SimpleProductionModel) {
			((SimpleProductionModel) pModel)
					.setWeight(
							service,
							((SimpleProductionModel) pModel).getProductionWeights().getDouble(
									service) / effect);
		}
	}
}
