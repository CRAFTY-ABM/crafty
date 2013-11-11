package org.volante.abm.visualisation;

import org.volante.abm.data.*;
import org.volante.abm.example.*;
import org.volante.abm.models.DemandModel.DemandDisplay;
import org.volante.abm.schedule.RunInfo;

public class StaticPerCellDemandDisplay extends AbstractDisplay implements DemandDisplay
{
	StaticPerCellDemandModel model;
	
	public StaticPerCellDemandDisplay( StaticPerCellDemandModel model )
	{
		this.model = model;
	}

	public void update()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void initialise( ModelData data, RunInfo info, Regions region ) throws Exception
	{
		// TODO Auto-generated method stub

	}

}
