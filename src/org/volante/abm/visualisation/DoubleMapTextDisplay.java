package org.volante.abm.visualisation;

import java.awt.Dimension;
import java.util.*;

import javax.swing.*;
import javax.swing.border.TitledBorder;

public class DoubleMapTextDisplay extends JPanel implements DoubleMapDisplay
{
	Map<Object, JLabel> displays = new HashMap<Object, JLabel>();
	
	public DoubleMapTextDisplay()
	{
		setLayout( new BoxLayout( this, BoxLayout.Y_AXIS ) );
	}
	public DoubleMapTextDisplay(String title)
	{
		this();
		setBorder( new TitledBorder( title ) );
	}

	public JComponent getDisplay() { return this; }
	public void setMap( Map<?,? extends Number> map )
	{
		for( Object t : map.keySet() )
		{
			if( ! displays.containsKey( t )) addItem( t, map.get(t).doubleValue() );
			displays.get( t ).setText( format( map.get( t ).doubleValue()) );
		}
	}
	
	public void addItem( Object item, double val )
	{
		Box b = new Box( BoxLayout.X_AXIS );
		JLabel lab = new JLabel( item.toString() + ": " );
		lab.setPreferredSize( new Dimension(170,15) );
		b.add( lab );
		JLabel disp = new JLabel( format( val ) );
		disp.setPreferredSize( new Dimension(80,15) );
		disp.setMinimumSize( new Dimension(80,15) );
		b.add( disp );
		displays.put( item, disp );
		b.setAlignmentX( 1 );
		add( b );
		invalidate();
	}
	
	public void clear()
	{
		for( JLabel l : displays.values() )  l.setText("?");
	}

	public String format( double d ) { return String.format( "%7.4f", d ); }
	//public String format( double d ) { return d + ""; }
}
