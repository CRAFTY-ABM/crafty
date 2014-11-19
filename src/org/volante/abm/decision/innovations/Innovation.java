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
 * Created by Sascha Holzhauer on 12.02.2014
 */
package org.volante.abm.decision.innovations;


import java.util.LinkedHashSet;
import java.util.Set;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.volante.abm.agent.Agent;
import org.volante.abm.agent.InnovationAgent;
import org.volante.abm.agent.SocialAgent;
import org.volante.abm.data.ModelData;
import org.volante.abm.data.Region;
import org.volante.abm.decision.innovations.bo.InnovationBo;
import org.volante.abm.schedule.RunInfo;
import org.volante.abm.serialization.Initialisable;


/**
 * Subclasses should call {@link super#initialise(ModelData, RunInfo, Region)} in order to register
 * this innovation at the {@link InnovationRegistry}.
 * 
 * @author Sascha Holzhauer
 */
public abstract class Innovation implements Initialisable{

	/**
	 * Identifier must be given since every innovation is registered at the
	 * innovation registry with its identifier during initialisation.
	 */
	@Attribute(name = "id", required = true)
	protected String	identifier;

	/**
	 * Factor in the decision of trial. Values > 1 cause the trial to be likelier, values < 1 cause
	 * to adoption to be less likely. Default is 1.0
	 */
	@Element(name = "trialFactor", required = false)
	protected double		trialFactor		= 1.0;

	/**
	 * Factor in the decision of adoption. Values > 1 cause the adoption to be likelier, values < 1
	 * cause to adoption to be less likely. Default is 1.0
	 */
	@Element(name = "adoptionFactor", required = false)
	protected double	adoptionFactor	= 1.0;

	/**
	 * Comma-separated list of AFT IDs that are allowed to adopt.
	 */
	@Element(required = false)
	protected String		affectedAFTs	= "all";

	protected Set<String>	affectedAftSet	= null;

	protected Region	region;
	protected RunInfo	rInfo;
	protected ModelData	modelData;

	/**
	 * Factor in the decision of trial. Values > 1 cause the trial to be likelier, values < 1 cause
	 * to adoption to be less likely.
	 * 
	 * @return
	 */
	public double getTrialFactor(Agent agent) {
		return trialFactor;
	}

	/**
	 * Factor in the decision of adoption. Values > 1 cause the adoption to be likelier, values < 1
	 * cause to adoption to be less likely.
	 * 
	 * @return
	 */
	public double getAdoptionFactor(Agent agent) {
		return adoptionFactor;
	}

	public Innovation(@Attribute(name = "id") String identifier) {
		this.identifier = identifier;
	}

	public abstract InnovationBo getWaitingBo(SocialAgent agent);

	/**
	 * Let this innovation take effect for the given agent.
	 * 
	 * @param agent
	 */
	public abstract void perform(InnovationAgent agent);

	/**
	 * Undo the effect of this innovation for the given agent.
	 * 
	 * @param agent
	 */
	public abstract void unperform(InnovationAgent agent);

	/**
	 * Assign model data, run info, and region. Register this innovation at the region's
	 * {@link InnovationRegistry}.
	 * 
	 * 
	 * @see org.volante.abm.serialization.Initialisable#initialise(org.volante.abm.data.ModelData,
	 *      org.volante.abm.schedule.RunInfo, org.volante.abm.data.Region)
	 */
	public void initialise(ModelData data, RunInfo info, Region r) {
		this.modelData = data;
		this.rInfo = info;
		this.region = r;

		affectedAftSet = new LinkedHashSet<String>();
		for (String aft : affectedAFTs.split(",")) {
			aft = aft.trim();
			affectedAftSet.add(aft);
		}

		r.getInnovationRegistry().registerInnovation(this, identifier);
	}

	/**
	 * @return
	 */
	public Set<String> getAffectedAFTs() {
		return affectedAftSet;
	}
}
