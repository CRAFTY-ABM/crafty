/**
 * 
 */
package org.volante.abm.output;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import com.moseph.gis.raster.Raster;

/**
 * @author Sascha Holzhauer
 *
 */
public class PRasterWriter {

	NumberFormat						cellFormat		= null;
	String								nodataString	= Raster.DEFAULT_NODATA;
	public static final DecimalFormat	INT_FORMAT		= new DecimalFormat("0");

	public void writeRaster(String filename, Raster r) throws IOException {
		File f = new File(filename);
		if (f.exists()) {
			f.delete();
		}
		if (!f.createNewFile()) {
			System.err.println("Could not create file for some reason!");
		}
		FileWriter o = new FileWriter(f);
		o.write("ncols " + r.getCols());
		o.write("\n");
		o.write("nrows " + r.getRows());
		o.write("\n");
		o.write("xllcorner " + r.getXll());
		o.write("\n");
		o.write("yllcorner " + r.getYll());
		o.write("\n");
		o.write("cellsize " + r.getCellsize());
		o.write("\n");
		// this hack is required since writing NULL results in some kind of segmentation fault would
		// when running with MPI on eddie:
		o.write("NODATA_value " + r.getNDATA() != null ? r.getNDATA() : "null");

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
