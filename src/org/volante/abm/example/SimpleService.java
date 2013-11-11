package org.volante.abm.example;

import org.volante.abm.data.Service;

import com.moseph.modelutils.fastdata.*;

public enum SimpleService implements Service
{
	HOUSING(0),
	TIMBER(1),
	FOOD(2),
	RECREATION(3),
	;
	
	int index;
	
	public static final NamedIndexSet<Service> simpleServices = new NamedArrayIndexSet<Service>(SimpleService.values());
	
	private SimpleService( int index )
	{
		this.index = index;
	}

	public String getName() { return toString(); }
	public int getIndex() { return index; }

}
