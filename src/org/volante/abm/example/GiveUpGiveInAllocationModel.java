package org.volante.abm.example;

import static com.moseph.modelutils.Utilities.*;
import static java.lang.Math.pow;

import java.util.*;

import org.simpleframework.xml.Attribute;
import org.volante.abm.agent.*;
import org.volante.abm.data.*;
import org.volante.abm.schedule.RunInfo;

import com.moseph.modelutils.Utilities.Score;
import com.moseph.modelutils.Utilities.ScoreComparator;

/**
 * A very simple kind of allocation. Any abandoned cells get the most
 * competitive agent assigned to them.
 * @author dmrust
 *
 */
public class GiveUpGiveInAllocationModel extends SimpleAllocationModel
{
	@Attribute(required=false)
	public int numCells = 30; // The number of cells an agent (type) can search over to find maximum competitiveness
	@Attribute(required=false)
	public int numTakeovers = 30;	// The number of times an agent (type) can search the above no. of cells
	@Attribute(required=false)
	public int probabilityExponent = 2;
	Cell perfectCell = new Cell();
	ModelData data;
	
	public void initialise( ModelData data, RunInfo info, Region r )
	{
		super.initialise( data, info, r );
		this.data = data;
		perfectCell.initialise( data, info, r );
		for( Capital c : data.capitals) perfectCell.getModifiableBaseCapitals().putDouble( c, 1 );
	};
	
	/**
	 * Creates a copy of the best performing potential agent on each empty cell
	 */
	public void allocateLand( final Region r )
	{
		super.allocateLand( r ); //Puts the best agent on any unmanaged cells
		Score<PotentialAgent> compScore = new Score<PotentialAgent>()
		{ 
			public double getScore( PotentialAgent a )
			{
				return pow( r.getCompetitiveness( a, perfectCell ), probabilityExponent );
			}
		};
		for ( int i = 0; i < numTakeovers; i++ ) 
		{
			//Resample this each time to deal with changes in supply affecting competitiveness
			Map<PotentialAgent, Double> scores = scoreMap( r.getPotentialAgents(), compScore );
			tryToComeIn( sample( scores, true ), r );
		}
	}
	
	/**
	 * Tries to create one of the given agents if it can take over a cell
	 * @param a
	 * @param r
	 */
	/*
	public void tryToComeIn( final PotentialAgent a, final Region r )
	{
		if( a == null ) return; //In the rare case that all have 0 competitiveness, a can be null
		final Agent agent = a.createAgent( r );
		Map<Cell, Double> competitiveness = scoreMap( sampleN( r.getCells(), numCells ), 
			new Score<Cell>() {
				public double getScore( Cell c ) { return r.getCompetitiveness( agent.supply( c ), c ); }
		});
		List<Cell> sorted = new ArrayList<Cell>(competitiveness.keySet());
		Collections.sort( sorted, new ScoreComparator<Cell>( competitiveness ) );
		
		
		for( Cell c : sorted )
		{
			if( competitiveness.get( c ) < a.getGivingUp() ) break;
			boolean canTake = c.getOwner().canTakeOver( c, competitiveness.get(c) );
			if( canTake )
			{
				r.setOwnership( agent, c );
				break;
			}
		}
	}
	*/
	
	public void tryToComeIn( final PotentialAgent a, final Region r )
	{
		if( a == null ) return; //In the rare case that all have 0 competitiveness, a can be null
		Map<Cell, Double> competitiveness = scoreMap( sampleN( r.getCells(), numCells ), new Score<Cell>() {
			public double getScore( Cell c ) 
			{ return r.getCompetitiveness( a, c ); }
		});
		List<Cell> sorted = new ArrayList<Cell>(competitiveness.keySet());
		Collections.sort( sorted, new ScoreComparator<Cell>( competitiveness ) );
		// For checking cells in reverse score order:
//		Collections.reverse( sorted);
		// For checking cells randomly:
		Collections.shuffle( sorted);
		for( Cell c : sorted )
		{
//			if (competitiveness.get(c) < a.getGivingUp()) return;
			if (competitiveness.get(c) > a.getGivingUp()) {
			boolean canTake = c.getOwner().canTakeOver( c, competitiveness.get(c) );
			if( canTake )
			{
				r.setOwnership( a.createAgent(r), c );
				break;
			}
			}
		}
	}


}
