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


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.simpleframework.xml.Attribute;
import org.volante.abm.agent.Agent;
import org.volante.abm.agent.PotentialAgent;
import org.volante.abm.data.Cell;
import org.volante.abm.data.ModelData;
import org.volante.abm.data.Region;
import org.volante.abm.data.Service;
import org.volante.abm.schedule.PreTickAction;
import org.volante.abm.schedule.RunInfo;

import com.moseph.modelutils.curve.Curve;
import com.moseph.modelutils.curve.LinearFunction;
import com.moseph.modelutils.fastdata.DoubleMap;
import com.moseph.modelutils.fastdata.UnmodifiableNumberMap;


/**
 * A more complex model of competitiveness allowing the applications of functions.
 * 
 * @author dmrust
 * 
 */
public class NormalisedAdaptingCurveCompetitivenessModel extends CurveCompetitivenessModel implements PreTickAction {

	/**
	 * Logger
	 */
	static private Logger logger = Logger.getLogger(NormalisedAdaptingCurveCompetitivenessModel.class);

	@Attribute(required = false)
	boolean	normaliseCellResidual	= true;


	@Attribute(required = false)
	boolean	normaliseCellSupply		= true;

	boolean curveAdapted = false;

	public void initialise(ModelData data, RunInfo info, Region extent) throws Exception {
		super.initialise(data, info, extent);
		info.getSchedule().register(this);
	}
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
		// adaptCurves();
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

	protected void adaptCurves() {
		if (!this.curveAdapted) {
			for (Service service : this.data.services) {
				double balanced = getBalancingCompetitiveness(service);
				// <- LOGGING
				logger.info("Set competition function parameter A for " + service + " to " + balanced);
				// LOGGING ->
				((LinearFunction) this.curves.get(service)).setA(balanced);
			}
		}
		this.curveAdapted = true;
	}

	public double getBalancingCompetitiveness(Service service) {
		if (this.region.getDemandModel().getDemand().get(service) <= 0) {
			return 0;
		}

		double prodsum = 0;
		TreeSet<CompetitionCell> sortedSet = new TreeSet<>();
		
		for (Cell c : this.region.getAllCells()) {
			if (c.getOwner() != Agent.NOT_MANAGED) {
				double prod = c.getSupply().getDouble(service);
				if (prod > 0) {
					prodsum += prod;
					sortedSet.add(new CompetitionCell(c, c.getOwner().getGivingUp() / prod, prod));
				}
			}
		}
		// this restriction assumes most competitive farmers are currently allocated...
		if (prodsum < this.region.getDemandModel().getDemand().get(service)) {
			for (Cell c : this.region.getAvailable()) {
				// get best performing AFT with respect to service:
				prodsum += createBestAgentForCell(service, c, sortedSet);
			}
		}

		if (prodsum < this.region.getDemandModel().getDemand().get(service)) {
			// <- LOGGING
			if (logger.isDebugEnabled()) {
				logger.debug("Demand for service " + service + " cannot be fulfilled. Returning Double.MAX_VALUE");
			}
			// LOGGING ->

			return Double.MAX_VALUE;

		} else {
			prodsum = 0;
			Iterator<CompetitionCell> iterator = sortedSet.iterator();
			CompetitionCell ccell = null;
			while (prodsum < this.region.getDemandModel().getDemand().get(service) && iterator.hasNext()) {
				ccell = iterator.next();
				prodsum += ccell.production;
			}

			// <- LOGGING
			if (ccell == null && logger.isDebugEnabled()) {
				logger.debug("Demand for service " + service + " cannot be fulfilled. Returning Double.MAX_VALUE");
			}
			// LOGGING ->

			return (ccell == null ? Double.MAX_VALUE : ccell.prodcomp);
		}
	}

	public class CompetitionCell implements Comparable<CompetitionCell> {
		protected double prodcomp = 0;
		protected double production = 0;
		protected Cell cell = null;

		protected CompetitionCell(Cell cell, double prodcomp, double production) {
			this.cell = cell;
			this.prodcomp = prodcomp;
			this.production = production;
		}

		/**
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		@Override
		public int compareTo(CompetitionCell o) {
			return Double.compare(prodcomp, o.prodcomp);
		}
	}

	private double createBestAgentForCell(Service s, Cell c, TreeSet<CompetitionCell> sortedSet) {
		List<PotentialAgent> potential = new ArrayList<PotentialAgent>(this.region.getPotentialAgents());
		double max = 0;
		PotentialAgent p = null;
		for (PotentialAgent a : potential) {
			DoubleMap<Service> producable = this.data.serviceMap();
			a.getProduction().production(c, producable);
			double prods = producable.get(s);

			if (prods > max) {
				max = prods;
				p = a;
			}
		}

		if (max > 0) {
			sortedSet.add(new CompetitionCell(c, p.getGivingUp() / max, max));
		}

		return max;
	}

	/**
	 * @see org.volante.abm.schedule.PreTickAction#preTick()
	 */
	@Override
	public void preTick() {
		this.curveAdapted = false;
	}
}
