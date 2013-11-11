package org.volante.abm.serialization;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.simpleframework.xml.Attribute;
import org.volante.abm.data.*;
import org.volante.abm.serialization.RegionLoader.CellInitialiser;
import static java.lang.Double.*;

import com.csvreader.CsvReader;

import com.moseph.gis.raster.*;

/**
 * Reads information from a csv file into the given region
 * @author dmrust
 *
 */
public class CellRasterReader implements CellInitialiser
{
	@Attribute(name="file")
	String rasterFile;
	@Attribute(name="capital")
	String capitalName = "HUMAN";
	
	Logger log = Logger.getLogger( getClass() );

	public void initialise( RegionLoader rl ) throws Exception
	{
		ModelData data = rl.modelData;
		log.info("Loading raster for " + capitalName + " from " + rasterFile );
		Raster raster = rl.persister.readRaster( rasterFile );
		Capital capital = data.capitals.forName( capitalName );
		int cells = 0;
		for( int x = 0; x < raster.getCols(); x++ )
			for( int y = 0; y < raster.getRows(); y++ )
			{
				double val = raster.getValue( y, x );
				int xPos = raster.colToX( x );
				int yPos = raster.rowToY( y );
				if( isNaN( val )) continue;
				cells++;
				if( cells % 10000 == 0 )
				{
					log.debug("Cell: " + cells);
					Runtime r = Runtime.getRuntime();
					log.debug( String.format("Mem: Total: %d, Free: %d, Used: %d\n", r.totalMemory(), r.freeMemory(), r.totalMemory() - r.freeMemory() ));
				}
				Cell cell = rl.getCell( xPos, yPos );
				cell.getModifiableBaseCapitals().putDouble( capital, val );
			}
		
	}

}
