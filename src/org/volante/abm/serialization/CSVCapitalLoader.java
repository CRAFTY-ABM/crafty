package org.volante.abm.serialization;

import org.volante.abm.data.Capital;

public class CSVCapitalLoader extends NamedIndexLoader<Capital>
{
	
	Capital getType( String name, int index ) { return new CSVCapital( name, index ); }
	public static class CSVCapital implements Capital
	{
		String name;
		int index;
		public CSVCapital( String name, int index )
		{
			this.name = name;
			this.index = index;
		}
		public String getName() { return name; }
		public int getIndex() { return index; }
		
		public String toString() { return name; }
		
	}

}
