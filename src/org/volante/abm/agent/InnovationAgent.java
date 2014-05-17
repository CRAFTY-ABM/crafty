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
package org.volante.abm.agent;


import org.volante.abm.decision.innovations.Innovation;
import org.volante.abm.decision.innovations.InnovationState;
import org.volante.abm.decision.innovations.InnovationStates;
import org.volante.abm.models.ProductionModel;

/**
 * @author Sascha Holzhauer
 *
 */
public interface InnovationAgent extends Agent {

	/**
	 * @param innovation
	 * @return the given innovation's current state
	 */
	public InnovationState getState(Innovation innovation);

	/**
	 * Make this agent aware of the given innovation, i.e. set the innovation status to
	 * {@link InnovationStates#AWARE}.
	 * 
	 * @param innovation
	 */
	public void makeAware(Innovation innovation);

	/**
	 * Gives the given innovation a trial. Sets the innovation's state to
	 * {@link InnovationStates#TRIAL}.
	 * 
	 * @param innovation
	 */
	public void makeTrial(Innovation innovation);

	/**
	 * TODO make more generic
	 */
	public void decideAdoption();

	/**
	 * 
	 * @return this agents production model
	 */
	public ProductionModel getProductionModel();
}
