package org.volante.abm.data;

import org.volante.abm.example.*;

import com.moseph.modelutils.fastdata.*;

public class ModelData
{
	public NamedIndexSet<Capital> capitals = SimpleCapital.simpleCapitals;
	public NamedIndexSet<Service> services = SimpleService.simpleServices;
	public NamedIndexSet<LandUse> landUses = SimpleLandUse.simpleLandUses;

	public DoubleMap<Capital>	capitalMap() { return new DoubleMap<Capital>( capitals ); }
	public DoubleMap<Service>	serviceMap() { return new DoubleMap<Service>( services ); }
}
