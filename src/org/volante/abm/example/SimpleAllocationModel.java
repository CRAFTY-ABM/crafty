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
import java.util.List;

import org.apache.log4j.Logger;
import org.simpleframework.xml.Root;
import org.volante.abm.agent.Agent;
import org.volante.abm.agent.PotentialAgent;
import org.volante.abm.data.Cell;
import org.volante.abm.data.ModelData;
import org.volante.abm.data.Region;
import org.volante.abm.models.AllocationModel;
import org.volante.abm.schedule.RunInfo;
import org.volante.abm.visualisation.SimpleAllocationDisplay;

/**
 * A very simple kind of allocation. Any abandoned cells get the most
 * competitive agent assigned to them.
 * @author dmrust
 *
 */
@Root
public class SimpleAllocationModel implements AllocationModel
{
	/**
	 * Logger
	 */
	static private Logger	logger	= Logger.getLogger(SimpleAllocationModel.class);

	@Override
	public void initialise( ModelData data, RunInfo info, Region r ){};

	/**
	 * Creates a copy of the best performing potential agent on each empty cell
	 */
	@Override
	public void allocateLand( Region r )
	{
		// <- LOGGING
		logger.info("Allocate land for region " + r + " (" + r.getAvailable().size() + " cells)...");
		// LOGGING ->

		for( Cell c : new ArrayList<Cell>( r.getAvailable() ) ) {
			// <- LOGGING
			if (logger.isDebugEnabled()) {
				logger.debug("Create best agent for cell " + c + " of region " + r + " ...");
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
		}
	}

	@Override
	public AllocationDisplay getDisplay()
	{
		return new SimpleAllocationDisplay();
	}
}
