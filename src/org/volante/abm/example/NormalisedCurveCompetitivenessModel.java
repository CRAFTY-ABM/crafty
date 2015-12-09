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
import org.volante.abm.data.Service;

import com.moseph.modelutils.curve.Curve;
import com.moseph.modelutils.fastdata.UnmodifiableNumberMap;


/**
 * A more complex model of competitiveness allowing the applications of functions.
 * 
 * @author dmrust
 * 
 */
public class NormalisedCurveCompetitivenessModel extends CurveCompetitivenessModel {

	/**
	 * Logger
	 */
	static private Logger log = Logger.getLogger(NormalisedCurveCompetitivenessModel.class);

	@Attribute(required = false)
	boolean	normaliseCellResidual	= true;


	@Attribute(required = false)
	boolean	normaliseCellSupply		= true;

	/**
	 * Adds up marginal utilities (determined by competitiveness for unmet
	 * demand) of all services.
	 * 
	 * @param residualDemand
	 * @param supply
	 * @param showWorking
	 *            if true, log details in DEBUG mode
	 * @return summed marginal utilities of all services
	 */
	public double addUpMarginalUtilities(UnmodifiableNumberMap<Service> residualDemand,
			UnmodifiableNumberMap<Service> supply, boolean showWorking) {
		double sum = 0;

		for (Service s : supply.getKeySet()) {
			Curve c = curves.get(s); /* Gets the curve parameters for this service */

			double perCellDemand = region.getDemandModel().getAveragedPerCellDemand().get(s);
			perCellDemand = perCellDemand == 0 ? Double.MIN_VALUE : perCellDemand;

			if (c == null) {
				String message = "Missing curve for: " + s.getName() + " got: " + curves.keySet();
				log.fatal(message);
				throw new IllegalStateException(message);
			}
			double res = residualDemand.getDouble(s);
			if (normaliseCellResidual) {
				res /= perCellDemand;
			}
			double marginal = c.sample(res); /*
											 * Get the corresponding 'value' (y-value) for this
											 * level of unmet demand
											 */
			double amount = supply.getDouble(s);
			if (this.normaliseCellSupply) {
				amount /= perCellDemand;
			}

			if (removeNegative && marginal < 0) {
				marginal = 0;
			}

			double comp = (marginal == 0 || amount == 0 ? 0 : marginal * amount);

			if (log.isTraceEnabled()) {
				log.trace(String.format(
						"\t\tService %10s: Residual (%5f) > Marginal (%5f; Curve: %s) * Amount (%5f) = %5f",
						s.getName(), res, marginal, c.toString(), amount, marginal * amount));
			}
			sum += comp;
		}
		log.trace("Competitiveness sum: " + sum);

		return sum;
	}
}
