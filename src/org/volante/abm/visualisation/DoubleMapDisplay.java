package org.volante.abm.visualisation;

import java.util.Map;

import javax.swing.JComponent;


public interface  DoubleMapDisplay
{
	public JComponent getDisplay();
	public void setMap( Map<?,? extends Number> map );
	public void clear();
}
