package org.volante.abm.schedule;

import org.volante.abm.output.Outputs;
import org.volante.abm.serialization.ABMPersister;

public class RunInfo
{
	ABMPersister persister = ABMPersister.getInstance();
	Schedule schedule = new DefaultSchedule();
	Outputs outputs = new Outputs();
	String scenario = "";
	String runID = "";
	boolean useInstitutions = false;

	public ABMPersister getPersister() { return persister; }
	public void setPersister( ABMPersister persister ) { this.persister = persister; }
	public Schedule getSchedule() { return schedule; }
	public void setSchedule( Schedule schedule ) { this.schedule = schedule; }
	public Outputs getOutputs() { return outputs; }
	public void setOutputs( Outputs outputs ) { this.outputs = outputs; }
	public String getScenario() { return scenario; }
	public void setScenario( String scenario ) 
	{ 
		this.scenario = scenario; 
		persister.setContext( "s", scenario );
	}
	public String getRunID() { return runID; }
	public void setRunID( String runID ) 
	{ 
		persister.setContext( "i", runID );
		this.runID = runID; 
	}
	public boolean useInstitutions() { return useInstitutions; }
	public void setUseInstitutions( boolean useInstitutions ) { this.useInstitutions = useInstitutions; }

}
