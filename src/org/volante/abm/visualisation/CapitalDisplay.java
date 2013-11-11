package org.volante.abm.visualisation;

import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.util.*;

import javax.swing.JFrame;

import org.volante.abm.data.*;

public class CapitalDisplay extends DatatypeDisplay<Capital> implements Display, ActionListener
{
	Capital capital = null;

	public double getVal( Cell c )
	{
		if( capital == null ) return Double.NaN;
		return c.getEffectiveCapitals().getDouble( capital );
	}

	public Collection<String> getNames()
	{
		Set<String> names = new HashSet<String>();
		for( Capital c : data.capitals ) names.add( c.getName() );
		return names;
	}
	
	public void setupType( String type )
	{
		capital = data.capitals.forName( type );
	}
	
	
	public static void main( String[] args ) throws Exception
	{
		
		Region r = new Region();
		ModelData data = new ModelData();
		Capital capital = data.capitals.get( 0 );
		for( int x = 0; x < 255; x++ )
		{
			for( int y = 0; y < 255; y++ )
			{
				Cell c = new Cell(x,y);
				c.initialise( data, null, r );
				c.getModifiableBaseCapitals().putDouble( capital, x+y );
				r.addCell( c );
			}
		}
		
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
		
		CapitalDisplay  ce = new CapitalDisplay();
		ce.initialType = capital.getName();
		ce.initialise( data, null, r );
		ce.update();
		
		frame.add( ce.getDisplay() );
		frame.setSize( new Dimension( 500, 500 ) );
		frame.setVisible( true  );
		
	}

	

}
