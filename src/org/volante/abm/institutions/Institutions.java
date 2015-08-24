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
 */
package org.volante.abm.institutions;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.simpleframework.xml.Root;
import org.volante.abm.agent.PotentialAgent;
import org.volante.abm.data.Cell;
import org.volante.abm.data.ModelData;
import org.volante.abm.data.Region;
import org.volante.abm.data.Service;
import org.volante.abm.schedule.DefaultSchedule;
import org.volante.abm.schedule.PreTickAction;
import org.volante.abm.schedule.RunInfo;

import com.moseph.modelutils.fastdata.UnmodifiableNumberMap;

@Root
public class Institutions implements Institution, PreTickAction {
	Set<Institution> institutions = new HashSet<Institution>();
	Region				region			= null;
	ModelData			data			= null;
	RunInfo				info			= null;
	Logger log = Logger.getLogger(getClass());

	public void addInstitution(Institution i) {
		institutions.add(i);
	}

	@Override
	public boolean isAllowed(PotentialAgent a, Cell c) {
		for (Institution i : institutions) {
			if (!i.isAllowed(a, c)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void adjustCapitals(Cell c) {
		for (Institution i : institutions) {
			i.adjustCapitals(c);
		}
	}

	/**
	 * @see org.volante.abm.institutions.Institution#adjustCompetitiveness(org.volante.abm.agent.PotentialAgent,
	 *      org.volante.abm.data.Cell,
	 *      com.moseph.modelutils.fastdata.UnmodifiableNumberMap, double)
	 */
	@Override
	public double adjustCompetitiveness(PotentialAgent agent, Cell location,
			UnmodifiableNumberMap<Service> provision, double competitiveness) {
		double result = competitiveness;
		for (Institution i : institutions) {
			result = i.adjustCompetitiveness(agent, location,
					provision, result);
		}
		return result;
	}

	@Override
	public void update() {
		for (Institution i : institutions) {
			i.update();
		}
	}

	/**
	 * @see DefaultSchedule#tick()
	 */
	public void updateCapitals() {
		log.info("Adjusting capitals for Region " + region);
		for (Cell c : region.getAllCells()) {
			adjustCapitals(c);
		}
	}

	@Override
	public void initialise(ModelData data, RunInfo info, Region extent)
			throws Exception {
		this.data = data;
		this.info = info;
		this.region = extent;
		for (Institution i : institutions) {
			i.initialise(data, info, extent);
		}
	}

	/**
	 * @see org.volante.abm.schedule.PreTickAction#preTick()
	 */
	@Override
	public void preTick() {
		update();
	}
}
