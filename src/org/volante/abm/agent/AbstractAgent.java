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


import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.volante.abm.data.Cell;
import org.volante.abm.data.Region;
import org.volante.abm.data.Service;

import com.moseph.modelutils.fastdata.DoubleMap;


/**
 * Contains useful functionality for building agents. Covers: * having an age and increasing it by 1
 * each year * having a Region and a set of Cells * knowing the current service provision level and
 * competitiveness
 * 
 * @author dmrust
 * 
 */
public abstract class AbstractAgent implements Agent {

	int								age						= 0;
	protected String				id						= "Default";
	protected Region				region;
	protected Set<Cell>				cells					= new HashSet<Cell>();
	Set<Cell>						uCells					= Collections.unmodifiableSet(cells);
	protected DoubleMap<Service>	productivity;
	protected double				currentCompetitiveness	= 0;

	/*
	 * Generally useful methods
	 */
	@Override
	public void addCell(Cell c) {
		cells.add(c);
	}

	@Override
	public void removeCell(Cell c) {
		cells.remove(c);
	}

	@Override
	public double getCompetitiveness() {
		return currentCompetitiveness;
	}

	@Override
	public Set<Cell> getCells() {
		return uCells;
	}

	@Override
	public boolean toRemove() {
		return cells.size() == 0;
	}

	@Override
	public String getID() {
		return id;
	}

	@Override
	public String toString() {
		return getID() + ":" + hashCode();
	}

	public void setId(String id) {
		this.id = id;
	}

	@Override
	public void tickStartUpdate() {
		age++;
	}

	@Override
	public void tickEndUpdate() {
	}

	@Override
	public int getAge() {
		return age;
	}

	@Override
	public void setAge(int a) {
		age = a;
	}

	@Override
	public void setRegion(Region r) {
		region = r;
	}

	@Override
	public Region getRegion() {
		return region;
	}

	public void giveUp() {
		region.removeAgent(this);
		this.die();
	}

	/**
	 * @see org.volante.abm.agent.Agent#die()
	 */
	@Override
	public void die() {
		// nothing to do
	}

	/**
	 * Uses the current level of production in each Cell to update competitiveness (hence
	 * independant of the Agent)
	 */
	@Override
	public void updateCompetitiveness() {
		double comp = 0;
		for (Cell c : cells) {
			comp += region.getCompetitiveness(c);
		}
		currentCompetitiveness = comp / cells.size();
	}

}
