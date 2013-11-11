package org.volante.abm.visualisation;

import java.awt.Dimension;
import java.util.*;

import javax.swing.*;

import org.apache.log4j.Logger;
import org.simpleframework.xml.ElementList;
import org.volante.abm.data.*;
import org.volante.abm.schedule.RunInfo;

public class ModelDisplays extends JTabbedPane 
{
	@ElementList(inline=true,entry="display",required=false)
	List<Display> displays = new ArrayList<Display>();
	JFrame frame = new JFrame("Model Displays");
	Logger log = Logger.getLogger( getClass() );
	
	public ModelDisplays()
	{
		frame.add( this );
		frame.setSize( new Dimension(800,1200) );
	}

	public void initialise( ModelData data, RunInfo info, Regions extent ) throws Exception
	{
		log.info("Initialising displays: " + extent.getExtent());
		for( Display d : displays )
		{
			d.initialise( data, info, extent );
			info.getSchedule().register( d );
			addTab( d.getTitle(), d.getDisplay() );
		}
		if( displays.size() > 0 )
			frame.setVisible( true );
		for( Display d : displays ) registerDisplay( d );
	}
	
	public void registerDisplay( Display d )
	{
		for( Display o : displays ) if( o != d ) d.addCellListener( o );
		d.setModelDisplays( this );
	}

}
