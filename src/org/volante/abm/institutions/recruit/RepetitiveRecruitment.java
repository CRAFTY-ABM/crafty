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
 * Created by Sascha Holzhauer on 5 Dec 2014
 */
package org.volante.abm.institutions.recruit;

import java.util.Collection;

import org.simpleframework.xml.Element;
import org.volante.abm.agent.Agent;
import org.volante.abm.agent.InnovationAgent;
import org.volante.abm.data.ModelData;
import org.volante.abm.data.Region;
import org.volante.abm.schedule.RunInfo;


/**
 * Asks the initialTargetRecruitment for the first set of target recruitment agents and returns that
 * for every subsequent requests.
 * 
 * @author Sascha Holzhauer
 *
 */
public class RepetitiveRecruitment implements InstitutionTargetRecruitment {

	@Element(name = "initialTargetRecruitment", required = false)
	InstitutionTargetRecruitment initialTargetRecruitment;

	Collection<InnovationAgent> recruitedAgents = null;

	/**
	 * @see org.volante.abm.institutions.recruit.InstitutionTargetRecruitment#getRecruitedAgents(java.util.Collection)
	 */
	@Override
	public Collection<InnovationAgent> getRecruitedAgents(Collection<? extends Agent> allAgents) {
		if (this.recruitedAgents == null)
			this.recruitedAgents = this.initialTargetRecruitment.getRecruitedAgents(allAgents);
		return this.recruitedAgents;
	}

	/**
	 * @see org.volante.abm.serialization.Initialisable#initialise(org.volante.abm.data.ModelData,
	 *      org.volante.abm.schedule.RunInfo, org.volante.abm.data.Region)
	 */
	@Override
	public void initialise(ModelData data, RunInfo info, Region extent) throws Exception {
		this.initialTargetRecruitment.initialise(data, info, extent);
	}
}
