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
package org.volante.abm.agent;


import java.util.HashSet;
import java.util.Set;

import org.volante.abm.data.Cell;
import org.volante.abm.data.Region;
import org.volante.abm.data.Service;
import org.volante.abm.models.ProductionModel;

import com.moseph.modelutils.fastdata.UnmodifiableNumberMap;

import de.cesr.more.basic.agent.MoreObservingNetworkAgent;



/**
 * An interface detailing all the methods an Agent has to provide.
 *
 * @author dmrust
 *
 */
public interface Agent extends MoreObservingNetworkAgent<Agent> {

	/**
	 * Returns all the cells the agent manages
	 *
	 * @return cells the agent manages
	 */
	public Set<Cell> getCells();

	/**
	 * @return the cell that is considered as the agent's base cell
	 */
	public Cell getHomeCell();

	/**
	 * Removes the cell from the set the agent manages
	 *
	 * @param c
	 */
	public void removeCell(Cell c);

	/**
	 * Returns the agents current competitiveness. Should be free
	 *
	 * @return competitiveness
	 */
	public double getCompetitiveness();

	/**
	 * Returns the production model of this agent.
	 * 
	 * @return production model
	 */
	public ProductionModel getProductionModel();

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
	 *
	 * @param c
	 * @return unmodifiable supply map
	 */
	public UnmodifiableNumberMap<Service> supply(Cell c);

	/**
	 * 
	 * Adds the cell to the cells this agent manages. Use
	 * {@link Region#setOwnership(Agent, Cell...)} to add cells since this
	 * method is called by {@link Region#setOwnership(Agent, Cell...)}!
	 * 
	 * @param c
	 */
	public void addCell(Cell c);

	/**
	 * Returns true if this agent has lost all its cells and should be removed
	 *
	 * @return true if this agent is to remove
	 */
	public boolean toRemove();

	/**
	 * Called to remove the agent instance from the system.
	 */
	public void die();

	/**
	 * Returns the agent's ID/type
	 *
	 * @return ID
	 */
	public String getID();

	/**
	 * Return true if this agent is happy to cede to an agent with the given
	 * level of competitiveness
	 *
	 * @param c
	 * @param competitiveness
	 * @return true if an agent with the given competitiveness can take over the
	 *         given cell from this agents
	 */
	public boolean canTakeOver(Cell c, double competitiveness);

	/**
	 *
	 * Returns useful descriptive information about this agent
	 *
	 * @return info
	 */
	public String infoString();

	/**
	 * Returns the agent's current age in years
	 *
	 * @return agent's age
	 */
	public int getAge();

	/**
	 * Sets the agent's current age
	 *
	 * @param age
	 */
	public void setAge(int age);

	/**
	 * Called at the beginning of each tick to allow the agent to do any internal housekeeping
	 */
	public void tickStartUpdate();

	/**
	 * Called at the ending of each tick to allow the agent to do any internal
	 * housekeeping
	 */
	public void tickEndUpdate();

	public PotentialAgent getType();

	public void setGivingUp(double threshold);

	public double getGivingUp();

	public void setGivingIn(double threshold);

	public double getGivingIn();

	public void setRegion(Region r);

	public Region getRegion();

	/**
	 * The NOT_MANAGED agent is used for all cells without a manager
	 */
	public static Agent		NOT_MANAGED				= new Agent()
													{
														Region			r		= null;
														HashSet<Cell>	cells	= new HashSet<Cell>();

														@Override
														public Set<Cell> getCells() {
															return cells;
														}

														@Override
														public void removeCell(Cell c) {
														}

														@Override
														public double getCompetitiveness() {
															return NOT_MANAGED_COMPETITION;
														}

														@Override
														public double getGivingUp() {
															return 0;
														}

														@Override
														public double getGivingIn() {
															return 0;
														}

														@Override
														public void updateSupply() {
														}

														@Override
														public void updateCompetitiveness() {
														}

														@Override
														public void considerGivingUp() {
														}

														@Override
														public UnmodifiableNumberMap<Service> supply(
																Cell c) {
															return null;
														}

														@Override
														public void addCell(Cell c) {
														}

														@Override
														public boolean toRemove() {
															return false;
														}

														@Override
														public String getID() {
															return NOT_MANAGED_ID;
														}

														@Override
														public String toString() {
															return getID();
														}

														@Override
														public String infoString() {
															return "Not Managed...";
														}

														@Override
														public boolean canTakeOver(Cell c,
																double competitiveness) {
															return true;
														}

														@Override
														public int getAge() {
															return 0;
														}

														@Override
														public void setAge(int a) {
														}

														@Override
														public void tickStartUpdate() {
														}

														@Override
														public void tickEndUpdate() {
														}

														@Override
														public PotentialAgent getType() {
															return PotentialAgent.NOT_MANAGED_TYPE;
														}

														@Override
														public void setRegion(Region r) {
															this.r = r;
														}

														@Override
														public Region getRegion() {
															return r;
														}

														@Override
														public void die() {
															// do nothing
														}

														@Override
														public Cell getHomeCell() {
															return null;
														}

														@Override
														public ProductionModel getProductionModel() {
															return null;
														}
														
														@Override
														public void setGivingUp(double threshold) {
															// Nothing to do

														}

														@Override
														public void setGivingIn(double threshold) {
															// do nothing
														}

														@Override
														public void receiveNotification(
																de.cesr.more.basic.agent.MoreObservingNetworkAgent.NetworkObservation observation,
																Agent object) {
														}
													};

	public static String	NOT_MANAGED_ID			= "NOT MANAGED";
	public static double	NOT_MANAGED_COMPETITION	= -Double.MAX_VALUE;
}
