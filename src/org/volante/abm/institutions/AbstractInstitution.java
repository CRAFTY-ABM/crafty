package org.volante.abm.institutions;

import org.volante.abm.agent.*;
import org.volante.abm.data.*;
import org.volante.abm.schedule.RunInfo;

import com.moseph.modelutils.fastdata.*;

/**
 * AbstractInstitution - provides null implementations of all methods to provide a base for creating
 * new institutions when only some methods are necessary
 * @author dmrust
 *
 */
public class AbstractInstitution implements Institution
{
	ModelData data;
	RunInfo info;
	Region region;

	public void adjustCapitals( Cell c )
	{}

	public double adjustCompetitiveness( PotentialAgent agent, Cell location, UnmodifiableNumberMap<Service> provision, double competitiveness )
	{ return competitiveness; }

	public boolean isAllowed( PotentialAgent agent, Cell location ) { return true; }
	public void update() {}

	public void initialise( ModelData data, RunInfo info, Region extent ) throws Exception
	{
		this.data = data;
		this.info = info;
		this.region = extent;
	}

}
