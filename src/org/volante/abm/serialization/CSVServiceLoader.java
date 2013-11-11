package org.volante.abm.serialization;

import org.volante.abm.data.Service;

public class CSVServiceLoader extends NamedIndexLoader<Service>
{
	
	Service getType( String name, int index ) { return new CSVService( name, index ); }
	public static class CSVService implements Service
	{
		String name;
		int index;
		public CSVService( String name, int index )
		{
			this.name = name;
			this.index = index;
		}
		public String getName() { return name; }
		public int getIndex() { return index; }
		
		public String toString() { return name; }
	}

}
