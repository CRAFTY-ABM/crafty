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
package org.volante.abm.institutions;


import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.simpleframework.xml.ElementMap;
import org.volante.abm.agent.PotentialAgent;
import org.volante.abm.data.Capital;
import org.volante.abm.data.Cell;
import org.volante.abm.data.ModelData;
import org.volante.abm.data.Region;
import org.volante.abm.data.Service;
import org.volante.abm.schedule.RunInfo;

import com.moseph.modelutils.fastdata.DoubleMap;
import com.moseph.modelutils.fastdata.UnmodifiableNumberMap;


public class DefaultInstitution extends AbstractInstitution {
	DoubleMap<Service>			subsidies				= null;
	DoubleMap<Capital>			adjustments				= null;
	Map<PotentialAgent, Double>	agentSubsidies			= new HashMap<PotentialAgent, Double>();

	@ElementMap(inline = true, required = false, entry = "subsidy", attribute = true, key = "service")
	Map<String, Double>			serialSubsidies			= new HashMap<String, Double>();
	@ElementMap(inline = true, required = false, entry = "adjustment", attribute = true, key = "capital")
	Map<String, Double>			serialAdjustments		= new HashMap<String, Double>();
	@ElementMap(inline = true, required = false, entry = "agentSubsidy", attribute = true, key = "agent")
	Map<String, Double>			serialAgentSubsidies	= new HashMap<String, Double>();

	@Override
	public void adjustCapitals(Cell c) {
		DoubleMap<Capital> adjusted = modelData.capitalMap();
		c.getEffectiveCapitals().copyInto(adjusted);
		adjustments.addInto(adjusted);
		c.setEffectiveCapitals(adjusted);
	}

	@Override
	public double adjustCompetitiveness(PotentialAgent agent, Cell location,
			UnmodifiableNumberMap<Service> provision, double competitiveness) {
		double result = competitiveness;
		double subsidy = provision.dotProduct(subsidies);
		result += subsidy;
		if (agentSubsidies.containsKey(agent)) {
			result += agentSubsidies.get(agent);
		}
		return result;

	}

	@Override
	public boolean isAllowed(PotentialAgent agent, Cell location) {
		return true;
	}

	public void setAdjustment(UnmodifiableNumberMap<Capital> s) {
		adjustments.copyFrom(s);
	}

	public void setSubsidies(UnmodifiableNumberMap<Service> s) {
		subsidies.copyFrom(s);
	}

	public void setSubsidy(PotentialAgent a, double value) {
		agentSubsidies.put(a, value);
	}

	@Override
	public void initialise(ModelData data, RunInfo info, Region extent) throws Exception {
		super.initialise(data, info, extent);
		extent.setRequiresEffectiveCapitalData();
		extent.setHasCompetitivenessAdjustingInstitution();
		subsidies = data.serviceMap();
		adjustments = data.capitalMap();
		for (Entry<String, Double> e : serialSubsidies.entrySet()) {
			if (data.services.contains(e.getKey())) {
				subsidies.put(data.services.forName(e.getKey()), e.getValue());
			}
		}
		for (Entry<String, Double> e : serialAdjustments.entrySet()) {
			if (data.capitals.contains(e.getKey())) {
				adjustments.put(data.capitals.forName(e.getKey()), e.getValue());
			}
		}
		Map<String, PotentialAgent> agents = new HashMap<String, PotentialAgent>();
		for (PotentialAgent p : extent.getAllPotentialAgents()) {
			agents.put(p.getID(), p);
		}
		for (Entry<String, Double> e : serialAgentSubsidies.entrySet()) {
			if (agents.containsKey(e.getKey())) {
				agentSubsidies.put(agents.get(e.getKey()), e.getValue());
			}
		}

	}

}
