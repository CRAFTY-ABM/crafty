package org.volante.abm.output;

import org.apache.log4j.Logger;
import org.simpleframework.xml.Attribute;
import org.volante.abm.data.*;
import org.volante.abm.schedule.RunInfo;
import org.volante.abm.serialization.ABMPersister;

public abstract class AbstractOutputter implements Outputter
{
	protected Logger log = Logger.getLogger( getClass() );
	protected Outputs outputs;
	@Attribute(required = false)
	private String outputName = "";
	@Attribute(required = false)
	String extension = "csv";
	protected boolean disabled = false;
	@Attribute(required=false)
	protected int everyNYears=1;
	@Attribute(required=false)
	protected int startYear = 1;
	protected RunInfo runInfo;
	protected ModelData modelData;
	protected ABMPersister persister;
	
	public void initialise() throws Exception 
	{ 
		if( disable() )
		{
			disabled = true;
			return;
		}
	}
	
	/**
	 * Callback to start a file if one is required. This is good for e.g. csv files which start a header 
	 * and append to the same file each time writeRecord() is called
	 * but it is not needed for shapefiles where each writeRecord() creates its own file
	 * @param filename
	 */
	public void open() {}
	
	public void close() { }

	public void setOutputManager( Outputs outputs ) 
	{ 
		this.outputs = outputs; 
		this.runInfo = outputs.runInfo;
		this.modelData = outputs.modelData;
		this.persister = runInfo.getPersister();
	}
	public abstract String getDefaultOutputName();
	public String getOutputName() { if( outputName == null || outputName.length() == 0 ) return getDefaultOutputName(); return outputName; }
	public void setOutputName( String outputName ) { this.outputName = outputName; }
	public String getExtension() { return extension; }
	public String filename( Regions r ) { return outputs.getOutputFilename( getOutputName(), getExtension(), r ); }
	public String tickFilename( Regions r ) { return outputs.getOutputFilename( getOutputName(), getExtension(), outputs.tickPattern, r ); }
	public boolean disable() { return false; }

}
