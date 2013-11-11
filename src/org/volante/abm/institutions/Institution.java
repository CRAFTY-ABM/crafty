package org.volante.abm.institutions;

import org.simpleframework.xml.Root;
import org.volante.abm.agent.*;
import org.volante.abm.data.*;
import org.volante.abm.serialization.Initialisable;

import com.moseph.modelutils.fastdata.*;

@Root
public interface Institution extends Initialisable
{
	/**
	 * Allows the institution to adjust the effective capitals present in the cell
	 * @param c
	 */
	public void adjustCapitals( Cell c );
	/**
	 * When given an agent, a cell and the level of (potential) provision, adjusts the competitiveness level
	 * Must be able to deal with the agent being null if the cell is unoccupied.
	 * @param agent
	 * @param location
	 * @param provision
	 * @param competitiveness
	 * @return
	 */
	public double adjustCompetitiveness( PotentialAgent agent, Cell location, UnmodifiableNumberMap<Service> provision, double competitiveness );
	
	/**
	 * Determines whether this agent is forbidden from occupying that cell according to this institution
	 * @param agent
	 * @param location
	 * @return
	 */
	public boolean isAllowed( PotentialAgent agent, Cell location );

	/**
	 * Called at the start of each tick to allow this institution to perform any internal updates necessary.
	 */
	public void update();
}
