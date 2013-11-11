package org.volante.abm.serialization;

import java.io.IOException;
import java.util.Arrays;

import org.apache.log4j.Logger;
import org.simpleframework.xml.Attribute;
import org.volante.abm.data.*;
import org.volante.abm.serialization.RegionLoader.CellInitialiser;

import com.csvreader.CsvReader;

/**
 * Reads information from a csv file into the given region
 * @author dmrust
 *
 */
public class CellCSVReader implements CellInitialiser
{
	@Attribute
	String csvFile;
	@Attribute(required=false)
	String agentColumn = "Agent";
	@Attribute(required=false)
	String xColumn = "x";
	@Attribute(required=false)
	String yColumn = "y";
	
	Logger log = Logger.getLogger( getClass() );

	public void initialise( RegionLoader rl ) throws Exception
	{
		ModelData data = rl.modelData;
		if( ! rl.persister.csvFileOK( "RegionLoader", csvFile, xColumn, yColumn ) ) return;
		log.info("Loading cell CSV from " + csvFile );
		CsvReader reader = rl.persister.getCSVReader( csvFile );
		if( ! Arrays.asList( reader.getHeaders()).contains( agentColumn ))
			log.info( "No Agent Column found in CSV file: " + rl.persister.getFullPath( csvFile ) );
		while( reader.readRecord() )
		{
			int x = Integer.parseInt( reader.get("x") );
			int y = Integer.parseInt( reader.get("y") );
			Cell c = rl.getCell( x, y );
			for( Capital cap : data.capitals )
			{
				String s = reader.get( cap.getName() );
				if( s != null )
					c.getModifiableBaseCapitals().putDouble( cap, Double.parseDouble(s) );
			}
			String ag = reader.get(agentColumn);
			if( ag != null ) rl.setAgent( c, ag);
		}
	}

}
