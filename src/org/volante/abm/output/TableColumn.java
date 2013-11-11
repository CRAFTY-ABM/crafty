package org.volante.abm.output;

import org.volante.abm.data.*;
import org.volante.abm.schedule.RunInfo;

public interface TableColumn<T>
{
	public String getHeader();
	public String getValue( T t, ModelData data, RunInfo info, Regions r );
}
