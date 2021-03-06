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


import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;

import org.apache.log4j.Logger;
import org.simpleframework.xml.Element;
import org.volante.abm.agent.Agent;
import org.volante.abm.agent.PotentialAgent;
import org.volante.abm.agent.PotentialAgentProductionObserver;
import org.volante.abm.data.Cell;
import org.volante.abm.data.CellCapitalObserver;
import org.volante.abm.data.ModelData;
import org.volante.abm.data.Region;
import org.volante.abm.data.Service;
import org.volante.abm.models.utils.CellVolatilityObserver;
import org.volante.abm.models.utils.ProductionWeightReporter;
import org.volante.abm.models.utils.TakeoverObserver;
import org.volante.abm.output.GivingInStatisticsObserver;
import org.volante.abm.schedule.RunInfo;


/**
 * Cells that sampled potential agents seek to take over are selected based on the potential agents'
 * potential production of its main service on the particular cell.
 * 
 * @author Sascha Holzhauer
 * 
 */
public class BestProductionFirstGiveUpGiveInAllocationModel extends GiveUpGiveInAllocationModel
		implements CellCapitalObserver, PotentialAgentProductionObserver {

	/**
	 * Logger
	 */
	static private Logger					logger			= Logger.getLogger(BestProductionFirstGiveUpGiveInAllocationModel.class);

	protected Region						region;

	Map<PotentialAgent, SortedList<Cell>>	cellProductions	= new HashMap<PotentialAgent, SortedList<Cell>>();

	static int								counter			= 0;

	/**
	 * Applied to sampled indices from the list of sorted cells. A curve object can be assigned to
	 * the factory used to sample probabilities for selection of indices. The curve object is
	 * provided with the index to select or not.
	 */
	@Element(required = false)
	protected IterativeCellSamplerFactory	samplerFactory	= new IterativeCellSamplerFactory();

	@Override
	public void initialise(ModelData data, RunInfo info, Region r) {
		// <- LOGGING
		logger.info("Init...");
		// LOGGING ->
		super.initialise(data, info, r);
		this.region = r;
		this.initCellProductions();

		for (Cell c : region.getAllCells()) {
			c.registerCellCapitalObserver(this);
		}
		for (PotentialAgent pa : region.getAllPotentialAgents()) {
			pa.registerPotentialAgentProductionObserver(this);
		}
	};

	protected void initCellProductions() {
		for (final PotentialAgent pa : this.region.getPotentialAgents()) {
			final Service mainService;
			if (pa.getProduction() instanceof ProductionWeightReporter) {
				mainService = ((ProductionWeightReporter) pa.getProduction())
						.getProductionWeights().getMax();
			} else {
				mainService = null;
			}

			// <- LOGGING
			if (logger.isDebugEnabled()) {
				logger.debug("Main service for " + pa + ": " + mainService);
			}
			// LOGGING ->

			cellProductions.put(
					pa,
					new SortedList<Cell>(FXCollections.<Cell> observableArrayList(this.region
							.getCells()),
							new Comparator<Cell>() {
						@Override
						public int compare(Cell cell1, Cell cell2) {
									return (-1)
											* Double.compare(
													pa.getPotentialSupply(cell1).getDouble(
															mainService),
													pa
															.getPotentialSupply(cell2).getDouble(
																	mainService));
						}

					}));
			storeInitialCellSet("Initial_Fx" + pa.getID());
		}

		// // <- LOGGING
		// PotentialAgent a = region.getPotentialAgents().iterator().next();
		// Service mainService = ((ProductionWeightReporter)
		// a.getProduction()).getProductionWeights().getMax();
		// StringBuffer buffer = new StringBuffer();
		// SortedList<Cell> slist = cellProductions.get(a);
		// for (int i = 0; i < slist.size(); i++) {
		// // buffer.append(slist.get(i).getOwnerID() + "] competitiveness:"
		// // + region.getCompetitiveness(slist.get(i)) + System.getProperty("line.separator"));
		// //
		// buffer.append(slist.get(i) + "] production:" +
		// a.getPotentialSupply(slist.get(i)).getDouble(mainService)
		// + System.getProperty("line.separator"));
		// }
		// logger.info("Order: " + buffer.toString());
		// // LOGGING ->
	}

	/**
	 * Only output when logger in DEBUG level (or below)
	 */
	private void storeInitialCellSet(String id) {
		counter++;

		// <- LOGGING
		if (logger.isDebugEnabled()) {
			logger.debug("Output initial cell set for " + id + "(" + counter + ")");

			StringBuffer buffer = new StringBuffer();

			for (Cell c : region.getAllCells()) {
				buffer.append(c.getX() + "," + c.getY() + ","
						+ c.getOwner().getType().getSerialID()
						+ System.getProperty("line.separator"));
			}
			FileWriter fw;
			try {
				fw = new FileWriter("./output/" + id + "_" + String.format("%2d", counter) + ".csv");
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write(buffer.toString());
				bw.flush();
				bw.close();
			} catch (IOException exception) {
				exception.printStackTrace();
			}
		}
		// LOGGING ->
	}

	/**
	 * Tries to create one of the given agents if it can take over a cell
	 * 
	 * @param a
	 * @param r
	 */
	public void tryToComeIn(final PotentialAgent a, final Region r) {
		if (a == null) {
			return; // In the rare case that all have 0 competitiveness, a can be null
		}

		IterativeCellSampler cellsampler =
				this.samplerFactory.getIterativeCellSampler(r.getNumCells(), numSearchedCells, r);

		logger.debug("Try " + a.getID() + " to take over on mostly " + numSearchedCells
				+ " cells (region " + r.getID()
				+ " has " + r.getNumCells() + " cells).");

		Cell c;
		Double competitiveness;

		boolean takenover = false;
		while (!takenover && cellsampler.hasMoreToSample()) {
			c = cellProductions.get(a).get(cellsampler.sample());
			competitiveness = r.getCompetitiveness(a, c);

			if (logger.isDebugEnabled()) {
				logger.debug(cellsampler.numSampled() + "th sampled cell: " + c + " (owners["
						+ c.getOwnerID()
						+ "] competitiveness:" + r.getCompetitiveness(c) + " / challenger (" + a
						+ "): "
						+ competitiveness + ")");
			}

			if (competitiveness > a.getGivingUp() && c.getOwner().canTakeOver(c, competitiveness)) {
				Agent agent = a.createAgent(r);

				for (TakeoverObserver observer : takeoverObserver) {
					observer.setTakeover(r, c.getOwner(), agent);
				}
				for (CellVolatilityObserver o : cellVolatilityObserver) {
					o.increaseVolatility(c);
				}

				// <- LOGGING
				if (logger.isDebugEnabled()) {
					logger.debug("Ownership from :" + c.getOwner() + " --> " + agent);
					logger.debug("Take over " + cellsampler.numSampled() + "th cell (" + c
							+ ") of " + numSearchedCells);
				}
				// LOGGING ->

				for (GivingInStatisticsObserver observer : this.statisticsObserver) {
					observer.setNumberSearchedCells(r, a, cellsampler.numSampled());
				}

				r.setOwnership(agent, c);
				takenover = true;
			}
		}
	}

	@Override
	public void registerTakeoverOberserver(TakeoverObserver observer) {
		takeoverObserver.add(observer);

		// <- LOGGING
		if (logger.isDebugEnabled()) {
			logger.debug("Register TakeoverObserver " + observer);
		}
		// LOGGING ->
	}

	@Override
	public void cellCapitalChanged(Cell cell, boolean remove) {
		// <- LOGGING
		if (logger.isDebugEnabled()) {
			logger.debug("Cell capital changed: " + cell);
		}
		// LOGGING ->

		// // <- LOGGING
		// PotentialAgent a = region.getPotentialAgents().iterator().next();
		// Service mainService = ((ProductionWeightReporter)
		// a.getProduction()).getProductionWeights().getMax();
		// StringBuffer buffer = new StringBuffer();
		// SortedList<Cell> slist = cellProductions.get(a);
		// for (int i = 0; i < slist.size(); i++) {
		// // buffer.append(slist.get(i).getOwnerID() + "] competitiveness:"
		// // + region.getCompetitiveness(slist.get(i)) + System.getProperty("line.separator"));
		// //
		// buffer.append(slist.get(i) + "] production:" +
		// a.getPotentialSupply(slist.get(i)).getDouble(mainService)
		// + System.getProperty("line.separator"));
		// }
		// logger.info("Order: " + buffer.toString());
		// // LOGGING ->

		for (final PotentialAgent pa : this.region.getPotentialAgents()) {
			if (remove) {
				cellProductions.get(pa).getSource().remove(cell);
			} else {
				((ObservableList<Cell>) cellProductions.get(pa).getSource()).add(cell);
			}
		}
	}

	@Override
	public void potentialAgentProductionChanged(final PotentialAgent pa) {
		final Service mainService;
		if (pa.getProduction() instanceof ProductionWeightReporter) {
			mainService = ((ProductionWeightReporter) pa.getProduction()).getProductionWeights()
					.getMax();
		} else {
			mainService = null;
		}

		cellProductions.put(
				pa,
				new SortedList<Cell>(FXCollections.<Cell> observableArrayList(this.region
						.getCells()),
						new Comparator<Cell>() {
							@Override
							public int compare(Cell cell1, Cell cell2) {
								return (-1)
										* Double.compare(
												pa.getPotentialSupply(cell1).getDouble(mainService),
												pa
														.getPotentialSupply(cell2).getDouble(
																mainService));
							}

						}));
	}
}