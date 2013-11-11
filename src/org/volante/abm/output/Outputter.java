package org.volante.abm.output;

import org.volante.abm.data.Regions;

public interface Outputter
{
	public void initialise() throws Exception;
	public void setOutputManager( Outputs outputs );
	public void open();
	public void doOutput( Regions r );
	public void close();

}
