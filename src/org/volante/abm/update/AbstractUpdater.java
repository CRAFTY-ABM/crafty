package org.volante.abm.update;



import org.apache.log4j.Logger;
import org.volante.abm.data.*;
import org.volante.abm.schedule.RunInfo;

public abstract class AbstractUpdater implements Updater
{

	protected Logger log = Logger.getLogger( getClass() );
	protected ModelData data = null;
	protected RunInfo info = null;
	protected Region region = null;

	public void initialise( ModelData data, RunInfo info, Region extent ) throws Exception
	{
		this.data = data;
		this.info = info;
		this.region = extent;
	}



}
