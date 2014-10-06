/**
 * 
 */
package org.volante.abm.institutions;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.simpleframework.xml.Element;
import org.volante.abm.data.Capital;
import org.volante.abm.data.Cell;
import org.volante.abm.data.ModelData;
import org.volante.abm.data.Region;
import org.volante.abm.example.RegionalDemandModel;
import org.volante.abm.schedule.RunInfo;

import com.moseph.modelutils.curve.Curve;
import com.moseph.modelutils.curve.LinearInterpolator;
import com.moseph.modelutils.fastdata.DoubleMap;
import com.moseph.modelutils.fastdata.UnmodifiableNumberMap;

/**
 * Reads change factors for ticks from a CSV file and adjusts cells' capital
 * levels accordingly. The given tick values are interpolated into curves
 * similar as for {@link RegionalDemandModel}.
 * 
 * @author Sascha Holzhauer
 * 
 */
public class CapitalDynamicsInstitution extends AbstractInstitution {

	/**
	 * Logger
	 */
	static private Logger logger = Logger
			.getLogger(CapitalDynamicsInstitution.class);

	/**
	 * Name of CSV file that contains per-tick capital adjustment factors
	 * relative to the base capitals.
	 */
	@Element(required = true)
	String captialAdjustmentsCSV = null;

	/**
	 * Name of column in CSV file that specifies the year a row belongs to
	 */
	@Element(required = false)
	String tickCol = "Year";

	Map<Capital, Curve>		capitalFactorCurves		= new HashMap<Capital, Curve>();

	/**
	 * @see org.volante.abm.institutions.AbstractInstitution#adjustCapitals(org.volante.abm.data.Cell)
	 */
	@Override
	public void adjustCapitals(Cell c) {
		int tick = rInfo.getSchedule().getCurrentTick();
		UnmodifiableNumberMap<Capital> baseCapitals = c.getBaseCapitals();
		DoubleMap<Capital> adjusted = c.getModifiableEffectiveCapitals();
		
		for (Capital capital : modelData.capitals) {
			if (capitalFactorCurves.containsKey(capital)) {
				adjusted.put(capital, baseCapitals.getDouble(capital)
						* capitalFactorCurves.get(capital).sample(tick));
			}
		}
	}

	/**
	 * @see org.volante.abm.institutions.AbstractInstitution#initialise(org.volante.abm.data.ModelData,
	 *      org.volante.abm.schedule.RunInfo, org.volante.abm.data.Region)
	 */
	public void initialise(ModelData data, RunInfo info, Region extent)
			throws Exception {
		super.initialise(data, info, extent);

		// <- LOGGING
		logger.info("Initialise " + this);
		// LOGGING ->

		if (captialAdjustmentsCSV != null) {
			loadCapitalFactorCurves();
		}
	}

	/**
	 * @throws IOException
	 */
	void loadCapitalFactorCurves() throws IOException {
		// <- LOGGING
		logger.info("Load capital adjustment factors from "
				+ captialAdjustmentsCSV);
		// LOGGING ->

		try {
			Map<String, LinearInterpolator> curves = rInfo.getPersister()
					.csvVerticalToCurves(captialAdjustmentsCSV, tickCol,
							modelData.capitals.names());
			for (Capital c : modelData.capitals) {
				if (curves.containsKey(c.getName())) {
					capitalFactorCurves.put(c, curves.get(c.getName()));
				}
			}
		} catch (NumberFormatException e) {
			logger.error("A required number could not be parsed from " + captialAdjustmentsCSV
					+ ". Make "
					+ "sure the CSV files contains columns " + modelData.services.names());
			throw e;
		}
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Capital Dynamics Institution";
	}
}
