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
 * Takes all the cells in the region, and assigns the most competitive agent to each cell
 * @author dmrust
 *
 */
public class BestByCellAllocationModel extends OptimizationAllocationModel<List<PotentialAgent>>
{
	@Attribute(required=false)
	boolean shuffle = true;
	@Override
	void setupRun() { }

	@Override
	List<PotentialAgent> doRun()
	{
		List<PotentialAgent> list = currentLandUseList();
		Collection<Cell> cells = region.getCells();
		if( shuffle )
		{
			List<Cell> cs = new ArrayList<Cell>( cells );
			Utilities.shuffle(cs, region.getRandom().getURService(),
					RandomPa.RANDOM_SEED_RUN_ALLOCATION.name());
			cells = cs;
		}
		for( Cell c : cells ) {
			list.set( cellToIndex( c ), getBestAgent( c, c.getRegion().getPotentialAgents() ) );
		}
		return list;
	}

	PotentialAgent getBestAgent( Cell c, Collection<PotentialAgent> ag )
	{
		List<PotentialAgent> agents = new ArrayList<PotentialAgent>( ag );
		Utilities.shuffle(agents, region.getRandom().getURService(),
				RandomPa.RANDOM_SEED_RUN_ALLOCATION.name());
		double comp = -Double.MAX_VALUE;
		PotentialAgent best = null;
		//Map<String, Double> scores = new HashMap<String, Double>();
		for( PotentialAgent a : agents)
		{
			double score = c.getRegion().getCompetitiveness( a, c );
			//scores.put( a.getID(), score );
			if( score > comp )
			{
				comp = score;
				best = a;
			}
		}
		//System.err.println("Chose " + best.getID() +": " + scores);
		return best;
	}

	@Override
	void applySolution( List<PotentialAgent> solution ) { applyList( solution ); }
	@Override
	public String getOptimisationType() { return "Assign Best to Each Cell Randomly"; }
	
}
