package org.volante.abm.serialization;

import static org.junit.Assert.*;


import org.junit.Test;
import org.volante.abm.data.*;
import org.volante.abm.example.BasicTests;
import org.volante.abm.schedule.RunInfo;

import com.moseph.modelutils.fastdata.NamedIndexSet;

public class ScenarioLoadingDataTypesTest extends BasicTests
{

	@Test
	public void test() throws Exception
	{
		ScenarioLoader loader = persister.readXML( ScenarioLoader.class, "xml/datatype-test-scenario.xml" );
		loader.initialise(new RunInfo());
		NamedIndexSet<Capital> caps = loader.modelData.capitals;
		checkDataType( caps, "ECON", 3 );
		checkDataType( caps, "SOC", 2 );
		checkDataType( caps, "NAT", 1 );
		checkDataType( caps, "INF", 0 );
		
		NamedIndexSet<Service> services = loader.modelData.services;
		checkDataType( services, "FOOD", 3 );
		checkDataType( services, "NAT", 2 );
		checkDataType( services, "TIMBER", 1 );
		checkDataType( services, "INF", 0 );
		
		NamedIndexSet<LandUse> lus = loader.modelData.landUses;
		checkDataType( lus, "URBAN", 3 );
		checkDataType( lus, "AGRICULTURE", 2 );
		checkDataType( lus, "FOREST", 1 );
		checkDataType( lus, "WATER", 0 );
	}

}
