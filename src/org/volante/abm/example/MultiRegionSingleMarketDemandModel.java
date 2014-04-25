/**
 * 
 */
package org.volante.abm.example;


import org.volante.abm.data.Cell;
import org.volante.abm.data.ModelData;
import org.volante.abm.data.Region;
import org.volante.abm.data.Service;
import org.volante.abm.models.WorldDemandModel;
import org.volante.abm.schedule.RunInfo;

import com.moseph.modelutils.fastdata.DoubleMap;
import com.moseph.modelutils.fastdata.UnmodifiableNumberMap;


/**
 * @author Sascha Holzhauer
 *
 */
public class MultiRegionSingleMarketDemandModel extends RegionalDemandModel implements
		WorldDemandModel {

	protected DoubleMap<Service>	worldDemand		= null;

	protected DoubleMap<Service>	worldSupply		= null;

	protected DoubleMap<Service>	worldResidual	= null;

	protected int					worldNumCells	= 0;

	@Override
	public void initialise(ModelData data, RunInfo info, Region r) throws Exception {
		super.initialise(data, info, r);
		this.worldDemand = data.serviceMap();
		this.worldSupply = data.serviceMap();
		this.worldResidual = data.serviceMap();
	}

	/**
	 * Needed to be re-implemented because {@link this#updateSupply()} does not call {@link
	 * this#recalculateResidual()} anymore.
	 * 
	 * @see org.volante.abm.example.RegionalDemandModel#setDemand(com.moseph.modelutils.fastdata.UnmodifiableNumberMap)
	 */
	@Override
	public void setDemand(UnmodifiableNumberMap<Service> dem) {
		dem.copyInto(demand);
		updateSupply();
		recalculateResidual();
	}

	/**
	 * In contrast to {@link RegionalDemandModel#updateSupply()} this version does not call
	 * {@link RegionalDemandModel#recalculateResidual()}!
	 * 
	 * @see org.volante.abm.example.RegionalDemandModel#updateSupply()
	 */
	@Override
	public void updateSupply() {
		if (updateOnAgentChange) {
			for (Cell c : region.getCells()) {
				c.getSupply().copyInto(supply.get(c));
			}
		}
		totalSupply.clear();
		for (Cell c : region.getCells()) {
			c.getSupply().addInto(totalSupply);
		}
	}

	/**
	 * This yields most likely the same values in all regions but could be changed for certain
	 * regions, that e.g. have no full access to global market.
	 * 
	 * @see org.volante.abm.example.RegionalDemandModel#recalculateResidual()
	 */
	@Override
	public void recalculateResidual() {
		worldDemand.multiplyInto(1.0 / this.worldNumCells, perCellDemand);
		worldDemand.subtractInto(worldSupply, worldResidual);
		worldResidual.multiplyInto(1.0 / this.worldNumCells, perCellResidual);
	}

	@Override
	public DoubleMap<Service> getResidualDemand() {
		return worldResidual;
	}

	@Override
	public void postTick() {
		log.info("Demand: " + demand.prettyPrint());
		log.info("Supply: " + totalSupply.prettyPrint());
		log.info("WorldResidual: " + worldResidual.prettyPrint());
		log.info("Marginal Utilities: " + getMarginalUtilities().prettyPrint());
	}

	@Override
	public void setWorldNumberCells(int numCells) {
		this.worldNumCells = numCells;
	}

	@Override
	public DoubleMap<Service> getRegionalDemand() {
		return this.demand;
	}

	@Override
	public void setWorldDemand(DoubleMap<Service> worldDemand) {
		this.worldDemand = worldDemand;
	}

	@Override
	public DoubleMap<Service> getRegionalSupply() {
		return this.totalSupply;
	}

	@Override
	public void setWorldSupply(DoubleMap<Service> worldSupply) {
		this.worldSupply = worldSupply;
	}
}
