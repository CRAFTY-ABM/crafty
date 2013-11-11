package org.volante.abm.agent;

import java.util.*;

import org.volante.abm.data.*;

import com.moseph.modelutils.fastdata.*;

/**
 * Contains useful functionality for building agents. Covers:
 * * having an age and increasing it by 1 each year
 * * having a Region and a set of Cells
 * * knowing the current service provision level and competitiveness
 * @author dmrust
 *
 */
public abstract class AbstractAgent implements Agent
{

	int age = 0;
	protected String id = "Default";
	protected Region region;
	protected Set<Cell> cells = new HashSet<Cell>();
	Set<Cell> uCells = Collections.unmodifiableSet( cells );
	protected DoubleMap<Service> productivity;
	protected double currentCompetitiveness = 0;

	/*
	 * Generally useful methods
	 * 
	 */
	public void addCell( Cell c ) { cells.add(c); }
	public void removeCell( Cell c ) { cells.remove( c );  }
	public double getCompetitiveness() { return currentCompetitiveness; }
	public Set<Cell> getCells() { return uCells; }
	public boolean toRemove() { return cells.size() == 0; }
	public String getID() { return id; }
	public String toString() { return getID() + ":" + hashCode(); }
	public void setId( String id ) { this.id = id; }
	public void tickStartUpdate() { age++; }
	public void tickEndUpdate() {}
	public int getAge() { return age; }
	public void setAge( int a ) { age = a; }
	public void setRegion( Region r ) {region = r;}
	public Region getRegion() { return region; }
	
	public void giveUp() { region.removeAgent( this ); }
	
	/**
	 * Uses the current level of production in each Cell to update competitiveness
	 * (hence independant of the Agent)
	 */
	public void updateCompetitiveness()
	{
		double comp = 0;
		for( Cell c : cells )
			comp += region.getCompetitiveness( c );
		currentCompetitiveness = comp / cells.size();
	}

}
