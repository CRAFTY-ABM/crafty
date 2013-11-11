package org.volante.abm.serialization;

import static org.junit.Assert.*;

import org.junit.Test;
import org.volante.abm.data.Capital;
import org.volante.abm.example.BasicTests;

import com.moseph.modelutils.fastdata.NamedIndexSet;

public class CSVCapitalLoaderTest extends BasicTests
{
	@Test
	public void testBasicLoading() throws Exception
	{
		CSVCapitalLoader testCap = persister.readXML( CSVCapitalLoader.class, "xml/TestCapitals.xml" );
		NamedIndexSet<Capital> caps = testCap.getDataTypes( persister );
		checkDataType( caps, "ECON", 3 );
		checkDataType( caps, "SOC", 2 );
		checkDataType( caps, "NAT", 1 );
		checkDataType( caps, "INF", 0 );
	}

}
