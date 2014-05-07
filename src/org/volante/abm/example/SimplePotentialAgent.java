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
package org.volante.abm.example;


import org.apache.log4j.Logger;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.volante.abm.agent.Agent;
import org.volante.abm.agent.DefaultAgent;
import org.volante.abm.agent.PotentialAgent;
import org.volante.abm.data.Cell;
import org.volante.abm.data.ModelData;
import org.volante.abm.data.Region;
import org.volante.abm.data.Service;
import org.volante.abm.models.ProductionModel;
import org.volante.abm.schedule.RunInfo;
import org.volante.abm.serialization.Initialisable;

import com.moseph.modelutils.fastdata.DoubleMap;


public class SimplePotentialAgent implements PotentialAgent, Initialisable {
	@Element
	protected ProductionModel	production	= new SimpleProductionModel();
	@Attribute
	protected double			givingUp	= -Double.MAX_VALUE;
	@Attribute
	protected double			givingIn	= -Double.MAX_VALUE;
	@Attribute
	protected String			id			= "PotentialAgent";
	@Attribute
	protected int				serialID	= UNKNOWN_SERIAL;
	protected ModelData			data		= null;
	protected RunInfo			info		= null;

	protected Logger			log			= Logger.getLogger(getClass());

	public SimplePotentialAgent() {
	}

	public SimplePotentialAgent(String id, ModelData data, ProductionModel production,
			double givingUp, double givingIn) {
		this.id = id;
		this.production = production;
		this.givingUp = givingUp;
		this.givingIn = givingIn;
		this.data = data;
	}

	@Override
	public DoubleMap<Service> getPotentialSupply(Cell cell) {
		DoubleMap<Service> map = data.serviceMap();
		production.production(cell, map);
		return map;
	}

	@Override
	public Agent createAgent(Region region, Cell... cells) {
		DefaultAgent da = new DefaultAgent(this, id, data, region,
				production, givingUp, givingIn);
		region.setOwnership(da, cells);
		return da;
	}

	@Override
	public String getID() {
		return id;
	}

	@Override
	public int getSerialID() {
		return serialID;
	}

	@Override
	public double getGivingUp() {
		return givingUp;
	}

	@Override
	public double getGivingIn() {
		return givingIn;
	}

	@Override
	public void initialise(ModelData data, RunInfo info, Region r) throws Exception {
		this.data = data;
		this.info = info;
		production.initialise(data, info, r);
		log.debug("Agent initialised: " + getID());
		log.trace("Production: \n" + production);
	}

	@Override
	public ProductionModel getProduction() {
		return production;
	}

	@Override
	public String toString() {
		return String.format("%s", id);
	}
}
