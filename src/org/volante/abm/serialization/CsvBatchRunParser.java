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
 * Created by Sascha Holzhauer on 15 Sep 2014
 */
package org.volante.abm.serialization;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.volante.abm.schedule.RunInfo;

import com.csvreader.CsvReader;


/**
 * NOTE: Caching is save in this static class since data identifiers are filenames which are unique
 * in the entire simulation.
 * 
 * @author Sascha Holzhauer
 * 
 */
public class CsvBatchRunParser {

	/**
	 * Logger
	 */
	static private Logger											logger			= Logger.getLogger(CsvBatchRunParser.class);

	protected static Map<String, Map<String, Map<Integer, String>>>	cachedCsvData	= new HashMap<String, Map<String, Map<Integer, String>>>();
	protected static Map<String, String>							firstColumns	= new HashMap<String, String>();

	/**
	 * @param text
	 * @param rInfo
	 * @return parsed value
	 */
	public static double parseDouble(String text, RunInfo rInfo) {
		return Double.parseDouble(getValue(text, rInfo));
	}

	/**
	 * @param text
	 * @param rInfo
	 * @return parsed value
	 */
	public static int parseInt(String text, RunInfo rInfo) {
		return Integer.parseInt(getValue(text, rInfo));
	}

	/**
	 * @param text
	 * @param rInfo
	 * @return parsed value
	 */
	public static String parseString(String text, RunInfo rInfo) {
		return getValue(text, rInfo);
	}

	/**
	 * @param text
	 * @param rInfo
	 * @return parsed value
	 */
	protected static String getValue(String text, RunInfo rInfo) {
		// <- LOGGING
		if (logger.isDebugEnabled()) {
			logger.debug("Parse expression: " + text);
		}
		// LOGGING ->

		String preText = text.substring(0, text.indexOf("@"));
		if (!text.contains(")")) {
			logger.error("Text to parse (" + text
					+ ") does not contain closing parenthesis!");
			throw new IllegalStateException("Text to parse (" + text
					+ ") does not contain closing parenthesis!");
		}
		String text2parse = text.substring(text.indexOf("@") + 2, text.indexOf(")"));
		String postText = text.substring(text.indexOf(")") + 1, text.length());
		
		String[] textParsed = text2parse.split(",");
		String filename = textParsed[0].trim();
		String secondFilename = null;

		if (filename.contains("~")) {
			secondFilename = filename.split("~")[1].trim();
			filename = filename.split("~")[0].trim();
		}


		filename = rInfo.getCsvParamBasedirCorrection() + filename;

		String colName = textParsed[1].trim();

		Map<String, Map<Integer, String>> fileMap = readCsvFile(filename, rInfo);
		Integer run = rInfo.getCurrentRun();

		if (secondFilename != null) {
			secondFilename = rInfo.getCsvParamBasedirCorrection() + secondFilename;

			Map<String, Map<Integer, String>> fileMapSec = readCsvFile(secondFilename, rInfo);
			checkCsvData(secondFilename, colName, fileMapSec, null);
			String idCol = firstColumns.get(secondFilename);

			// <- LOGGING
			if (logger.isDebugEnabled()) {
				logger.debug("\t1st Colum: " + idCol);
				logger.debug("\tID: " + fileMap.get(idCol).get(run));
				logger.debug("\t2nd Colum: " + colName);
			}
			// LOGGING ->

			checkCsvData(filename, idCol, fileMap, run);

			String returnValue = preText
					+ fileMapSec.get(colName).get(Integer.parseInt(fileMap.get(idCol).get(run)))
					+ postText;

			// <- LOGGING
			if (logger.isDebugEnabled()) {
				logger.debug("\tReturn value: " + returnValue);
			}
			// LOGGING ->

			ModelRunner.clog(colName, returnValue + " (" + textParsed[0].trim() + ")");
			return returnValue;
		} else {
			checkCsvData(filename, colName, fileMap, run);

			ModelRunner.clog(colName, preText + fileMap.get(colName).get(run) + postText + " ("
					+ textParsed[0].trim() + ")");
			return preText + fileMap.get(colName).get(run) + postText;
		}
	}

	protected static Map<String, Map<Integer, String>> readCsvFile(String filename, RunInfo rInfo) {
		if (!cachedCsvData.containsKey(filename)) {
			Map<String, Map<Integer, String>> fileMap = new HashMap<String, Map<Integer, String>>();
			CsvReader reader;
			try {
				reader = rInfo.getPersister().getCSVReader(filename);
				firstColumns.put(filename, reader.getHeader(0));

				for (String col : reader.getHeaders()) {
					fileMap.put(col, new HashMap<Integer, String>());
				}

				while (reader.readRecord()) {
					int run = 0;
					try {
						run = Integer.parseInt(reader.get(0));
					} catch (NumberFormatException e) {
						logger.error("CSV parameter file >" + filename
								+ "< has not a first column parsable to integer");
						throw new IllegalStateException("CSV parameter file >" + filename
								+ "< has not a first column parsable to integer");
					}
					for (int i = 0; i < reader.getColumnCount(); i++) {
						fileMap.get(reader.getHeaders()[i]).put(new Integer(run), reader.get(i));
					}
				}
			} catch (IOException exception) {
				exception.printStackTrace();
			}

			cachedCsvData.put(filename, fileMap);
		}
		return cachedCsvData.get(filename);
	}

	/**
	 * @param filename
	 * @param colName
	 * @param fileMap
	 * @param run
	 */
	protected static void checkCsvData(String filename, String colName,
			Map<String, Map<Integer, String>> fileMap, Integer run) {
		if (!fileMap.containsKey(colName)) {
			logger.error("CSV parameter file >" + filename + "< does not contain column >"
					+ colName + "<!");
			throw new IllegalStateException("CSV parameter file >" + filename
					+ "< does not contain column >" +
					colName + "<!");
		} else if (run != null && !fileMap.get(colName).containsKey(run)) {
			logger.error("CSV parameter file >" + filename + "< does not contain run >" + run
					+ "<!");
			throw new IllegalStateException("CSV parameter file >" + filename
					+ "< does not contain run >" +
					run + "<!");
		}
	}
}
