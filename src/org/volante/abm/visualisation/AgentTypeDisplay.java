package org.volante.abm.visualisation;

import java.awt.*;
import java.util.*;

import javax.swing.*;

import org.simpleframework.xml.Attribute;
import org.volante.abm.agent.Agent;
import org.volante.abm.data.Cell;
import org.volante.abm.visualisation.CellDisplay;

import com.moseph.modelutils.Utilities;

public class AgentTypeDisplay extends CellDisplay
{
	
	Map<String, Color> agentColours = new HashMap<String, Color>();
	@Attribute(required=false)
	String prefix = null;
	JPanel legend = new JPanel();
	
	public AgentTypeDisplay()
	{
		addAgent(Agent.NOT_MANAGED_ID, Color.gray.brighter());
	}
	
	public void addAgent( String name, Color color )
	{
		agentColours.put( name, color );
	}
	public Color getColor( Cell c )
	{
		Color col =  agentColours.get( c.getOwnerID() );
		if( col !=  null ) return col;
		log.warn("No colour found for: " + c.getOwnerID() + " so making one up");
		Color nc = new Color( Utilities.nextIntFromTo( 0, 255 ),
				Utilities.nextIntFromTo( 0, 255 ), Utilities.nextIntFromTo( 0, 255 ) );
		agentColours.put( c.getOwnerID(), nc );
		updateLegend();
		return nc;
	}

	public int getColourForCell( Cell c )
	{
		return getColor( c ).getRGB();
	}

	public JComponent getLegend()
	{
		updateLegend();
		return legend;
	}
	
	public void updateLegend()
	{
		legend.setLayout( new FlowLayout() );
		legend.removeAll();
		for( String name : agentColours.keySet() )
		{
			Box b = new Box(BoxLayout.Y_AXIS);
			JPanel p = new JPanel();
			p.setBackground( agentColours.get( name ) );
			p.setPreferredSize( new Dimension(30, 30) );
			p.setAlignmentX( 0.5f );
			b.add( p );
			if( prefix != null ) name = name.replace(prefix,"");
			JLabel lab = new JLabel(name);
			lab.setAlignmentX( 0.5f );
			b.add(lab );
			legend.add( b );
		}
		revalidate();
	}
}
