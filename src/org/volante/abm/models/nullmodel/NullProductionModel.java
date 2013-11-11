package org.volante.abm.models.nullmodel;

import org.volante.abm.data.*;
import org.volante.abm.models.ProductionModel;
import org.volante.abm.schedule.RunInfo;

import com.moseph.modelutils.fastdata.DoubleMap;

public class NullProductionModel implements ProductionModel
{
	public static NullProductionModel INSTANCE = new NullProductionModel();
	public void production( Cell c, DoubleMap<Service> v )
	{
		v.clear();
	}

	public void initialise( ModelData data, RunInfo info, Region r ){};
}
