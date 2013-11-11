package org.volante.abm.visualisation;

import java.awt.*;

import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

import org.apache.log4j.Logger;
import org.simpleframework.xml.*;
import org.volante.abm.data.*;
import org.volante.abm.schedule.RunInfo;

public abstract class AbstractDisplay extends JPanel implements Display
{
	@Attribute
	String title = "Unknown";
	public void postTick() { update(); }
	public String getTitle() { return title; }
	protected Logger log = Logger.getLogger( getClass() );
	
	public JComponent getControls() { return null; }
	public JComponent getLegend() { return null; }
	public JComponent getPanel() { return null; }
	public JComponent getMainPanel() { return this; }
	
	List<Display> cellListeners = new ArrayList<Display>();
	
	protected ModelData data;
	protected RunInfo info;
	protected Regions region;

	protected ModelDisplays modelDisplays;

	public JComponent getDisplay()
	{
		JPanel panel = new JPanel();
		panel.setLayout( new BorderLayout() );
		panel.add( getMainPanel(), BorderLayout.CENTER );
		JComponent controls = getControls();
		if( controls != null ) panel.add( controls, BorderLayout.NORTH );
		
		JComponent legend = getLegend();
		if( legend != null ) panel.add( legend, BorderLayout.SOUTH );
		
		JComponent p = getPanel();
		if( p != null ) panel.add( p, BorderLayout.EAST );
		return panel;
	}
	
	public void paint( Graphics g )
	{
		((Graphics2D)g).setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
		super.paint(g);
	}
	
	public void addCellListener( Display d ) { if( d != this ) cellListeners.add( d ); }
	public void cellChanged( Cell c ) {}
	public void fireCellChanged( Cell c ) { for( Display d : cellListeners ) d.cellChanged( c ); }
	public void setModelDisplays( ModelDisplays d ) { this.modelDisplays = d; }
	public void update() {} //Nothing to do here
	public void postUpdate()
	{
	}
	
	public void initialise( ModelData data, RunInfo info, Regions region ) throws Exception
	{
		this.data = data;
		this.info = info;
		this.region = region;
	}
}
