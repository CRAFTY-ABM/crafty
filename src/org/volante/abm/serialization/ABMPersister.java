/**
 * This file is part of
 * 
 * CRAFTY - Competition for Resources between Agent Functional TYpes
 *
 * Copyright (C) 2014 School of GeoScience, University of Edinburgh, Edinburgh, UK
 * 
 * CRAFTY is free software: You can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software 
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *  
 * CRAFTY is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * School of Geoscience, University of Edinburgh, Edinburgh, UK
 * 
 */
package org.volante.abm.serialization;


import java.text.DecimalFormat;

import org.volante.abm.data.Cell;
import org.volante.abm.data.Extent;
import org.volante.abm.data.Regions;
import org.volante.abm.output.PRasterWriter;
import org.volante.abm.schedule.RunInfo;

import com.moseph.gis.raster.Raster;
import com.moseph.gis.raster.RasterWriter;
import com.moseph.modelutils.serialisation.EasyPersister;


/**
 * Note: The Raster class is not well implemented. Calling {@link Raster#getNDATA()} without a
 * previous call to {@link Raster#setNDATA(String)} may cause a segmentation fault since the object
 * it returns has not been initialised.
 * 
 * @author Dave Murray-Rust
 * @author Sascha Holzhauer
 * 
 */
public class ABMPersister extends EasyPersister {
	static ABMPersister	instance	= null;

	public static ABMPersister getInstance() {
		if (instance == null) {
			instance = new ABMPersister();
		}
		return instance;
	}
	
	public void regionsToRaster(String filename, Regions r, CellToDouble converter,
			boolean writeInts) throws Exception {
		this.regionsToRaster(filename, r, converter, writeInts, null);
	}

	public void regionsToRaster(String filename, Regions r, CellToDouble converter,
			boolean writeInts, DecimalFormat format) throws Exception {
		Extent e = r.getExtent();
		Raster raster = new Raster(e.getMinX(), e.getMinY(), e.getMaxX(), e.getMaxY());
		for (Cell c : r.getAllCells()) {
			raster.setXYValue(c.getX(), c.getY(), converter.apply(c));
		}
		PRasterWriter writer = new PRasterWriter();
		if (format != null) {
			writer.setCellFormat(format);
		} else if (writeInts) {
			writer.setCellFormat(RasterWriter.INT_FORMAT);
		}
		writer.writeRaster(filename, raster);
	}

	public void setRegion(Regions r) {
		if (r != null) {
			setContext("r", r.getID());
		}
	}

	public void setRunInfo(RunInfo info) {
	}
}
