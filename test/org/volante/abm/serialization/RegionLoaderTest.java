package org.volante.abm.serialization;

import static org.junit.Assert.*;



import org.junit.Test;
import org.volante.abm.agent.PotentialAgent;
import org.volante.abm.data.*;
import org.volante.abm.example.*;
import org.volante.abm.schedule.RunInfo;

public class RegionLoaderTest extends BasicTests
{

	@Test
	public void testReadingCSV() throws Exception
	{
		RegionLoader loader = ABMPersister.getInstance().readXML( RegionLoader.class, "xml/SmallWorldRegion1.xml" );
		loader.initialise( new RunInfo() );
		Region region = loader.region;
		
		assertEquals( 10, loader.cellTable.size() );
		assertNotNull( loader.demand );
		assertNotNull( loader.allocation );
		assertNotNull( loader.competition );
		assertNotNull( region.getDemandModel() );
		assertNotNull( region.getCompetitionModel() );
		assertNotNull( region.getAllocationModel() );
		
		assertEqualMaps( loader.cellTable.get( 1, 1 ).getEffectiveCapitals(), capitals( 1, 0, 0.5, 0.5, 0, 1, 0.1 ) );
		assertEqualMaps( loader.cellTable.get( 1, 2 ).getEffectiveCapitals(), capitals( 1, 0, 0.5, 0.5, 0, 1, 0.2 ) );
		assertEqualMaps( loader.cellTable.get( 1, 3 ).getEffectiveCapitals(), capitals( 1, 0, 0.5, 0.5, 0, 1, 0.3 ) );
		assertEqualMaps( loader.cellTable.get( 2, 1 ).getEffectiveCapitals(), capitals( 1, 0, 0.5, 0.5, 0, 1, 0.4 ) );
		assertEqualMaps( loader.cellTable.get( 2, 2 ).getEffectiveCapitals(), capitals( 1, 0, 1, 0.5, 0, 1, 0.5 ) );
		assertEqualMaps( loader.cellTable.get( 2, 3 ).getEffectiveCapitals(), capitals( 1, 0, 1, 0.5, 0, 1, 0.6 ) );
		assertEqualMaps( loader.cellTable.get( 3, 1 ).getEffectiveCapitals(), capitals( 1, 0, 1, 0.5, 0, 1, 0.7 ) );
		assertEqualMaps( loader.cellTable.get( 3, 2 ).getEffectiveCapitals(), capitals( 1, 0, 1, 0.5, 1, 0, 0.8 ) );
		assertEqualMaps( loader.cellTable.get( 3, 3 ).getEffectiveCapitals(), capitals( 1, 0, 1, 0.5, 1, 0, 0.9 ) );
		assertEqualMaps( loader.cellTable.get( 4, 2 ).getEffectiveCapitals(), capitals( 1, 0, 1, 0.5, 1, 0, 1 ) );
		
		PotentialAgent ag = loader.agentsByID.get( "LowIntensityArable" );
		assertNotNull( ag );
		SimplePotentialAgentTest.testLowIntensityArableAgent( (SimplePotentialAgent) loader.agentsByID.get("LowIntensityArable") );
		SimplePotentialAgentTest.testHighIntensityArableAgent( (SimplePotentialAgent) loader.agentsByID.get("HighIntensityArable") );
		SimplePotentialAgentTest.testCommercialForestryAgent( (SimplePotentialAgent) loader.agentsByID.get("CommercialForestry") );
		
		
	}

}
