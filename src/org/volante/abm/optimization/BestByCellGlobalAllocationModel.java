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
package org.volante.abm.optimization;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.simpleframework.xml.Attribute;
import org.volante.abm.agent.PotentialAgent;
import org.volante.abm.data.Cell;
import org.volante.abm.param.RandomPa;

import com.moseph.modelutils.Utilities;


/**
 * Takes all the cells in the region, and assigns the agent which most optimises global fitness
 * to each cell.
 * 
 * Allows a number of iterations to try and improve
 * @author dmrust
 *
 */
public class BestByCellGlobalAllocationModel extends OptimizationAllocationModel<List<PotentialAgent>>
{
	@Attribute(required=false)
	boolean shuffle = true;
	@Attribute(required=false)
	int maxRuns = 1;
	@Attribute(required=false)
	double convergence = 0.1;
	@Attribute(required=false)
	int convergenceGens = 3;
	
	ConvergenceChecker checker;
	
	@Override
	void setupRun() 
	{ 
		checker = new ConvergenceChecker( convergence, convergenceGens, maxRuns );
	}
	

	@Override
	List<PotentialAgent> doRun()
	{
		List<PotentialAgent> list = currentLandUseList();
		List<Cell> cells = new ArrayList<Cell>(region.getCells());
		double comp = 0;
		while( ! checker.isSatisfied() )
		{
			if (shuffle) {
				Utilities.shuffle( cells, region.getRandom().getURService(), RandomPa.RANDOM_SEED_RUN_ALLOCATION.name() );
			}
			for( Cell c : cells ) {
				comp = assignBestAgent( c, c.getRegion().getPotentialAgents(), list );
			}
			checker.addScore( comp );
			log.debug("Score: "+comp);
		}
		return list;
	}
	
	double assignBestAgent( Cell c, Collection<PotentialAgent> agents, List<PotentialAgent> currentSetup )
	{
		double comp = -Double.MAX_VALUE;
		PotentialAgent best = null;
		int index = cellToIndex( c );
		for( PotentialAgent a : agents)
		{
			currentSetup.set( index, a );
			double score = calculateFitness( currentSetup );
			if( score > comp )
			{
				comp = score;
				best = a;
			}
		}
		currentSetup.set( index, best );
		return comp;
	}

	@Override
	void applySolution( List<PotentialAgent> solution ) { applyList( solution ); }
	@Override
	public String getOptimisationType() { return "Assign Best to Each Cell Randomly"; }
	
}
