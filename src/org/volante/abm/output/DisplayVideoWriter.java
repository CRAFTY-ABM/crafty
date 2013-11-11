package org.volante.abm.output;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.swing.JComponent;

import org.simpleframework.xml.*;
import org.volante.abm.data.*;
import org.volante.abm.output.Outputs.CloseableOutput;
import org.volante.abm.schedule.RunInfo;
import org.volante.abm.serialization.Initialisable;
import org.volante.abm.visualisation.*;

public class DisplayVideoWriter extends AbstractVideoWriter 
{
	@Attribute(required=false)
	boolean includeSurroundings = true;
	
	@Element
	Display display;
	JComponent toPaint;
	
	
	public BufferedImage getImage( Regions r )
	{
		display.update();
		return ComponentImageCreator.createImage( toPaint );
	}
	

	public void initialise( ModelData data, RunInfo info, Region extent ) throws Exception
	{
		super.initialise(data, info, extent );
		display.initialise( data, info, extent );
		
		if( output == null || output.equals("") )
			output = display.getTitle().replaceAll( "\\s", "" );
		//Either just get the main panel, or get the whole display
		if( ! includeSurroundings && display instanceof AbstractDisplay) 
			toPaint = ((AbstractDisplay)display).getMainPanel();
		else 
			toPaint = display.getDisplay();
		toPaint.setPreferredSize( new Dimension( width, height ) );
		toPaint.setSize( new Dimension( width, height ) );
	}

}
