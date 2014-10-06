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
 */
package org.volante.abm.serialization;


import org.apache.log4j.Logger;
import org.rosuda.JRI.REXP;
import org.rosuda.JRI.Rengine;
import org.volante.abm.schedule.RunInfo;


/**
 * @author Sascha Holzhauer
 *
 */
public class BatchRunParser {

	/**
	 * Logger
	 */
	static private Logger	logger	= Logger.getLogger(BatchRunParser.class);

	public static double parseDouble(String text, RunInfo rInfo) {
		if (text.contains("@")) {
			return CsvBatchRunParser.parseDouble(text, rInfo);

		} else if (text.contains("|")) {
			int run = rInfo.getCurrentRun();
			String[] values = text.split("\\|");
			return Double.parseDouble(values[run]);

		} else if (text.contains("(")) {
			int run = rInfo.getCurrentRun();
			double[] values;

			// parse parameters for all run configurations:
			values = parseRDoubleArray(text, rInfo);

			return values[run];

		} else {
			return Double.parseDouble(text);
		}
	}

	public static int parseInt(String text, RunInfo rInfo) {
		if (text.startsWith("@")) {
			return CsvBatchRunParser.parseInt(text, rInfo);

		} else if (text.contains("|")) {
			int run = rInfo.getCurrentRun();
			String[] values = text.split("\\|");
			return Integer.parseInt(values[run]);

		} else if (text.contains("(")) {
			int run = rInfo.getCurrentRun();
			int[] values;

			// parse parameters for all run configurations:
			values = parseRIntArray(text, rInfo);

			return values[run];

		} else {
			return Integer.parseInt(text);
		}
	}

	public static String parseString(String text, RunInfo rInfo) {
		if (text.contains("@")) {
			return CsvBatchRunParser.parseString(text, rInfo);

		} else if (text.contains("|")) {
			int run = rInfo.getCurrentRun();
			String[] values = text.split("\\|");
			return values[run];

		} else if (text.contains("(")) {
			int run = rInfo.getCurrentRun();
			String[] values;

			// parse parameters for all run configurations:
			values = parseRStringArray(text, rInfo);

			return values[run];

		} else {
			return text;
		}
	}

	private static double[] parseRDoubleArray(String text, RunInfo rInfo) {
		Rengine re = RService.getRengine(rInfo);

		REXP result;

		result = re.eval(text);
		logger.info("Result: " + result);
		return result.asDoubleArray();
	}

	private static int[] parseRIntArray(String text, RunInfo rInfo) {
		Rengine re = RService.getRengine(rInfo);

		REXP result;

		result = re.eval(text);
		logger.info("Result: " + result);
		return result.asIntArray();
	}

	private static String[] parseRStringArray(String text, RunInfo rInfo) {
		Rengine re = RService.getRengine(rInfo);

		REXP result;

		result = re.eval(text);
		logger.info("Result: " + result);
		return result.asStringArray();
	}
}
