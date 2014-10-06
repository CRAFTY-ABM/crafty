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


import static com.moseph.modelutils.Utilities.sample;
import static com.moseph.modelutils.Utilities.sampleN;
import static com.moseph.modelutils.Utilities.scoreMap;
import static java.lang.Math.pow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.simpleframework.xml.Attribute;
import org.volante.abm.agent.Agent;
import org.volante.abm.agent.PotentialAgent;
import org.volante.abm.data.Capital;
import org.volante.abm.data.Cell;
import org.volante.abm.data.ModelData;
import org.volante.abm.data.Region;
import org.volante.abm.output.TakeoverMessenger;
import org.volante.abm.output.TakeoverObserver;
import org.volante.abm.param.RandomPa;
import org.volante.abm.schedule.RunInfo;
import org.volante.abm.serialization.BatchRunParser;

import com.moseph.modelutils.Utilities;
import com.moseph.modelutils.Utilities.Score;
import com.moseph.modelutils.Utilities.ScoreComparator;


/**
 * A very simple kind of allocation. Any abandoned cells get the most competitive agent assigned to
 * them.
 * 
 * @author dmrust
 * 
 */
public class GiveUpGiveInAllocationModel extends SimpleAllocationModel implements TakeoverMessenger {

	/**
	 * Logger
	 */
	static private Logger	logger				= Logger.getLogger(GiveUpGiveInAllocationModel.class);

	/**
	 * The number of cells an agent (type) can search over to find maximum competitiveness
	 */
	@Attribute(required = false)
	public String			numCells			= "NaN";

	protected int			numSearchedCells	= Integer.MIN_VALUE;

	/**
	 * Alternative to {@link this#numCells}: specify the percentage of entire cells in the region to
	 * search over.
	 */
	@Attribute(required = false)
	public String			percentageCells		= "NaN";

	/**
	 * The number of times an agent (type) can search the above no. of
	 */
	@Attribute(required = false)
	public int				numTakeovers		= 30;													// The
																										// number
																										// of
																										// times
																										// an
																										// agent
																										// (type)
																										// can
																										// search
																										// the
																										// above
																										// no.
																										// of
																										// cells
	@Attribute(required = false)
	public int				probabilityExponent	= 2;
	Cell					perfectCell			= new Cell();
	ModelData				data				= null;

	Set<TakeoverObserver>	takeoverObserver	= new HashSet<TakeoverObserver>();

	@Override
	public void initialise(ModelData data, RunInfo info, Region r) {
		super.initialise(data, info, r);

		if (!numCells.equals("NaN") && !this.percentageCells.equals("NaN")) {
			logger.error("You may not specify both, numCells and percentageCells!");
			throw new IllegalStateException(
					"You may not specify both, numCells and percentageCells!");
		}

		if (numCells.equals("NaN")) {
			if (this.percentageCells.equals("NaN")) {
				logger.error("You need to specify either numCells or percentageCells!");
				throw new IllegalStateException(
						"You need to specify either numCells or percentageCells!");
			} else {
				this.numSearchedCells = (int) (r.getNumCells()
						* BatchRunParser.parseDouble(this.percentageCells, info) / 100.0);
			}
		} else {
			this.numSearchedCells = BatchRunParser.parseInt(this.numCells, info);
		}


		this.data = data;
		perfectCell.initialise(data, info, r);
		for (Capital c : data.capitals) {
			perfectCell.getModifiableBaseCapitals().putDouble(c, 1);
		}
	};

	/**
	 * Creates a copy of the best performing potential agent on each empty cell
	 */
	@Override
	public void allocateLand(final Region r) {
		if (r.getRinfo().getSchedule().getCurrentTick() == r.getRinfo().getSchedule()
				.getStartTick()) {
			for (TakeoverObserver o : takeoverObserver) {
				o.initTakeOvers(r);
			}
		}

		super.allocateLand(r); // Puts the best agent on any unmanaged cells
		Score<PotentialAgent> compScore = new Score<PotentialAgent>()
		{
			@Override
			public double getScore(PotentialAgent a)
			{
				return pow(r.getCompetitiveness(a, perfectCell), probabilityExponent);
			}
		};
		for (int i = 0; i < numTakeovers; i++) {
			// Resample this each time to deal with changes in supply affecting competitiveness
			Map<PotentialAgent, Double> scores = scoreMap(r.getPotentialAgents(), compScore);

			tryToComeIn(
					sample(scores, true, r.getRandom().getURService(),
							RandomPa.RANDOM_SEED_RUN_ALLOCATION.name()), r);
		}
	}

	/**
	 * Tries to create one of the given agents if it can take over a cell
	 * 
	 * @param a
	 * @param r
	 */
	/*
	 * public void tryToComeIn( final PotentialAgent a, final Region r ) { if( a == null ) return;
	 * //In the rare case that all have 0 competitiveness, a can be null final Agent agent =
	 * a.createAgent( r ); Map<Cell, Double> competitiveness = scoreMap( sampleN( r.getCells(),
	 * numCells ), new Score<Cell>() { public double getScore( Cell c ) { return
	 * r.getCompetitiveness( agent.supply( c ), c ); } }); List<Cell> sorted = new
	 * ArrayList<Cell>(competitiveness.keySet()); Collections.sort( sorted, new
	 * ScoreComparator<Cell>( competitiveness ) );
	 * 
	 * 
	 * for( Cell c : sorted ) { if( competitiveness.get( c ) < a.getGivingUp() ) break; boolean
	 * canTake = c.getOwner().canTakeOver( c, competitiveness.get(c) ); if( canTake ) {
	 * r.setOwnership( agent, c ); break; } } }
	 */

	public void tryToComeIn(final PotentialAgent a, final Region r) {
		if (a == null) {
			return; // In the rare case that all have 0 competitiveness, a can be null
		}

		Map<Cell, Double> competitiveness = scoreMap(
				sampleN(r.getCells(), numSearchedCells, r.getRandom().getURService(),
						RandomPa.RANDOM_SEED_RUN_ALLOCATION.name()),
				new Score<Cell>() {
					@Override
					public double getScore(Cell c)
					{
						return r.getCompetitiveness(a, c);
					}
				});

		List<Cell> sorted = new ArrayList<Cell>(competitiveness.keySet());
		Collections.sort(sorted, new ScoreComparator<Cell>(competitiveness));
		// For checking cells in reverse score order:
		// Collections.reverse( sorted);
		// For checking cells randomly:
		Utilities.shuffle(sorted, r.getRandom().getURService(),
				RandomPa.RANDOM_SEED_RUN_ALLOCATION.name());

		logger.debug("Allocate " + sorted.size() + " cells (region " + r.getID() + " has "
				+ r.getNumCells() + " cells).");

		for (Cell c : sorted) {
			// if (competitiveness.get(c) < a.getGivingUp()) return;
			if (competitiveness.get(c) > a.getGivingUp()) {
				boolean canTake = c.getOwner().canTakeOver(c, competitiveness.get(c));
				if (canTake) {
					Agent agent = a.createAgent(r);

					for (TakeoverObserver observer : takeoverObserver) {
						observer.setTakeover(r, c.getOwner(), agent);
					}

					// <- LOGGING
					if (logger.isDebugEnabled()) {
						logger.debug("Ownership from :" + c.getOwner() + " --> " + agent);
					}
					// LOGGING ->

					r.setOwnership(agent, c);

					break;
				}
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
}
