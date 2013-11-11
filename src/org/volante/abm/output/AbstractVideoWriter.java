package org.volante.abm.output;

import static org.monte.media.FormatKeys.*;
import static org.monte.media.VideoFormatKeys.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.*;

import org.apache.log4j.Logger;
import org.monte.media.*;
import org.monte.media.Format;
import org.monte.media.FormatKeys.MediaType;
import org.monte.media.avi.AVIWriter;
import org.monte.media.math.Rational;
import org.simpleframework.xml.Attribute;
import org.volante.abm.data.*;
import org.volante.abm.output.Outputs.CloseableOutput;
import org.volante.abm.schedule.RunInfo;
import org.volante.abm.serialization.Initialisable;

public abstract class AbstractVideoWriter implements CloseableOutput, Outputter, Initialisable
{

	/**
	 * Output name
	 * Subclasses should provide sensible defaults for this in initialise if it is blank
	 */
	@Attribute(required = false)
	String output; 
	/**
	 * Number of frames per second in the video file
	 */
	@Attribute(required = false)
	long frameRate = 1; 
	/**
	 * Number of times to write an image to the file each tick. Can be used to make videos with 
	 * slower updates than 1 per second.
	 */
	@Attribute(required = false)
	long imagesPerFrame = 1; 
	/**
	 * Width of the video file. Defaults to 500px
	 */
	@Attribute(required = false)
	int width = 500;
	/**
	 * Height of the video file. Defaults to 500px
	 */
	@Attribute(required = false)
	int height = 500;
	
	/**
	 * Should the current tick be added to the images?
	 */
	@Attribute(required=false)
	boolean addTick = true;
	
	Color tickColor = new Color( 0.0f, 0.6f, 0.3f, 0.5f );
	NumberFormat tickFormat = new DecimalFormat( "000" );
	
	protected AVIWriter out;
	protected String fn;
	protected Logger log = Logger.getLogger( getClass() );
	protected Outputs outputs;
	protected RunInfo info;
	protected ModelData data;

	public void open()
	{
		try
		{
			fn = outputs.getOutputFilename( output, ".avi"); //Construct proper output filename
			File file = new File( fn );
			Format format = new Format( MediaTypeKey, MediaType.VIDEO, //
					EncodingKey, ENCODING_AVI_PNG, 
					FrameRateKey, new Rational( frameRate, 1 ),//
					WidthKey, width, //
					HeightKey, height,//
					DepthKey, 24 );
			out = new AVIWriter( file );
			log.info( "Starting video file: " + fn + " using " + out + " on file: " + file + ", w:" + width + ",h:" + height);
			BufferedImage image = new BufferedImage( width, height, BufferedImage.TYPE_INT_RGB );
			out.addTrack( format );
			out.setPalette( 0, image.getColorModel() );
		} catch (IOException e )
		{
			log.error("Couldn't start video file: " + fn);
			e.printStackTrace();
		}
	}
	
	public void doOutput( Regions r )
	{
		if( out == null ) return;
		try
		{
			for( int i = 0; i < imagesPerFrame; i++ )
			{
				BufferedImage image = getImage( r );
				if( addTick )
				{
					Graphics2D g = image.createGraphics();
					g.setColor( tickColor );
					g.setFont( g.getFont().deriveFont( 36.0f ).deriveFont( Font.BOLD ) );
					g.drawString( "t="+tickFormat.format( info.getSchedule().getCurrentTick() ), 2, height-2 );
					g.dispose();
				}
				out.write( 0, image, 1);
			}
		} catch (IOException e)
		{
			log.error( "Couldn't write file to " + fn );
			e.printStackTrace();
		}
	}
	
	abstract BufferedImage getImage( Regions r );

	public void close()
	{
		if( out == null ) return;
		try
		{
			out.close();
			log.info("Closed video file: " + fn );
		} catch (IOException e)
		{
			log.error( "Couldn't close video file: " + fn );
			e.printStackTrace();
		}
	}

	public void initialise( ModelData data, RunInfo info, Region extent ) throws Exception
	{
		outputs = info.getOutputs();
		outputs.registerClosableOutput( this );
		this.info = info;
		this.data = data;
	}

	public void initialise() throws Exception { } //Do it all in the real initialise
	public void setOutputManager( Outputs outputs ) { this.outputs = outputs; }

}
