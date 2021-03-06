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
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;
import org.volante.abm.agent.Agent;
import org.volante.abm.agent.PotentialAgent;
import org.volante.abm.data.Cell;
import org.volante.abm.data.ModelData;
import org.volante.abm.data.Region;
import org.volante.abm.models.AllocationModel;
import org.volante.abm.models.utils.CellVolatilityMessenger;
import org.volante.abm.models.utils.CellVolatilityObserver;
import org.volante.abm.param.RandomPa;
import org.volante.abm.schedule.RunInfo;
import org.volante.abm.visualisation.SimpleAllocationDisplay;

import com.moseph.modelutils.Utilities;


/**
 * A very simple kind of allocation. Any abandoned cells get the most
 * competitive agent assigned to them.
 * 
 * Note: Subclasses need to consider reporting allocation changes to the
 * {@link CellVolatilityObserver}.
 * 
 * @author dmrust
 * @author Sascha Holzhauer
 * 
 */
@Root
public class SimpleAllocationModel implements AllocationModel,
		CellVolatilityMessenger
{
	/**
	 * Logger
	 */
	static private Logger	logger	= Logger.getLogger(SimpleAllocationModel.class);


	protected Set<CellVolatilityObserver> cellVolatilityObserver = new HashSet<CellVolatilityObserver>();

	@Attribute(required = false)
	double						proportionToAllocate	= 1;

	@Override
	public void initialise( ModelData data, RunInfo info, Region r ){};
	
	/**
	 * Creates a copy of the best performing potential agent on each empty cell
	 */
	@Override
	public void allocateLand( Region r )
	{
		// <- LOGGING
		logger.info("Allocate land for region " + r + " (allocating " + r.getAvailable().size()
				+ " cells)...");
		// LOGGING ->

		// Determine random subset of available cells:
		Collection<Cell> cells2allocate = Utilities.sampleN(r.getAvailable(),
				(int) (r.getAvailable().size() * proportionToAllocate), r.getRandom()
						.getURService(),
				RandomPa.RANDOM_SEED_RUN_ALLOCATION.name());

		for (Cell c : cells2allocate) {
			// <- LOGGING
			if (logger.isDebugEnabled()) {
				logger.debug("Create best agent for cell " + c + " of region "
						+ r + " (current owner: " + c.getOwner()
						+ ")...");
			}
			// LOGGING ->

			createBestAgentForCell( r, c );
		}
	}

	private void createBestAgentForCell( Region r, Cell c )
	{
		List<PotentialAgent> potential = new ArrayList<PotentialAgent>( r.getPotentialAgents() );
		double max = -Double.MAX_VALUE;
		PotentialAgent p = null;
		for( PotentialAgent a : potential )
		{
			if (!r.hasInstitutions() || r.getInstitutions().isAllowed(a, c)) {
				double s = r.getCompetitiveness( a, c );
	
				// <- LOGGING
				if (logger.isDebugEnabled()) {
					logger.debug(a + "> competitiveness: " + s);
				}
				// LOGGING ->
	
				if( s > max )
				{
					if (s > a.getGivingUp()) {
						max = s;
						p = a;
					}
				}
			}
		}
		//Only create agents if their competitiveness is good enough

		// TODO
		if (p != null) {
			Agent agent = p.createAgent(r);

			// <- LOGGING
			if (logger.isDebugEnabled()) {
				logger.debug("Ownership from :" + c.getOwner() + " --> " + agent);
			}
			// LOGGING ->

			r.setOwnership(agent, c);

			for (CellVolatilityObserver o : cellVolatilityObserver) {
				o.increaseVolatility(c);
			}
		}
	}

	@Override
	public AllocationDisplay getDisplay()
	{
		return new SimpleAllocationDisplay(this);
	}

	/**
	 * @see org.volante.abm.models.utils.CellVolatilityMessenger#registerCellVolatilityOberserver(org.volante.abm.models.utils.CellVolatilityObserver)
	 */
	@Override
	public void registerCellVolatilityOberserver(CellVolatilityObserver observer) {
		this.cellVolatilityObserver.add(observer);
	}
}