package org.volante.abm.serialization;

import javax.swing.*;

import org.volante.abm.schedule.*;
import org.volante.abm.serialization.*;
import org.volante.abm.visualisation.*;

import com.moseph.modelutils.Utilities;

public class ModelRunner
{
	public static void main( String[] args ) throws Exception
	{
		String filename = args.length > 0 ? args[0] : "xml/test-scenario.xml";
		String directory = args.length > 1 ? args[1] : "test-data";
		int start = args.length > 2 ? Integer.parseInt( args[2] ) : 2000;
		int end = args.length > 3 ? Integer.parseInt( args[3] ) : -1;
		String scenario = args.length > 4 ? args[4] : "TestScenario";
		int numRuns = args.length > 5 ? Integer.parseInt( args[5] ) : 1;
		System.out.printf( "File: %s, Dir: %s, Start: %d, End: %d\n", filename, directory, start, end );
		for( int i = 0; i < numRuns; i++ )
		{
			long randomSeed = args.length > 6 ? Integer.parseInt( args[6] ) : System.currentTimeMillis();
			Utilities.setSeed( randomSeed );
			//Worry about random seeds here...
			doRun( filename, directory, start, end, scenario );
		}
		if( end > start ) System.exit(0);
	}
	
	public static void doRun( String filename, String directory, int start, int end, String scenario ) throws Exception
	{
		ScenarioLoader loader = setupRun( filename, directory, start, end, scenario );
		if( end > start ) noninteractiveRun( loader, start, end );
		else interactiveRun( loader );
	}
	
	public static void noninteractiveRun( ScenarioLoader loader, int start, int end )
	{
		System.out.printf("Running from %d to %d\n", start, end );
		loader.schedule.runFromTo( start, end );
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
	
	public static ScenarioLoader setupRun( String filename, String directory, int start, int end, String scenario) throws Exception
	{
		ABMPersister p = ABMPersister.getInstance();
		p.setBaseDir( directory );
		ScenarioLoader loader = ABMPersister.getInstance().readXML(ScenarioLoader.class, filename );
		loader.startTick = start;
		loader.endTick = end;
		loader.scenario = scenario;
		loader.initialise( new RunInfo() );
		loader.schedule.setRegions( loader.regions );
		return loader;
	}

}
