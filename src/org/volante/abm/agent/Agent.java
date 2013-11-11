package org.volante.abm.agent;

import java.util.*;

import org.volante.abm.data.*;

import com.moseph.modelutils.fastdata.UnmodifiableNumberMap;

/**
 * An interface detailing all the methods an Agent has to provide
 * @author dmrust
 *
 */
public interface Agent
{
	/**
	 * Returns all the cells the agent manages
	 * @return
	 */
	public Set<Cell> getCells();
	

	
	/**
	 * Removes the cell from the set the agent manages
	 * @param c
	 */
	public void removeCell( Cell c );
	/**
	 * Returns the agents current competitiveness. Should be free
	 * @return
	 */
	public double getCompetitiveness();
	/**
	 * Updates the agent's competitiveness, in response to demand changes etc.
	 */
	public void updateCompetitiveness();
	/**
	 * Recalculates the services this agent can supply
	 */
	public void updateSupply();
	/**
	 * Asks this agent if it wants to give up
	 */
	public void considerGivingUp();
	/**
	 * Returns what this agent could supply on the given cell
	 * @param c
	 * @return
	 */
	public UnmodifiableNumberMap<Service> supply( Cell c );
	/**
	 * Adds the cell to the cells this agent manages
	 * @param c
	 */
	public void addCell( Cell c );
	/**
	 * Returns true if this agent has lost all its cells and should be removed
	 * @return
	 */
	public boolean toRemove();
	/**
	 * Returns the agent's ID/type
	 * @return
	 */
	public String getID();
	/**
	 * Return true if this agent is happy to cede to an agent with the given level of competitiveness
	 * @param c
	 * @param incoming
	 * @return
	 */
	public boolean canTakeOver( Cell c, double competitiveness ); 
	
	/**
	 * 
	 * Returns useful descriptive information about this agent
	 * @return
	 */
	public String infoString();
	
	/**
	 * Returns the agent's current age in years
	 * @return
	 */
	public int getAge();
	
	/**
	 * Sets the agent's current age
	 * @param age
	 */
	public void setAge( int age );
	
	/**
	 * Called at the beginning of each tick to allow the agent to do any internal housekeeping
	 */
	public void tickStartUpdate();
	
	/**
	 * Called at the beginning of each tick to allow the agent to do any internal housekeeping
	 */
	public void tickEndUpdate();
	
	public PotentialAgent getType();
	
	public double getGivingUp();
	
	public double getGivingIn();
	
	public void setRegion( Region r );
	public Region getRegion();
	
	/**
	 * The NOT_MANAGED agent is used for all cells without a manager
	 */
	public static Agent NOT_MANAGED = new Agent() 
	{
		Region r = null;
		HashSet<Cell> cells = new HashSet<Cell>();
		public Set<Cell> getCells() { return cells; }
		public void removeCell( Cell c ) { }
		public double getCompetitiveness() { return NOT_MANAGED_COMPETITION; }
		public double getGivingUp() {return 0;}
		public double getGivingIn() {return 0;}
		public void updateSupply() {}
		public void updateCompetitiveness() {}
		public void considerGivingUp() {}
		public UnmodifiableNumberMap<Service> supply( Cell c ) { return null; }
		public void addCell( Cell c ) {}
		public boolean toRemove() { return false; }
		public String getID() { return NOT_MANAGED_ID; }
		public String toString() { return getID(); }
		public String infoString() { return "Not Managed..."; }
		public boolean canTakeOver( Cell c, double competitiveness ) { return true; }
		public int getAge() { return 0; }
		public void setAge( int a ) {}
		public void tickStartUpdate() {}
		public void tickEndUpdate() {}
		public PotentialAgent getType() { return PotentialAgent.NOT_MANAGED_TYPE; }
		public void setRegion( Region r ) {this.r=r;}
		public Region getRegion() { return r; }
	};
	
	public static String NOT_MANAGED_ID = "NOT MANAGED";
	public static double NOT_MANAGED_COMPETITION = -Double.MAX_VALUE;



}
