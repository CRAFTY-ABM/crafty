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
 * Created by Sascha Holzhauer on 07.05.2014
 */
package org.volante.abm.serialization;


import org.volante.abm.data.ModelData;
import org.volante.abm.data.Regions;
import org.volante.abm.schedule.RunInfo;


/**
 * Initialisation is there to allow objects to set themselves up. In particular to load serialised
 * data. For objects that are initialised per region there is an alternative interface
 * {@link Initialisable}.
 * 
 * @author Sascha Holzhauer
 * 
 */
public interface GloballyInitialisable {

	/**
	 * @param data
	 *        model data
	 * @param info
	 *        run information
	 * @throws Exception
	 */
	public void initialise(ModelData data, RunInfo info, Regions regions) throws Exception;
}
