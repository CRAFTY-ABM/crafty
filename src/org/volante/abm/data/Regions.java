package org.volante.abm.data;

import org.volante.abm.agent.*;
import org.volante.abm.serialization.Initialisable;

public interface Regions extends Initialisable
{
	public String getID();
	public Iterable<Region> getAllRegions();
	public Iterable<Agent> getAllAgents();
	public Iterable<Cell> getAllCells();
	public Iterable<PotentialAgent> getAllPotentialAgents();
	public Extent getExtent();
	public int getNumCells();
}
