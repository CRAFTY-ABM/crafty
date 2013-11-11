package org.volante.abm.output;

import java.io.File;

import java.util.*;

import org.apache.log4j.Logger;
import org.simpleframework.xml.*;
import org.volante.abm.data.*;
import org.volante.abm.schedule.*;
import org.volante.abm.serialization.Initialisable;

/**
 * Manages creation of output of a variety of types.
 * 
 * Uses substitution patterns to do filenames. There is one pattern for:
 * - outputDirectory
 * - files without time in their name (filePattern)
 * - files with the year in their name (yearlyPattern)
 * - files with the day and year in their name (dailyPattern)
 * 
 * Substitutions are done with % signs, with each letter being a different
 * variable:
 * - %o = output name
 * - %s = scenario name
 * - %i = run ID (i.e. number within a batch run, may be timestamp unless otherwise set)
 * - %u = timestamp (almost certainly unique)
 * - %y = current tick/year
 * - %r = current region
 * 
 * As an example, a file pattern of "%s-%o-%y" and an outputDirectory of "output/%s-%i"
 * might create a file: "output/A1-3/A1-parcels-2034.asc" (for a raster)
 * @author dmrust
 *
 */
public class Outputs implements Initialisable
{
	@Attribute( required=false, name="outputDirectory" )
	String outputDirectoryPattern = "output";
	@Attribute( required=false)
	String filePattern = "%n-%i-%o";
	@Attribute( required=false)
	String tickPattern = "%n-%i-%o-%t";
	@Attribute( required=false )
	String outputsFile = "";
	@ElementList(inline=true, required=false,entry="output")
	List<Outputter> outputs = new ArrayList<Outputter>();
	@Attribute( required = false)
	boolean clearExistingFiles = true;
	Logger log = Logger.getLogger( getClass() );
	protected RunInfo runInfo;
	protected ModelData modelData;
	List<CloseableOutput> outputsToClose = new ArrayList<CloseableOutput>();

	public void initialise( ModelData data, RunInfo info, Region extent ) throws Exception
	{
		runInfo = info;
		modelData = data;
		//Setup timestamp for output
		runInfo.getPersister().setContext( "u", System.currentTimeMillis() + "" );
		//If there's no runID set, then use a timestamp to ensure different directories
		if( runInfo.getRunID() == null || runInfo.getRunID().equals("") )
			runInfo.getPersister().setContext( "i", System.currentTimeMillis() + "" );
		
		if( outputsFile != null && outputsFile.length() > 0)
		{
			Outputs op = runInfo.getPersister().readXML( Outputs.class, outputsFile );
			outputs.addAll( op.outputs );
		}
		for( Outputter o : outputs )
		{
			log.info( "Loading Output: " + o.getClass() );
			o.setOutputManager( this );
			if( o instanceof Initialisable ) { ((Initialisable)o).initialise( data, info, extent ); }
			else o.initialise(); //Outputs do their own scheduling in initialise();
			o.open();
		}
		runInfo.setOutputs( this );
		setupClosingOutputs();
	}
	
	public void doOutput( Regions r )
	{
		for( Outputter o : outputs ) o.doOutput( r );
	}
	
	public void finished()
	{
		for( Outputter o : outputs ) o.close();
	}

	public void addOutput( Outputter out ) 
	{ 
		outputs.add( out ); 
		out.setOutputManager( this );
	}
	
	
	/*
	 * Convenience wrappers for getOutputFilename
	 */
	public String getOutputFilename( String output, String extension ) { return getOutputFilename( output, extension, filePattern ); }
	public String getOutputFilename( String output, String extension, String pattern ) { return getOutputFilename( output, extension, pattern, null ); }
	public String getOutputFilename( String output, String extension, Regions r ) { return getOutputFilename( output, extension, filePattern, r ); }
	
	/**
	 * Gets an output filename, and makes sure that the relevant directory exists
	 * @param output
	 * @param extension
	 * @param pattern
	 * @param r
	 * @return
	 */
	public String getOutputFilename( String output, String extension, String pattern, Regions r )
	{
		runInfo.getPersister().setRegion( r );
		Map<String,String> extra = new HashMap<String, String>();
		if( r != null ) extra.put( "r", r.getID() );
		if( output != null ) extra.put("o", output );
		
		String outputFile = runInfo.getPersister().ensureDirectoryExists( pattern, outputDirectoryPattern, false, extra );
		if( extension != null ) 
		{
			if( extension.startsWith( "." ))
				outputFile = outputFile + extension;
			else
				outputFile = outputFile +"."+ extension;
		}
		return outputFile;
	}
	
	public void setOutputDirectory( String outputDirectory ) { this.outputDirectoryPattern = outputDirectory; }

	public boolean isClearExistingFiles() { return clearExistingFiles; }

	public void setClearExistingFiles( boolean clearExistingFiles ) { this.clearExistingFiles = clearExistingFiles; }

	public static interface CloseableOutput extends Outputter { } //Doesn't actually do anything, just lets us know we have to close it
	
	public void registerClosableOutput( CloseableOutput o ) { outputsToClose.add( o ); }
	
	void setupClosingOutputs()
	{
		Runtime.getRuntime().addShutdownHook(new Thread() {
		      public void run() {
		    	  for( CloseableOutput c : outputsToClose ) c.close();
		      } });
	}
}
