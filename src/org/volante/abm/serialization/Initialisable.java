package org.volante.abm.serialization;

import org.volante.abm.data.*;
import org.volante.abm.schedule.RunInfo;

/**
 * Initialisation is there to allow objects to set themselves up. In particular
 * to load serialised data.
 * 
 * The final regions parameter is to allow regional initialisation where appropriate.
 * It should be able to be null if appropriate
 * 
 * @author dmrust
 *
 */
public interface Initialisable
{
	public void initialise( ModelData data, RunInfo info, Region extent ) throws Exception;
}
