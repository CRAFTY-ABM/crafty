package org.volante.abm.serialization;

import java.io.IOException;
import java.util.*;

import org.simpleframework.xml.Attribute;

import com.csvreader.CsvReader;
import com.moseph.modelutils.fastdata.*;

public abstract class NamedIndexLoader<S extends Named & Indexed> implements DataTypeLoader<S>
{
	@Attribute(name="file")
	String file = "";
	@Attribute(name="indexed",required=false)
	boolean indexed = true;
	@Attribute(name="nameColumn",required=false)
	String nameColumn = "Name";
	@Attribute(name="indexColumn",required=false)
	String indexColumn = "Index";
	

	public NamedIndexSet<S> getDataTypes(ABMPersister persister ) throws IOException
	{
		CsvReader reader = persister.getCSVReader( file );
		int index = 0;
		List<S> datatypes = new ArrayList<S>();
		while( reader.readRecord() )
		{
			int ind = indexed ? Integer.parseInt( reader.get(indexColumn) ) : index;
			index++;
			datatypes.add(getType( reader.get(nameColumn), ind ));
		}
		return new NamedArrayIndexSet<S>( datatypes );
	}
	
	abstract S getType( String name, int index );

}
