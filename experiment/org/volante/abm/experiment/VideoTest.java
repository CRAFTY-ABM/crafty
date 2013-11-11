package org.volante.abm.experiment;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.Random;

import javax.swing.JComponent;

import org.apache.log4j.Logger;
import org.monte.media.*;
import org.monte.media.FormatKeys.MediaType;
import org.monte.media.avi.AVIWriter;
import org.monte.media.math.Rational;
import org.simpleframework.xml.Attribute;
import org.volante.abm.data.*;
import org.volante.abm.output.*;
import org.volante.abm.output.Outputs.ClosableOutput;
import org.volante.abm.schedule.RunInfo;
import org.volante.abm.serialization.Initialisable;

import com.moseph.modelutils.Utilities;

import static org.monte.media.FormatKeys.*;
import static org.monte.media.VideoFormatKeys.*;

public class VideoTest
{
	@Attribute(required=false)
	long frameDuration = 1000;
	@Attribute(required=false)
	int width = 320;
	@Attribute(required=false)
	int height = 240;
	
	private AVIWriter out;
	private int track;
	
	String fn;
	
	Logger log = Logger.getLogger( getClass() );
	private Format format;
	
	public static void main( String[] args )
	{
		VideoTest vt = new VideoTest();
		vt.startFile();
		for( int i = 0; i < 100; i++ )
			vt.doFrame();
		vt.endFile();
	}
	
	public void startFile() 
	{
		try
		{
			fn = "output/test.avi";
			File file = new File( fn );
			for( Format f : Registry.getInstance().getWriterFormats() )
				System.err.println("Format: " + f );
			format = new Format( 
					MediaTypeKey, MediaType.VIDEO, //
					EncodingKey, ENCODING_AVI_MJPG, 
					FrameRateKey, new Rational( 1, 1 ),//
					WidthKey, width, //
					HeightKey, height,//
					QualityKey, 1f,
					DepthKey, 24 );
			System.err.println("Format: " + format);
			out = new AVIWriter( file );
			log.info( "Starting video file: " + fn + " using " + out + " on file: " + file);
			BufferedImage image = new BufferedImage( width, height, BufferedImage.TYPE_INT_RGB );
			out.addTrack( format );
			out.setPalette( 0, image.getColorModel() );
		} catch (IOException e )
		{
			log.error("Couldn't start video file: " + fn);
			e.printStackTrace();
		}
	}
	
	public void doFrame()
	{
		BufferedImage image = new BufferedImage( width, height, BufferedImage.TYPE_INT_RGB );
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		g.setColor( Color.green );
		g.fillRect( 0, 0, 500, 500 );
		g.setColor( Color.red );
		g.fillOval( 
				(int)Utilities.nextDoubleFromTo( 0, width-50 ), 
				(int)Utilities.nextDoubleFromTo( 0, height-50 ), 
				(int)Utilities.nextDoubleFromTo( 40, 100 ), 
				(int)Utilities.nextDoubleFromTo( 40, 100 ) 
				);
		try
		{
			out.write( 0, image, 30 );
		} catch (IOException e)
		{
			log.error( "Couldn't write file to " + fn );
			e.printStackTrace();
		}
	}
	
	public void endFile()
	{
		log.info("Closing video file: " + fn );
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
	
	public void close() { endFile(); }
	

}



