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


import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementMap;
import org.volante.abm.agent.Agent;
import org.volante.abm.agent.InnovationAgent;
import org.volante.abm.agent.SocialAgent;
import org.volante.abm.data.ModelData;
import org.volante.abm.data.Region;
import org.volante.abm.data.Service;
import org.volante.abm.decision.innovations.Innovation;
import org.volante.abm.decision.innovations.bo.InnovationBo;
import org.volante.abm.example.SimpleProductionModel;
import org.volante.abm.models.ProductionModel;
import org.volante.abm.schedule.RunInfo;

/**
 * @author Sascha Holzhauer
 *
 */
public class ProductivityInnovation extends Innovation {

	/**
	 * Logger
	 */
	static private Logger	logger	= Logger.getLogger(ProductivityInnovation.class);

	/**
	 * Specifies for each AFT the required proportions of adopted among neighbours to adopt itself
	 */
	@ElementMap(entry = "socialPartnerShareAdjustment", key = "aft", attribute = true, inline = true)
	protected Map<String, Double>	socialPartnerShareAdjustment;

	/**
	 * Normalise changes in productivity (subtract the average increase from every agent's
	 * productivity)?
	 */
	@Element(name = "normaliseProductivity", required = false)
	protected Boolean				normaliseProductivity	= false;

	/**
	 * Inrease of productivity in case of adoption.
	 */
	@Element(name = "effectOnProductivity", required = false)
	protected double				effectOnProductivity	= 1.002;

	/**
	 * AFTs that count in the evaluation of social network partners.
	 */
	@Element(required = false)
	protected String				affectiveAFTs			= "all";

	@Element(required = true)
	protected String				affectedServices		= "";


	protected Set<String>			affectiveAFTset;

	protected Set<String>			affectedAFTset;

	protected Set<Service>			affectedServiceSet;


	/**
	 * @param identifier
	 */
	public ProductivityInnovation(@Attribute(name = "id") String identifier) {
		super(identifier);
	}

	/**
	 * @see org.volante.abm.decision.innovations.Innovation#initialise(org.volante.abm.data.ModelData,
	 *      org.volante.abm.schedule.RunInfo, org.volante.abm.data.Region)
	 */
	public void initialise(ModelData data, RunInfo info, Region r) {
		super.initialise(data, info, r);

		this.affectedServiceSet = new HashSet<Service>();
		for (String service : affectedServices.split(",")) {
			service = service.trim();
			this.affectedServiceSet.add(modelData.services.forName(service));
		}

		if (this.normaliseProductivity) {
			for (Service service : this.affectedServiceSet) {
				this.region.registerHelper(this, new NormaliseProductivityRegionHelper(
						this.region, service, this.normaliseProductivity));
			}
		}
	}

	/**
	 * Multiplies the generic adoption factor with AFT specific social partner share adjustment
	 * factor.
	 * 
	 * @see org.volante.abm.decision.innovations.Innovation#getTrialFactor(org.volante.abm.agent.Agent)
	 */
	public double getTrialFactor(Agent agent) {
		if (!socialPartnerShareAdjustment.containsKey(agent.getType().getID())) {
			// <- LOGGING
			logger.warn("No social partner share adjustment factor provided for "
					+ agent.getType().getID()
					+ ". Using 1.0.");
			// LOGGING ->
			return super.getTrialFactor(agent);
		}
		return super.getTrialFactor(agent)
				* socialPartnerShareAdjustment.get(agent.getType().getID());
	}

	@Override
	public InnovationBo getWaitingBo(SocialAgent agent) {
		return null;
	}

	/**
	 * @see org.volante.abm.decision.innovations.Innovation#perform(org.volante.abm.agent.InnovationAgent)
	 */
	@Override
	public void perform(InnovationAgent agent) {
		ProductionModel pModel = agent.getProductionModel();
		if (pModel instanceof SimpleProductionModel) {
			for (Service service : this.affectedServiceSet) {
				((SimpleProductionModel) pModel).setWeight(service,
						((SimpleProductionModel) pModel).getProductionWeights().getDouble(
								service) * effectOnProductivity);

				// <- LOGGING
				if (logger.isDebugEnabled()) {
					logger.debug("New productivity: "
							+ ((SimpleProductionModel) pModel).getProductionWeights().getDouble(
									service) * effectOnProductivity);
				}
				// LOGGING ->
			}
		}
	}

	/**
	 * @see org.volante.abm.decision.innovations.Innovation#unperform(org.volante.abm.agent.InnovationAgent)
	 */
	@Override
	public void unperform(InnovationAgent agent) {
		ProductionModel pModel = agent.getProductionModel();
		if (pModel instanceof SimpleProductionModel) {
			for (Service service : this.affectedServiceSet) {
				((SimpleProductionModel) pModel).setWeight(service,
							((SimpleProductionModel) pModel).getProductionWeights().getDouble(
									service) / effectOnProductivity);

				// <- LOGGING
				if (logger.isDebugEnabled()) {
					logger.debug("Unperform " + this);
				}
				// LOGGING ->
			}
		}		
	}
}
