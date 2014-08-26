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

import javax.swing.BoxLayout;
import javax.swing.JFrame;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.log4j.Logger;
import org.volante.abm.param.RandomPa;
import org.volante.abm.schedule.RunInfo;
import org.volante.abm.schedule.ScheduleThread;
import org.volante.abm.visualisation.ScheduleControls;
import org.volante.abm.visualisation.TimeDisplay;

import de.cesr.parma.core.PmParameterManager;


public class ModelRunner
{

	/**
	 * Logger
	 */
	static private Logger logger = Logger.getLogger(ModelRunner.class);

	public static void main( String[] args ) throws Exception
	{
		CommandLineParser parser = new BasicParser();
		CommandLine cmd = parser.parse(manageOptions(), args);

		if (cmd.hasOption('h')) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("CRAFTY", manageOptions());
			System.exit(0);
		}

		boolean interactive = cmd.hasOption("i");

		String filename = cmd.hasOption("f") ? cmd.getOptionValue('f') : "xml/test-scenario.xml";
		String directory = cmd.hasOption("d") ? cmd.getOptionValue('d') : "test-data";

		int start = cmd.hasOption("s") ? Integer.parseInt(cmd.getOptionValue('s'))
				: Integer.MIN_VALUE;
		int end = cmd.hasOption("e") ? Integer.parseInt(cmd.getOptionValue('e'))
				: Integer.MIN_VALUE;

		int numRuns = cmd.hasOption("n") ? Integer.parseInt(cmd.getOptionValue('n')) : 1;
		int startRun = cmd.hasOption("sr") ? Integer.parseInt(cmd.getOptionValue("sr")) : 0;

		int numOfRandVariation = cmd.hasOption("r") ? Integer.parseInt(cmd.getOptionValue('r')) : 1;

		logger.info(String.format("File: %s, Dir: %s, Start: %s, End: %s\n", filename, directory,
				(start == Integer.MIN_VALUE ? "<ScenarioFile>" : start),
				(end == Integer.MIN_VALUE ? "<ScenarioFile>" : end)));

		if (end < start) {
			logger.error("End tick must not be larger than start tick!");
			System.exit(0);
		}

		if (startRun > numRuns) {
			logger.error("StartRun must not be larger than number of runs!");
			System.exit(0);
		}

		for (int i = startRun; i < numRuns; i++) {
			for (int j = 0; j < numOfRandVariation; j++) {
				int randomSeed = cmd.hasOption('o') ? (j + Integer
						.parseInt(cmd.getOptionValue('o')))
						: (int) System
								.currentTimeMillis();
				logger.info("Run " + i + " (of " + numRuns + ") with random seed " + randomSeed);
				PmParameterManager.getInstance(null).setParam(RandomPa.RANDOM_SEED, randomSeed);

				// Worry about random seeds here...
				RunInfo rInfo = new RunInfo();
				rInfo.setNumRuns(numRuns);
				rInfo.setNumRandomVariations(numOfRandVariation);
				rInfo.setCurrentRun(i);
				rInfo.setCurrentRandomSeed(randomSeed);
				doRun(filename, directory, start, end, rInfo, interactive);
			}
		}
	}

	public static void doRun(String filename, String directory, int start,
			int end, RunInfo rInfo, boolean interactive) throws Exception
	{
		ScenarioLoader loader = setupRun(filename, directory, start, end, rInfo);
		if (interactive) {
			interactiveRun(loader);
		} else {
			noninteractiveRun(loader, start == Integer.MIN_VALUE ? loader.startTick : start,
					end == Integer.MIN_VALUE ? loader.endTick : end);
		}
	}

	public static void noninteractiveRun( ScenarioLoader loader, int start, int end )
	{
		logger.info(String.format("Running from %s to %s\n",
				(start == Integer.MIN_VALUE ? "<ScenarioFile>" : start + ""),
				(end == Integer.MIN_VALUE ? "<ScenarioFile>" : end + "")));
		if (end != Integer.MIN_VALUE) {
			if (start != Integer.MIN_VALUE) {
				loader.schedule.runFromTo(start, end);
			} else {
				loader.schedule.runUntil(end);
				loader.schedule.finish();
			}
		} else {
			loader.schedule.run();
			loader.schedule.finish();
		}
	}

	public static void interactiveRun( ScenarioLoader loader )
	{
		logger.info("Setting up interactive run");
		ScheduleThread thread = new ScheduleThread( loader.schedule );
		thread.start();
		JFrame controls = new JFrame();
		TimeDisplay td = new TimeDisplay( loader.schedule );
		ScheduleControls sc = new ScheduleControls( loader.schedule );
		controls.getContentPane().setLayout( new BoxLayout( controls.getContentPane(), BoxLayout.Y_AXIS ) );
		controls.add( td );
		controls.add( sc );
		controls.pack();
		controls.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
		controls.setVisible( true );
	}

	public static ScenarioLoader setupRun(String filename, String directory,
			int start, int end, RunInfo rInfo) throws Exception
	{
		ABMPersister p = ABMPersister.getInstance();

		p.setBaseDir( directory );
		ScenarioLoader loader = ABMPersister.getInstance().readXML(ScenarioLoader.class, filename);
		loader.setRunID(rInfo.getCurrentRun() + "-" + rInfo.getCurrentRandomSeed());
		loader.initialise(rInfo);
		loader.schedule.setRegions( loader.regions );
		return loader;
	}

	@SuppressWarnings("static-access")
	protected static Options manageOptions() {
		Options options = new Options();

		options.addOption(OptionBuilder.withDescription("Display usage")
				.withLongOpt("help")
				.isRequired(false)
				.create("h"));

		options.addOption(OptionBuilder.withDescription("Interactive mode?")
				.withLongOpt("interactive")
				.isRequired(false)
				.create("i"));

		options.addOption(OptionBuilder.withArgName("dataDirectory")
				.hasArg()
				.withDescription("Location of data directory")
				.withLongOpt("directory")
				.isRequired(false)
				.create("d"));

		options.addOption(OptionBuilder.withArgName("scenarioFilename")
				.hasArg()
				.withDescription("Location and name of scenario file relative to directory")
				.withLongOpt("filename")
				.isRequired(false)
				.create("f"));

		options.addOption(OptionBuilder.withArgName( "startTick" )
				.hasArg()
				.withDescription("Start tick of simulation")
				.withType(Integer.class)
				.withLongOpt("start")
				.isRequired(false)
				.create("s"));

		options.addOption(OptionBuilder.withArgName("endTick")
				.hasArg()
				.withDescription("End tick of simulation")
				.withType(Integer.class)
				.withLongOpt("end")
				.isRequired(false)
				.create("e"));

		options.addOption(OptionBuilder.withArgName("numOfRuns")
				.hasArg()
				.withDescription("Number of runs with distinct configuration")
				.withType(Integer.class)
				.withLongOpt("runs")
				.isRequired(false)
				.create("n"));

		options.addOption(OptionBuilder.withArgName("startRun")
				.hasArg()
				.withDescription("Number of run to start with")
				.withType(Integer.class)
				.withLongOpt("startRun")
				.isRequired(false)
				.create("sr"));

		options.addOption(OptionBuilder.withArgName("numOfRandVariation")
				.hasArg()
				.withDescription("Number of runs of each configuration with distinct random seed)")
				.withType(Integer.class)
				.withLongOpt("randomVariations")
				.isRequired(false)
				.create("r"));

		options.addOption(OptionBuilder.withArgName("offset")
				.hasArg()
				.withDescription("Random seed offset")
				.withType(Integer.class)
				.withLongOpt("randomseedoffset")
				.isRequired(false)
				.create("o"));

		return options;
	}
}
