/**
 * 
 */
package org.volante.abm.update;


import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.volante.abm.data.Capital;
import org.volante.abm.data.Cell;
import org.volante.abm.data.ModelData;
import org.volante.abm.data.Region;
import org.volante.abm.schedule.RunInfo;
import org.volante.abm.serialization.ABMPersister;
import org.volante.abm.serialization.transform.IntTransformer;

import com.csvreader.CsvReader;
import com.moseph.modelutils.fastdata.DoubleMap;


/**
 * Once Reads a CSV file with factors per cell and capital which are applied every tick
 * 
 * @author Sascha Holzhauer
 * 
 */
public class CsvLinearCapitalUpdater extends AbstractUpdater {

	protected class CellCapitalData {
		int x;
		int y;

		// Map<Capital, Double> factors;
		DoubleMap<Capital> operands;

		public CellCapitalData(DoubleMap<Capital> factors, int x, int y) {
			this.operands = factors;
			this.x = x;
			this.y = y;
		}

		protected void apply(Region r) {
			Cell c = r.getCell(x, y);

			if (c == null) // Complain if we couldn't find it - implies there's data that doesn't line up!
			{
				log.warn("Update for unknown cell:" + x + ", " + y);
			} else {

				DoubleMap<Capital> adjusted = r.getModelData().capitalMap();
				c.getBaseCapitals().copyInto(adjusted);

				for (Capital cap : operands.getKeySet()) {
					adjusted.putDouble(cap, adjusted.getDouble(cap) + operands.get(cap));
				}
				c.setBaseCapitals(adjusted);
			}
		}
	}

	@Element(required = true)
	String operandsCsvFilename = "";

	@Attribute(required = false)
	String X_COL = "X";

	@Attribute(required = false)
	String Y_COL = "Y";

	@Element(required = false)
	IntTransformer xTransformer = null;

	@Element(required = false)
	IntTransformer yTransformer = null;

	Set<CellCapitalData> cellCapitalData = new HashSet<>();

	public void initialise(ModelData data, RunInfo info, Region extent) throws Exception {
		super.initialise(data, info, extent);
		ABMPersister p = ABMPersister.getInstance();
		CsvReader csvReader = p.getCSVReader(this.operandsCsvFilename, region.getPeristerContextExtra());

		// Assume we've got the CSV file, and we've read the headers in
		while (csvReader.readRecord()) {
			// For each entry
			DoubleMap<Capital> adjusted = data.capitalMap();
			for (Capital c : data.capitals) // Set each capital in turn
			{
				String cap = csvReader.get(c.getName());
				if (!(cap == null || cap.equals(""))) // It's possible the file doesn't have all baseCapitals in
				{
					double val = Double.parseDouble(cap);
					adjusted.putDouble(c, val);
				} else {
					adjusted.putDouble(c, 1.0);
				}
			}
			
			// TODOD check for empty row (last one)
			int x = Integer.parseInt(csvReader.get(X_COL));
			if (xTransformer != null) {
				x = xTransformer.transform(x);
			}

			int y = Integer.parseInt(csvReader.get(Y_COL));
			if (yTransformer != null) {
				y = yTransformer.transform(y);
			}
			
			cellCapitalData.add(new CellCapitalData(adjusted, x, y));
		}
	}

	/**
	 * Use the csv file to set the capital levels for the cells
	 * 
	 * @param file
	 * @throws IOException
	 */
	void applyFactors() {
		// Assume we've got the CSV file, and we've read the headers in
		for (CellCapitalData cdata : cellCapitalData) {
			cdata.apply(this.region);
		}
	}

	/**
	 * @see org.volante.abm.schedule.PrePreTickAction#prePreTick()
	 */
	@Override
	public void prePreTick() {
		applyFactors();
	}
}
