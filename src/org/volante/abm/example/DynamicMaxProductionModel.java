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
 * Created by Sascha Holzhauer on 12 Oct 2015
 */
package org.volante.abm.example;


import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.nfunk.jep.JEP;
import org.simpleframework.xml.Attribute;
import org.volante.abm.data.Capital;
import org.volante.abm.data.Cell;
import org.volante.abm.data.ModelData;
import org.volante.abm.data.Region;
import org.volante.abm.data.Service;
import org.volante.abm.schedule.RunInfo;

import com.moseph.modelutils.fastdata.DoubleMap;
import com.moseph.modelutils.fastdata.UnmodifiableNumberMap;


/**
 * @author Sascha Holzhauer
 *
 */
public class DynamicMaxProductionModel extends SimpleProductionModel {

	/**
	 * Logger
	 */
	static private Logger logger = Logger.getLogger(DynamicMaxProductionModel.class);

	@Attribute(required = false)
	boolean allowImplicitMultiplication = true;

	protected Map<String, String> maxProductionFunctions = new HashMap<>();

	protected Map<Service, JEP> maxProductionParsers = new HashMap<>();

	protected RunInfo rInfo;

	/**
	 * Default constructor
	 */
	public DynamicMaxProductionModel() {
	}

	/**
	 * Takes an array of capital weights, in the form: { { c1s1, c2s1 ... } //Weights for service 1 { c1s2, c2s2 ... }
	 * //Weights for service 2 ... i.e. first index is Services, second is baseCapitals
	 * 
	 * @param weights
	 * @param productionWeights
	 */
	public DynamicMaxProductionModel(double[][] weights, double[] productionWeights) {
		this.capitalWeights.putT(weights);
		this.productionWeights.put(productionWeights);
	}

	/**
	 * @see org.volante.abm.example.SimpleProductionModel#initialise(org.volante.abm.data.ModelData,
	 *      org.volante.abm.schedule.RunInfo, org.volante.abm.data.Region)
	 */
	public void initialise(ModelData data, RunInfo info, Region r) throws Exception {
		super.initialise(data, info, r);
		this.rInfo = info;
		initMaxProductionFunctionFromCSV(data, info, r);
	}

	/**
	 * Avoids reading production weights.
	 * 
	 * @see org.volante.abm.example.SimpleProductionModel#initWeightsFromCSV(org.volante.abm.data.ModelData,
	 *      org.volante.abm.schedule.RunInfo, org.volante.abm.data.Region)
	 */
	void initWeightsFromCSV(ModelData data, RunInfo info, Region region) throws Exception {
		capitalWeights =
				info.getPersister().csvToMatrix(csvFile, data.capitals, data.services,
						region != null ? region.getPeristerContextExtra() : null);

		productionWeights = new DoubleMap<Service>(data.services);
	}

	/**
	 * Parses String in column "Production" with JEP function parser.
	 * 
	 * @see "http://www.cse.msu.edu/SENS/Software/jep-2.23/doc/website/"
	 * 
	 * @param data
	 * @param info
	 * @param region
	 * @throws Exception
	 */
	protected void initMaxProductionFunctionFromCSV(ModelData data, RunInfo info, Region region) throws Exception {
		maxProductionFunctions =
				info.getPersister().csvToStringMap(csvFile, "Service", "Production",
						region != null ? region.getPeristerContextExtra() : null);

		for (Service service : data.services) {
			JEP productionParser = new JEP();
			productionParser.addStandardFunctions();
			productionParser.addStandardConstants();

			productionParser.setImplicitMul(allowImplicitMultiplication);

			for (Capital capital : data.capitals) {
				productionParser.addVariable(capital.getName(), 0);
			}
			productionParser.addVariable("CTICK", 0);
			this.addVariablesHook(productionParser);

			// <- LOGGING
			if (logger.isDebugEnabled()) {
				logger.debug("Parse function '" + maxProductionFunctions.get(service.getName()) + "' for service "
						+ service.getName() + "...");
			}
			// LOGGING ->

			productionParser.parseExpression(maxProductionFunctions.get(service.getName()));

			if (productionParser.hasError()) {
				logger.error("Error while parsing maximum production function: " + productionParser.getErrorInfo());
				throw new IllegalStateException("Error while parsing maximum production function.");
			}

			maxProductionParsers.put(service, productionParser);
		}
	}

	/**
	 * Can be overridden to add variables to the production parser.
	 * 
	 * @param productionParser
	 */
	public void addVariablesHook(JEP productionParser) {
		// nothing to do here
	}

	/**
	 * Updates variables in maximum production function and puts function values in <code>productionWeights</code>.
	 * 
	 * @see org.volante.abm.example.SimpleProductionModel#production(com.moseph.modelutils.fastdata.UnmodifiableNumberMap,
	 *      com.moseph.modelutils.fastdata.DoubleMap, org.volante.abm.data.Cell)
	 */
	public void production(UnmodifiableNumberMap<Capital> capitals, DoubleMap<Service> production, Cell cell) {
		updateProductionWeigths(capitals);
		basicProduction(capitals, production, cell);
	}

	public void basicProduction(UnmodifiableNumberMap<Capital> capitals, DoubleMap<Service> production, Cell cell) {
		super.production(capitals, production, cell);
	}

	/**
	 * @param capitals
	 */
	protected void updateProductionWeigths(UnmodifiableNumberMap<Capital> capitals) {
		for (Service service : capitalWeights.rows()) {
			for (Capital capital : capitals.getKeySet()) {
				maxProductionParsers.get(service).addVariable(capital.getName(), capitals.getDouble(capital));
			}
			maxProductionParsers.get(service).addVariable("CTICK",
 rInfo.getSchedule().getCurrentTick());

			productionWeights.put(service, maxProductionParsers.get(service).getValue());
			if (maxProductionParsers.get(service).hasError()) {
				logger.error("Error while parsing maximum production function: "
						+ maxProductionParsers.get(service).getErrorInfo());
				throw new IllegalStateException("Error while parsing maximum production function.");
			}
		}
	}
}
