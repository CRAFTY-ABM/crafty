package org.volante.abm.serialization;

import com.moseph.modelutils.fastdata.*;

public interface DataTypeLoader<T extends Named & Indexed>
{
	NamedIndexSet<T> getDataTypes(ABMPersister persister) throws Exception;
}
