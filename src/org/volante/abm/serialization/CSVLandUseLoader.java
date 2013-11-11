package org.volante.abm.serialization;

import org.volante.abm.data.LandUse;

public class CSVLandUseLoader extends NamedIndexLoader<LandUse>
{
	
	LandUse getType( String name, int index ) { return new CSVLandUse( name, index ); }
	public static class CSVLandUse implements LandUse
	{
		String name;
		int index;
		public CSVLandUse( String name, int index )
		{
			this.name = name;
			this.index = index;
		}
		public String getName() { return name; }
		public int getIndex() { return index; }
		
		public String toString() { return name; }
	}

}
