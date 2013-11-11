package org.volante.abm.visualisation;

import static java.lang.Math.*;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.*;

import org.simpleframework.xml.Attribute;
import org.volante.abm.data.Cell;


public abstract class MaxMinCellDisplay extends CellDisplay
{
	@Attribute(required=false)
	boolean updateMaxMin = true;
	Color startCol = Color.black;
	Color endCol = Color.cyan;
	Color nanCol = Color.yellow;
	float[] start = startCol.getColorComponents( null );
	float[] end = endCol.getColorComponents( null );
	double min= 0;
	double max= 1;
	Legend legend = new Legend();

	public void update()
	{
		if( updateMaxMin )
		{
			min = Double.MAX_VALUE;
			max = Double.MIN_VALUE;
			for( Cell c : region.getAllCells() )
			{
				double v = getVal( c );
				min = min( min, v );
				max = max( max, v );
			}
		}
		legend.updateInfo( min, max, startCol, endCol );
		super.update();
	}
	public abstract double getVal( Cell c );
	
	public int getColourForCell( Cell c )
	{
		double v = getVal( c );
		if( Double.isNaN( v )) return nanCol.getRGB();
		double val = (getVal( c) - min ) / (max -min);
		val = max( 0, min(val,1));
		return interpolate( start, end, (float)val );
	}
	
	public int interpolate( float[] start, float[] end, float amount )
	{
		float nAmount = 1 - amount;
		float[] newCol = new float[start.length];
		for( int i = 0; i < start.length; i++ )
			newCol[i] = start[i] * nAmount + end[i] * amount;
		return floatsToARGB( 1, newCol[0], newCol[1], newCol[2] );
	}
	
	public void setStartColour( Color s )
	{
		startCol = s;
		start = startCol.getColorComponents( null );
	}
	
	public void setEndColour( Color e )
	{
		endCol = e;
		end = endCol.getColorComponents( null );
	}
	
	
	public class Legend extends JPanel
	{
		JPanel startPan = new JPanel();
		JPanel endPan = new JPanel();
		JLabel startNum = new JLabel("0.0");
		JLabel endNum = new JLabel("0.0");
		
		public Legend()
		{
			setLayout( new BoxLayout( this, BoxLayout.X_AXIS ) );
			startPan.setPreferredSize( new Dimension( 50, 20 ) );
			startPan.setMaximumSize( new Dimension( 50, 20 ) );
			endPan.setPreferredSize( new Dimension( 50, 20 ) );
			endPan.setMaximumSize( new Dimension( 50, 20 ) );
			startPan.setAlignmentX( 1 );
			startNum.setAlignmentX( 1 );
			endPan.setAlignmentX( 1 );
			endNum.setAlignmentX( 1 );
			add(Box.createHorizontalGlue());
			Box start = new Box( BoxLayout.X_AXIS );
			start.add( startNum );
			start.add(Box.createHorizontalStrut( 5 ));
			start.add( startPan );
			add( start );
			add(Box.createHorizontalStrut( 20 ));
			add( new JLabel(" to "));
			add(Box.createHorizontalStrut( 20 ));
			Box end = new Box( BoxLayout.X_AXIS );
			end.add( endPan );
			end.add(Box.createHorizontalStrut( 5 ));
			end.add( endNum );
			add( end );
			add(Box.createHorizontalGlue());
			setBorder( new TitledBorder( new EtchedBorder(), "Key" ) );
		}
		public void updateInfo( double min, double max, Color start, Color end )
		{
			startPan.setBackground( start );
			endPan.setBackground( end );
			startNum.setText( min + "" );
			endNum.setText( max + "" );
		}
	}

	public JComponent getControls() { return null; }
	public JComponent getLegend() { return legend; }



}
