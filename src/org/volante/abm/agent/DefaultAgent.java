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
package org.volante.abm.agent;


import org.apache.log4j.Logger;
import org.volante.abm.data.Cell;
import org.volante.abm.data.ModelData;
import org.volante.abm.data.Region;
import org.volante.abm.data.Service;
import org.volante.abm.example.SimpleProductionModel;
import org.volante.abm.models.ProductionModel;
import org.volante.abm.models.nullmodel.NullProductionModel;

import com.moseph.modelutils.fastdata.DoubleMap;
import com.moseph.modelutils.fastdata.UnmodifiableNumberMap;


/**
 * This is a default agent
 *
 * @author jasper
 *
 */
public class DefaultAgent extends AbstractAgent {

	/**
	 * Logger
	 */
	static private Logger	logger	= Logger.getLogger(DefaultAgent.class);


	/*
	 * Characteristic fields (define an agent)
	 */
	protected ProductionModel	production	= NullProductionModel.INSTANCE;
	protected double			givingUp	= -Double.MAX_VALUE;
	protected double			givingIn	= Double.MAX_VALUE;
	protected PotentialAgent	type		= null;

	public DefaultAgent() {
	}

	public DefaultAgent(String id, ModelData data) {
		this(null, id, data, null, NullProductionModel.INSTANCE, -Double.MAX_VALUE,
				Double.MAX_VALUE);
	}

	public DefaultAgent(PotentialAgent type, ModelData data, Region r, ProductionModel prod,
			double givingUp, double givingIn) {
		this(type, "NA", data, r, prod, givingUp, givingIn);
	}

	public DefaultAgent(PotentialAgent type, String id, ModelData data, Region r,
			ProductionModel prod, double givingUp, double givingIn) {
		this.id = id;
		this.type = type;
		this.region = r;
		this.production = prod;
		this.givingUp = givingUp;
		this.givingIn = givingIn;

		productivity = new DoubleMap<Service>(data.services);
	}

	@Override
	public void updateSupply() {
		productivity.clear();
		for (Cell c : cells) {
			production.production(c, c.getModifiableSupply());

			if (logger.isDebugEnabled()) {
				logger.debug(this + "(cell " + c.getX() + "|" + c.getY() + "): " + c.getModifiableSupply().prettyPrint());
			}
			c.getSupply().addInto(productivity);
		}
	}

	/**
	 * @see org.volante.abm.agent.Agent#getProductionModel()
	 */
	@Override
	public ProductionModel getProductionModel() {
		return this.production;
	}

	@Override
	public void considerGivingUp() {
		if (currentCompetitiveness < givingUp) {
			giveUp();
		}
	}

	@Override
	public boolean canTakeOver(Cell c, double incoming) {
		return incoming > (getCompetitiveness() + givingIn);
	}

	@Override
	public UnmodifiableNumberMap<Service> supply(Cell c) {
		DoubleMap<Service> prod = productivity.duplicate();
		production.production(c, prod);
		return prod;
	}

	public void setProductionFunction(ProductionModel f) {
		this.production = f;
	}

	public ProductionModel getProductionFunction() {
		return production;
	}

	public void setGivingUp(double g) {
		this.givingUp = g;
	}

	public void setGivingIn(double g) {
		this.givingIn = g;
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
	public PotentialAgent getType() {
		return type;
	}

	@Override
	public String infoString() {
		return "Giving up: " + givingUp + ", Giving in: " + givingIn + ", nCells: " + cells.size();
	}
}
