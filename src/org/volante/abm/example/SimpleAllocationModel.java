package org.volante.abm.example;

import java.util.*;


import org.simpleframework.xml.Root;
import org.volante.abm.agent.PotentialAgent;
import org.volante.abm.data.*;
import org.volante.abm.models.*;
import org.volante.abm.schedule.RunInfo;
import org.volante.abm.visualisation.SimpleAllocationDisplay;

import com.moseph.modelutils.fastdata.UnmodifiableNumberMap;


import static com.moseph.modelutils.Utilities.*;

/**
 * A very simple kind of allocation. Any abandoned cells get the most
 * competitive agent assigned to them.
 * @author dmrust
 *
 */
@Root
public class SimpleAllocationModel implements AllocationModel
{
	public void initialise( ModelData data, RunInfo info, Region r ){};
	
	/**
	 * Creates a copy of the best performing potential agent on each empty cell
	 */
	public void allocateLand( Region r )
	{
		for( Cell c : new ArrayList<Cell>( r.getAvailable() ) ) {
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
			if( s > max )
			{
				if (s > a.getGivingUp()) {
					max = s;
					p = a;
				}
			}
		}
		//Only create agents if their competitiveness is good enough
		if( p != null ) {
			r.setOwnership( p.createAgent(r), c );
		}
	}

	public AllocationDisplay getDisplay()
	{
		return new SimpleAllocationDisplay();
	}
	

	
}
