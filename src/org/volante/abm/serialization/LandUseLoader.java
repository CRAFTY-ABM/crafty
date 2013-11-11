package org.volante.abm.serialization;

import org.volante.abm.data.*;

import com.moseph.modelutils.fastdata.NamedIndexSet;

public interface LandUseLoader
{
	NamedIndexSet<LandUse> getLandUses( ABMPersister persister );
}
