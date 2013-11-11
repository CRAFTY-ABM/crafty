package org.volante.abm.optimization;
import static java.lang.Math.pow;

import java.util.*;

import org.simpleframework.xml.Attribute;
import org.volante.abm.agent.*;
import org.volante.abm.data.Cell;

import com.moseph.modelutils.*;
import com.moseph.modelutils.Utilities.Score;


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
	
	void setupRun() 
	{ 
		checker = new ConvergenceChecker( convergence, convergenceGens, maxRuns );
	}
	

	List<PotentialAgent> doRun()
	{
		List<PotentialAgent> list = currentLandUseList();
		List<Cell> cells = new ArrayList<Cell>(region.getCells());
		double comp = 0;
		while( ! checker.isSatisfied() )
		{
			if( shuffle ) Utilities.shuffle( cells );
			for( Cell c : cells ) comp = assignBestAgent( c, c.getRegion().getPotentialAgents(), list );
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

	void applySolution( List<PotentialAgent> solution ) { applyList( solution ); }
	public String getOptimisationType() { return "Assign Best to Each Cell Randomly"; }
	
}
