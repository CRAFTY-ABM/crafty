package org.volante.abm.output;

import org.simpleframework.xml.Attribute;
import org.volante.abm.data.*;
import org.volante.abm.serialization.*;

public abstract class RasterOutputter extends AbstractOutputter implements CellToDouble
{
	@Attribute(required=false)
	boolean perRegion = false;

	public void doOutput( Regions regions )
	{
		if( perRegion)
			for( Region r : regions.getAllRegions() )
				writeRaster( r );
		else
			writeRaster( regions );
	}
	
	public void writeRaster( Regions r ) 
	{
		String fn = tickFilename(r);
		try
		{
			outputs.runInfo.getPersister().regionsToRaster( fn, r, this, isInt() );
		} catch( Exception e )
		{
			log.error( "Couldn't write output raster '" + fn + "': " + e.getMessage(), e );
		}
	}

	public boolean isInt() { return false; }
	public String getExtension() { return "asc"; }


}
