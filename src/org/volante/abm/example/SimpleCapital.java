package org.volante.abm.example;

import org.volante.abm.data.Capital;

import com.moseph.modelutils.fastdata.*;

public enum SimpleCapital implements Capital
{
	HUMAN(0),
	INFRASTRUCTURE(1),
	ECONOMIC(2),
	NATURAL_GRASSLAND(3),
	NATURAL_FOREST(4),
	NATURAL_CROPS(5),
	NATURE_VALUE(6)
	;
	
	int index;
	public static final NamedIndexSet<Capital> simpleCapitals = new NamedArrayIndexSet<Capital>(SimpleCapital.values());
	
	private SimpleCapital( int index ) { this.index = index; }
	public String getName() { return toString(); }
	public int getIndex() { return index; }

}
