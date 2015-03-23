/**
 * This file is part of
 * 
 * CRAFTY - Competition for Resources between Agent Functional TYpes
 *
 * Copyright (C) 2015 School of GeoScience, University of Edinburgh, Edinburgh, UK
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
 * Created by Sascha Holzhauer on 6 Feb 2015
 */
package org.volante.abm.testutils;

import org.volante.abm.data.ModelData;
import org.volante.abm.data.RegionSet;
import org.volante.abm.models.WorldSynchronisationModel;
import org.volante.abm.schedule.RunInfo;

/**
 * For testing purposes to assign a {@link WorldSynchronisationModel} that does
 * nothing.
 * 
 * @author Sascha Holzhauer
 * 
 */
public class NullWorldSynchronisationModel implements WorldSynchronisationModel {

	/**
	 * @see org.volante.abm.models.WorldSynchronisationModel#initialise(org.volante.abm.data.ModelData, org.volante.abm.schedule.RunInfo)
	 */
	@Override
	public void initialise(ModelData data, RunInfo info) {
		// nothing to do
	}

	/**
	 * @see org.volante.abm.models.WorldSynchronisationModel#synchronizeNumOfCells(org.volante.abm.data.RegionSet)
	 */
	@Override
	public void synchronizeNumOfCells(RegionSet regions) {
		// nothing to do
	}

	/**
	 * @see org.volante.abm.models.WorldSynchronisationModel#synchronizeDemand(org.volante.abm.data.RegionSet)
	 */
	@Override
	public void synchronizeDemand(RegionSet regions) {
		// nothing to do
	}

	/**
	 * @see org.volante.abm.models.WorldSynchronisationModel#synchronizeSupply(org.volante.abm.data.RegionSet)
	 */
	@Override
	public void synchronizeSupply(RegionSet regions) {
		// nothing to do
	}

}
