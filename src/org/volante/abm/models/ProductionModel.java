package org.volante.abm.models;

import org.volante.abm.data.*;
import org.volante.abm.serialization.Initialisable;

import com.moseph.modelutils.fastdata.*;

/**
 * Part of an Agent, and used to calculate the agent's production on a given cell
 */
public interface ProductionModel extends Initialisable
{
	/**
	 * Calculates production on the given cell, puts it into the supplied NumberMap
	 * @param c
	 * @param v
	 */
	public void production( Cell c, DoubleMap<Service> v );

}
