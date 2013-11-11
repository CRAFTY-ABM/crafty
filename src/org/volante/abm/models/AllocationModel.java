package org.volante.abm.models;

import org.volante.abm.data.Region;
import org.volante.abm.serialization.Initialisable;
import org.volante.abm.visualisation.*;

/**
 * The allocation procedure deals with all land allocation in a particular region.
 * 
 * It has access to all the data - current production, demand, residuals, potential
 * agents etc. Common tasks are:
 * * allocating empty cells
 * * allowing potential agents to force out existing agents.
 * @author dmrust
 *
 */
public interface AllocationModel extends Initialisable, Displayable
{
	public void allocateLand( Region r );
	
	public AllocationDisplay getDisplay();
	
	public interface AllocationDisplay extends Display {}
}
