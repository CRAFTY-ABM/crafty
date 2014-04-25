/**
 * 
 */
package org.volante.abm.example;


import org.apache.log4j.Logger;
import org.volante.abm.data.ModelData;
import org.volante.abm.data.Region;
import org.volante.abm.data.RegionSet;
import org.volante.abm.data.Service;
import org.volante.abm.models.WorldDemandModel;
import org.volante.abm.models.WorldSynchronisationModel;
import org.volante.abm.schedule.MpiUtilities;
import org.volante.abm.schedule.RunInfo;

import com.moseph.modelutils.fastdata.DoubleMap;


/**
 * @author Sascha Holzhauer
 *
 */
public class SingleMarketWorldSynchronisationModel implements WorldSynchronisationModel {

	/**
	 * Logger
	 */
	static private Logger	logger		= Logger.getLogger(SingleMarketWorldSynchronisationModel.class);

	ModelData	modelData	= new ModelData();
	RunInfo		info		= new RunInfo();

	@Override
	public void initialise(ModelData data, RunInfo info) {
		this.modelData = data;
		this.info = info;
	}

	@Override
	public void synchronizeNumOfCells(RegionSet regions) {
		int numRegionalCells = 0;
		for (Region r : regions.getAllRegions()) {
			numRegionalCells += r.getNumCells();
		}

		int numWorldCells = MpiUtilities.distributeNumOfCells(numRegionalCells);

		// <- LOGGING
		logger.info("Number of cells in the world: " + numWorldCells);
		// LOGGING ->

		for (Region r : regions.getAllRegions()) {
			((WorldDemandModel) r.getDemandModel()).setWorldNumberCells(numWorldCells);
		}
	}

	/**
	 * @see org.volante.abm.models.WorldSynchronisationModel#synchronizeDemand(org.volante.abm.data.RegionSet, org.volante.abm.data.ModelData)
	 */
	@Override
	public void synchronizeDemand(RegionSet regions) {
		DoubleMap<Service> demand = modelData.serviceMap();
		for (Region r : regions.getAllRegions()) {
			if (r.getDemandModel() instanceof WorldDemandModel) {
				((WorldDemandModel) r.getDemandModel()).getRegionalDemand().addInto(demand);
			} else {
				throw new IllegalStateException("The demand model of region " + r
						+ " does not implement" +
						"WorldDemandModel but is meant to be applied to a global market!");
			}
		}

		double[] worldDemand = MpiUtilities.distributeWorldDemand(demand.getAll());
		DoubleMap<Service> worldDemandMap = new DoubleMap<Service>(modelData.services, worldDemand);

		// <- LOGGING
		logger.info("World Demand: " + worldDemandMap.prettyPrint());
		// LOGGING ->

		for (Region r : regions.getAllRegions()) {
			((WorldDemandModel) r.getDemandModel()).setWorldDemand(worldDemandMap);
		}
	}

	/**
	 * @see org.volante.abm.models.WorldSynchronisationModel#synchronizeSupply(org.volante.abm.data.RegionSet, org.volante.abm.data.ModelData)
	 */
	@Override
	public void synchronizeSupply(RegionSet regions) {
		DoubleMap<Service> supply = modelData.serviceMap();
		for (Region r : regions.getAllRegions()) {
			if (r.getDemandModel() instanceof WorldDemandModel) {
				((WorldDemandModel) r.getDemandModel()).getRegionalSupply().addInto(supply);

				// <- LOGGING
				logger.info("Supply of region " + r + ": "
						+ ((WorldDemandModel) r.getDemandModel()).getRegionalSupply().prettyPrint());
				// LOGGING ->

			} else {
				throw new IllegalStateException("The demand model of region " + r
						+ " does not implement" +
						"WorldDemandModel but is meant to be applied to a global market!");
			}
		}

		double[] worldSupply = MpiUtilities.distributeWorldSupply(supply.getAll());
		DoubleMap<Service> worldSupplyMap = new DoubleMap<Service>(modelData.services, worldSupply);

		// <- LOGGING
		logger.info("World Supply: " + worldSupplyMap.prettyPrint());
		// LOGGING ->

		for (Region r : regions.getAllRegions()) {
			((WorldDemandModel) r.getDemandModel()).setWorldSupply(worldSupplyMap);
		}
	}
}
