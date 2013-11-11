package org.volante.abm.models;

import org.volante.abm.data.*;
import org.volante.abm.serialization.Initialisable;
import org.volante.abm.visualisation.*;

import com.moseph.modelutils.fastdata.*;

/**
 * Models the allocation and satisfaction of demand
 * @author dmrust
 *
 */
public interface DemandModel extends Initialisable, Displayable
{
	/**
	 * Should be called to get the level of demand in a particular cell
	 * This can include any regional demand
	 * @param c
	 * @param region
	 * @return
	 */
	public DoubleMap<Service> getDemand( Cell c );
	/**
	 * Returns the level of demand for the Region
	 * @param c
	 * @return
	 */
	public DoubleMap<Service> getDemand();
	/**
	 * The spatialised demand for a single cell
	 * @param c
	 * @return
	 */
	public DoubleMap<Service> getResidualDemand( Cell c );
	/**
	 * Returns the level of residual demand for the region
	 * @return
	 */
	public DoubleMap<Service> getResidualDemand();
	
	/**
	 * Gets the marginal utility of producing a unit of each service at the current supply levels
	 * Uses the competitiveness model, but ignores cell/agent adjustments
	 * @return
	 */
	public DoubleMap<Service> getMarginalUtilities();
	/**
	 * Called when an agent changes on a cell, to allow updating on agent changes
	 * i.e. as demand gets satisfied through agent change
	 * @param c
	 */
	public void agentChange( Cell c );
	/**
	 * Called after all agent changes have been done, and production has been updated.
	 * 
	 * A good place to recalculate residual demand etc.
	 */
	public void updateSupply();
	
	public DoubleMap<Service> getSupply();
	
	public DemandDisplay getDisplay();

	public static interface DemandDisplay extends Display {};
}
