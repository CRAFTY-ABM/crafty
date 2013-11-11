package org.volante.abm.serialization;

import org.volante.abm.data.Service;

import com.moseph.modelutils.fastdata.NamedIndexSet;

public interface ServiceLoader
{
	NamedIndexSet<Service> getServices( ABMPersister persister );
}
