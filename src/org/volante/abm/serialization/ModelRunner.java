package org.volante.abm.serialization;

import javax.swing.BoxLayout;
import javax.swing.JFrame;

import mpi.MPI;

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
		logger.info("Start CRAFTY parallel");
		String[] realArgs = MPI.Init(args);

		CommandLineParser parser = new BasicParser();
		CommandLine cmd = parser.parse(manageOptions(), realArgs);

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

		int numRuns = cmd.hasOption("r") ? Integer.parseInt(cmd.getOptionValue('r')) : 1;

		logger.info(String.format("File: %s, Dir: %s, Start: %s, End: %s\n", filename, directory,
				(start == Integer.MIN_VALUE ? "<ScenarioFile>" : start),
				(end == Integer.MIN_VALUE ? "<ScenarioFile>" : end)));

		for( int i = 0; i < numRuns; i++ )
		{
			int randomSeed = cmd.hasOption('o') ? (i + Integer.parseInt(cmd.getOptionValue('o')))
					: (int) System
					.currentTimeMillis();
			PmParameterManager.getInstance(null).setParam(RandomPa.RANDOM_SEED, randomSeed);

			//Worry about random seeds here...
			doRun(filename, directory, start, end, randomSeed, interactive);
		}
		if( end > start ) {
			System.exit(0);
		}
	}
	
	public static void doRun(String filename, String directory, int start,
			int end, long randomSeed, boolean interactive) throws Exception
	{
		ScenarioLoader loader = setupRun(filename, directory, start, end, randomSeed);
		if (interactive) {
			interactiveRun(loader);
		} else {
			noninteractiveRun(loader, start, end);
		}
	}
	
	public static void noninteractiveRun( ScenarioLoader loader, int start, int end )
	{
		System.out.printf("Running from %d to %d\n", start, end );
		if (end != Integer.MIN_VALUE) {
			if (start != Integer.MIN_VALUE) {
				loader.schedule.runFromTo(start, end);
			} else {
				loader.schedule.runUntil(end);
			}
		} else {
			loader.schedule.run();
		}
	}
	
	public static void interactiveRun( ScenarioLoader loader )
	{
		System.out.println("Setting up interactive run");
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
			int start, int end, long randomSeed) throws Exception
	{
		ABMPersister p = ABMPersister.getInstance();

		p.setBaseDir( directory );
		ScenarioLoader loader = ABMPersister.getInstance().readXML(ScenarioLoader.class, filename);
		loader.setRunID("" + randomSeed);
		loader.initialise( new RunInfo() );
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
				.withDescription("Number of runs (with distinct random seed)")
				.withType(Integer.class)
				.withLongOpt("runs")
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
