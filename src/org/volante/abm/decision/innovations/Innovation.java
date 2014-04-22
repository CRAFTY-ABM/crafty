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


import org.simpleframework.xml.Attribute;
import org.volante.abm.agent.InnovationAgent;
import org.volante.abm.agent.SocialAgent;
import org.volante.abm.data.Region;
import org.volante.abm.decision.innovations.bo.InnovationBo;

/**
 * @author Sascha Holzhauer
 *
 */
public abstract class Innovation {

	@Attribute(name = "id")
	protected String	identifier;


	public Innovation(@Attribute(name = "id") String identifier) {
		this.identifier = identifier;
	}

	public abstract InnovationBo getWaitingBo(SocialAgent agent);

	public abstract void perform(InnovationAgent agent);

	public abstract void unperform(InnovationAgent agent);

	public void initialise(Region r) {
		r.getInnovationRegistry().registerInnovation(this, identifier);
	}
}
