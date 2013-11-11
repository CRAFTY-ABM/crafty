package org.volante.abm.serialization;

import org.volante.abm.data.*;
import org.volante.abm.schedule.RunInfo;

import com.moseph.gis.raster.*;
import com.moseph.modelutils.serialisation.EasyPersister;

public class ABMPersister extends EasyPersister
{
	static ABMPersister instance = null;
	
	public static ABMPersister getInstance()
	{
		if( instance == null ) instance = new ABMPersister();
		return instance;
	}

	public void regionsToRaster( String filename, Regions r, CellToDouble converter, boolean writeInts ) throws Exception
	{
		Extent e = r.getExtent();
		Raster raster = new Raster( e.getMinX(), e.getMinY(), e.getMaxX(), e.getMaxY() );
		for( Cell c : r.getAllCells() ) raster.setXYValue( c.getX(), c.getY(), converter.apply( c ) );
		RasterWriter writer = new RasterWriter();
		if( writeInts ) writer.setCellFormat( RasterWriter.INT_FORMAT );
		writer.writeRaster( filename, raster );
	}
	
	public void setRegion( Regions r )
	{
		if( r != null )
			setContext( "r", r.getID() );
	}
	
	public void setRunInfo( RunInfo info )
	{
	}
	
}
