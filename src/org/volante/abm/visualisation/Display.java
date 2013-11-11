package org.volante.abm.visualisation;

import javax.swing.JComponent;

import org.simpleframework.xml.Root;
import org.volante.abm.data.*;
import org.volante.abm.schedule.*;

@Root
public interface Display extends PostTickAction
{
	public void update();
	public String getTitle();
	public JComponent getDisplay();
	public void initialise( ModelData data, RunInfo info, Regions region ) throws Exception;
	public void setModelDisplays( ModelDisplays d );
	public void addCellListener( Display d );
	public void cellChanged( Cell c );
	public void fireCellChanged( Cell c );
}
