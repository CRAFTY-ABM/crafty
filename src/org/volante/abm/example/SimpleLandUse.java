package org.volante.abm.example;

import org.volante.abm.data.LandUse;

import com.moseph.modelutils.fastdata.*;

public enum SimpleLandUse implements LandUse
{
	FOREST(1),
	UNKNOWN(2),
	AGRICULTURE(3),
	WATER(4)
	;
	
	int index;
	
	public static final NamedIndexSet<LandUse> simpleLandUses = new NamedArrayIndexSet<LandUse>(SimpleLandUse.values());
	
	private SimpleLandUse( int index )
	{
		this.index = index;
	}

	public String getName()
	{
		return toString();
	}

	public int getIndex()
	{
		return 0;
	}

}
