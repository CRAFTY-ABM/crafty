/**
 * 
 */
package org.volante.abm.output;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.apache.log4j.Logger;

import com.moseph.gis.raster.Raster;
import com.moseph.gis.raster.RasterWriter;


/**
 * Note: The Raster class is not well implemented. Calling {@link Raster#getNDATA()} without a
 * previous call to {@link Raster#setNDATA(String)} may cause a segmentation fault since the object
 * it returns has not been initialised.
 * 
 * Based on {@link RasterWriter} (Dave Murray-Rust)
 * 
 * @author Sascha Holzhauer
 * 
 */
public class PRasterWriter {

	/**
	 * Logger
	 */
	static private Logger				logger			= Logger.getLogger(PRasterWriter.class);

	NumberFormat						cellFormat		= null;
	String								nodataString	= Raster.DEFAULT_NODATA;
	public static final DecimalFormat	INT_FORMAT		= new DecimalFormat("0");

	public void writeRaster(String filename, Raster r) throws IOException {
		r.setNDATA("null");

		File f = new File(filename);
		if (f.exists()) {
			f.delete();
		}
		if (!f.createNewFile()) {
			logger.error("Could not create file for some reason!");
		}
		String linebreak = System.getProperty("line.separator");
		FileWriter o = new FileWriter(f);
		o.write("ncols " + r.getCols());
		o.write(linebreak);
		o.write("nrows " + r.getRows());
		o.write(linebreak);
		o.write("xllcorner " + r.getXll());
		o.write(linebreak);
		o.write("yllcorner " + r.getYll());
		o.write(linebreak);
		o.write("cellsize " + r.getCellsize());
		o.write(linebreak);
		o.write("NODATA_value " + r.getNDATA());
		o.write(linebreak);

		for (double[] row : r.getData()) {
			StringBuffer b = new StringBuffer();
			for (int i = 0; i < row.length; i++) {
				if (Double.isNaN(row[i])) {
					b.append(r.getNDATA());
				} else if (cellFormat != null) {
					b.append(cellFormat.format(row[i]));
				} else {
					b.append(row[i]);
				}
				if (i < row.length - 1) {
					b.append(" ");
				}
			}
			o.write(b.toString());
		}
		o.close();
	}

	public void writeRaster(String filename, double[][] data, double xll, double yll, double size,
			String ndata) throws IOException {
		writeRaster(filename, Raster.getTempRaster(data, xll, yll, size, ndata));
	}

	public void setCellFormat(NumberFormat format) {
		cellFormat = format;
	}
}
