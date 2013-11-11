package org.volante.abm.visualisation;

import java.awt.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;

import org.volante.abm.agent.PotentialAgent;
import org.volante.abm.data.*;
import org.volante.abm.schedule.RunInfo;

public class CellInfoDisplay extends JPanel
{
	DoubleMapDisplay capitalDisplay = new DoubleMapTextDisplay();
	DoubleMapDisplay baseCapitalDisplay = new DoubleMapTextDisplay();
	DoubleMapDisplay productionDisplay = new DoubleMapTextDisplay();
	DoubleMapDisplay competitivenessDisplay = new DoubleMapTextDisplay();
	DoubleMapDisplay unadjustedCompetitivenessDisplay = new DoubleMapTextDisplay();
	JTextArea owner = new JTextArea("Unknown");
	JLabel xLoc = new JLabel("X=?");
	JLabel yLoc = new JLabel("Y=?");
	
	public CellInfoDisplay()
	{
		setLayout( new BoxLayout( this, BoxLayout.Y_AXIS ) );
		owner.setLineWrap(true);
		JScrollPane ownerScroll = new JScrollPane(owner);
		ownerScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		ownerScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		
		
		Box location = new Box( BoxLayout.Y_AXIS );
		location.add( xLoc );
		location.add( yLoc );
		addPanel(location,"Location");
		addPanel(ownerScroll, "Owner");
		addPanel( capitalDisplay.getDisplay(), "Capitals");
		addPanel( baseCapitalDisplay.getDisplay(), "Base Capitals");
		addPanel( productionDisplay.getDisplay(), "Productivity");
		addPanel( competitivenessDisplay.getDisplay(), "Competitiveness");
		addPanel( unadjustedCompetitivenessDisplay.getDisplay(), "Unadjusted Competitiveness");
		setPreferredSize( new Dimension(250,400) );
		setMaximumSize( new Dimension(250,4000) );
		clearCell();
		
	}
	
	public void addPanel( JComponent cDisp, String title )
	{
		cDisp.setBorder( new TitledBorder( new EtchedBorder(), title ));
		cDisp.setPreferredSize( new Dimension( 250, 500 ) );
		cDisp.setMaximumSize( new Dimension( 250, 1000 ) );
		cDisp.setAlignmentX( 0.5f );
		add( cDisp );
	}
	
	public void setCell( Cell c )
	{
		if( c == null )
		{
			clearCell();
			return;
		}
		capitalDisplay.setMap( c.getEffectiveCapitals().toMap() );
		baseCapitalDisplay.setMap( c.getBaseCapitals().toMap() );
		productionDisplay.setMap( c.getSupply().toMap() );
		competitivenessDisplay.setMap( getCompetitivenessMap( c ) );
		unadjustedCompetitivenessDisplay.setMap( getUnadjustedCompetitivenessMap( c ) );
		owner.setText( c.getOwnerID() + "\n" + c.getOwner().infoString() );
		setCellXY( c.getX(), c.getY() );
		revalidate();
		repaint();
	}
	
	public void setCellXY( int x, int y )
	{
		xLoc.setText( "X=" + ( x != Integer.MIN_VALUE ? x+"" : "?" ) );
		yLoc.setText( "Y=" + ( y != Integer.MIN_VALUE ? y+"" : "?" ) );
	}
	
	public void clearCell()
	{
		capitalDisplay.clear();
		baseCapitalDisplay.clear();
		productionDisplay.clear();
		competitivenessDisplay.clear();
		unadjustedCompetitivenessDisplay.clear();
		owner.setText( "NONE SELECTED");
		setCellXY( Integer.MIN_VALUE, Integer.MIN_VALUE );
		revalidate();
		repaint();
	}
	
	public Map<String,Double> getCompetitivenessMap( Cell c )
	{
		Map<String, Double> map = new HashMap<String, Double>();
		Region r = c.getRegion();
		if( r != null )
			for( PotentialAgent a : r.getAllPotentialAgents() )
				map.put( a.getID(), r.getCompetitiveness( a, c ));
		return map;
	}
	public Map<String,Double> getUnadjustedCompetitivenessMap( Cell c )
	{
		Map<String, Double> map = new HashMap<String, Double>();
		Region r = c.getRegion();
		if( r != null )
			for( PotentialAgent a : r.getAllPotentialAgents() )
				map.put( a.getID(), r.getUnadjustedCompetitiveness( a, c ));
		return map;
	}

	public static void main( String[] args ) throws Exception
	{
		ModelData data = new ModelData();
		Region r = new Region();
		//r.initialise( data, null, null );
		Cell c = new Cell(20,40);
		r.addCell( c );
		c.initialise( data, new RunInfo(), null );
		
		CellInfoDisplay display = new CellInfoDisplay();
		
		JFrame frame = new JFrame("Test Cell Display");
		frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
		frame.setSize( new Dimension( 500, 500 ) );
		
		JPanel p = new JPanel();
		p.setLayout( new BorderLayout()  );
		p.add( display, BorderLayout.CENTER );
		p.add( new JPanel(), BorderLayout.WEST );
		frame.add(p);
		frame.pack();
		frame.setVisible( true );
		display.setCell( c );
	}
}
