package org.volante.abm.serialization;

import static org.junit.Assert.*;

import org.junit.Test;
import org.volante.abm.data.*;
import org.volante.abm.example.BasicTests;
import org.volante.abm.schedule.RunInfo;

public class WorldLoaderTest extends BasicTests
{

	@Test
	public void testLoading() throws Exception
	{
		WorldLoader loader = ABMPersister.getInstance().readXML( WorldLoader.class, "xml/MediumWorld.xml" );
		loader.initialise(new RunInfo() );
		RegionSet world = loader.getWorld();
		for( Cell c : world.getAllCells() )
			print( c.getRegionID(), c.getX(), c.getY() );
	}

}
