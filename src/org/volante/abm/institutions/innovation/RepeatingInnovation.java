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
 * Created by Sascha Holzhauer on 2 Dec 2014
 */
package org.volante.abm.institutions.innovation;

import org.volante.abm.institutions.innovation.repeat.InnovationRepComp;




/**
 * @author Sascha Holzhauer
 *
 */
public interface RepeatingInnovation {

	/**
	 * Produces a new repeating innovation with the same lifespan as this innovation, starting at
	 * the tick of calling.
	 * 
	 * @return
	 */
	public RepeatingInnovation getNewInnovation();

	/**
	 * Get the innovation's repetition component.
	 * 
	 * @return
	 */
	public InnovationRepComp getRepetitionComp();
}
