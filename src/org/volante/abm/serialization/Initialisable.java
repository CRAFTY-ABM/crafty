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
 */
package org.volante.abm.serialization;


import org.volante.abm.data.ModelData;
import org.volante.abm.data.Region;
import org.volante.abm.schedule.RunInfo;


/**
 * Initialisation is there to allow objects to set themselves up. In particular to load serialised
 * data.
 * 
 * The final regions parameter is to allow regional initialisation where appropriate. It should be
 * able to be null if appropriate
 * 
 * @author dmrust
 * 
 */
public interface Initialisable {
	public void initialise(ModelData data, RunInfo info, Region extent) throws Exception;
}
