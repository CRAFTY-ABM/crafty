/**
 * 
 */
package org.volante.abm.models;


import org.volante.abm.data.Service;

import com.moseph.modelutils.fastdata.DoubleMap;


/**
 * Basically, only world residuals are required to implement a global/super-regional market.
 * However, in order to calculate per-cell residuals entire-market-level demands, supplies and
 * number of cells are required. Therefore, these values are passed to the demand model which then
 * has more flexibility to calculate per-cell residuals (e.g. adding noise to some input data).
 * 
 * @author Sascha Holzhauer
 * 
 */
public interface WorldDemandModel extends DemandModel {

	public void setWorldNumberCells(int numCells);

	public DoubleMap<Service> getRegionalDemand();

	public void setWorldDemand(DoubleMap<Service> worldDemand);

	public DoubleMap<Service> getRegionalSupply();

	public void setWorldSupply(DoubleMap<Service> worldSupply);

}
