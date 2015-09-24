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
 * Created by Sascha Holzhauer on 23 Sep 2015
 */
package org.volante.abm.output;


import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.volante.abm.agent.Agent;
import org.volante.abm.data.Cell;
import org.volante.abm.data.CleanupRegionHelper;
import org.volante.abm.data.Region;


/**
 * Applied by {@link CellTable}.
 * 
 * @author Sascha Holzhauer
 * 
 */
public class PreAllocationStorageCleanupRegionHelper implements CleanupRegionHelper {

	public class PreAllocData {
		public PreAllocData(int agentID, double competitiveness, double guThreshold) {
			this.agentId = agentID;
			this.competitiveness = competitiveness;
			this.guThreshold = guThreshold;
		}

		protected int		agentId;
		protected double	competitiveness;
		protected double	guThreshold;
	}

	protected Map<Cell, PreAllocData>	preAllocDataMap	= new HashMap<>();

	@Override
	public void cleanUp(Region region, Set<Agent> agentsToRemove) {
		for (Cell c : region.getAllCells()) {
			if (!c.getOwner().equals(Agent.NOT_MANAGED)){
				preAllocDataMap.put(c, new PreAllocData(c.getOwner().getType().getSerialID(), 
						c.getOwner().getCompetitiveness(), c.getOwner().getGivingUp()));
			}
		}
		for (Agent a : agentsToRemove) {
			for (Cell c : a.getCells()) {
				preAllocDataMap.put(c, new PreAllocData(a.getType().getSerialID(),
						a.getCompetitiveness(), a.getGivingUp()));
			}
		}
	}

	public PreAllocData getPreAllocData(Cell c) {
		return preAllocDataMap.get(c);
	}

	public void clear() {
		preAllocDataMap.clear();
	}
}
