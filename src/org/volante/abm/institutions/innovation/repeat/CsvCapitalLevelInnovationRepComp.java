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
 * Created by Sascha Holzhauer on 4 Dec 2014
 */
package org.volante.abm.institutions.innovation.repeat;


import java.util.HashMap;
import java.util.Map;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.volante.abm.data.ModelData;
import org.volante.abm.data.Region;
import org.volante.abm.institutions.innovation.Innovation;
import org.volante.abm.institutions.innovation.RepeatingCapitalLevelInnovation;
import org.volante.abm.schedule.RunInfo;
import org.volante.abm.serialization.Initialisable;

import com.csvreader.CsvReader;


/**
 * NOTE: The effect adjustments only take effect when the innovation is renewed by an institution
 * which in turn usually considers the innovation's renewal interval!
 * 
 * @author Sascha Holzhauer
 *
 */
public class CsvCapitalLevelInnovationRepComp extends AbstractInnovationRepComp
		implements
		Initialisable {

	@Element(name = "effectAdjustmentsCsvFile", required = true)
	protected String effectAdjustmentsCsvFile = "";

	@Attribute(name = "colnameTick", required = false)
	protected String colnameTick = "Tick";

	@Attribute(name = "colnameFactor", required = false)
	protected String colnameFactor = "Factor";

	protected Map<Integer, Double> effects;
	protected RunInfo rInfo;

	/**
	 * @see org.volante.abm.serialization.Initialisable#initialise(org.volante.abm.data.ModelData,
	 *      org.volante.abm.schedule.RunInfo, org.volante.abm.data.Region)
	 */
	@Override
	public void initialise(ModelData data, RunInfo info, Region extent) throws Exception {
		this.rInfo = info;
		CsvReader reader = info.getPersister().getCSVReader(effectAdjustmentsCsvFile);
		effects = new HashMap<Integer, Double>();

		while (reader.readRecord()) {
			effects.put(new Integer(reader.get(colnameTick)),
					new Double(reader.get(colnameFactor)));
		}
	}

	/**
	 * @see org.volante.abm.institutions.innovation.repeat.InnovationRepComp#adjustRenewedInnovation(org.volante.abm.institutions.innovation.Innovation)
	 */
	@Override
	public <InnovationType extends Innovation> InnovationType adjustRenewedInnovation(
			InnovationType innovation) {
		if (innovation instanceof RepeatingCapitalLevelInnovation) {
			RepeatingCapitalLevelInnovation repInno = ((RepeatingCapitalLevelInnovation) innovation);

			repInno.setEffectOnCapitalFactor(effects.get(new Integer(
					rInfo.getSchedule()
								.getCurrentTick())));
		} else {
			throw new IllegalStateException(
					"Passed innovation is not of type RepeatingCapitalLevelInnovation!");
		}
		return innovation;
	}
}
