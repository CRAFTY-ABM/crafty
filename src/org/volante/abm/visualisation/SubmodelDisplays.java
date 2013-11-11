package org.volante.abm.visualisation;

import java.awt.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;

import org.volante.abm.data.*;
import org.volante.abm.models.*;
import org.volante.abm.schedule.RunInfo;

public class SubmodelDisplays extends AbstractDisplay
{
	JComponent competitionPanel = null;
	JComponent allocationPanel = null;
	JComponent demandPanel = null;
	JComponent agentsPanel = null;
	
	Display competitionDisplay = null;
	CompetitivenessModel competition = null;
	Display demandDisplay = null;
	DemandModel demand = null;
	AllocationModel allocation = null;
	
	CellDisplay map;
	
	Map<Displayable,Display> displays = new HashMap<Displayable, Display>();
	Map<JComponent,Displayable> currentSelection = new HashMap<JComponent, Displayable>();
	JPanel displaysPanel = new JPanel();
	
	public SubmodelDisplays()
	{
		map = new CellDisplay()
		{ public int getColourForCell( Cell c ) { return Color.gray.getRGB(); }

		public void initialise( ModelData data, RunInfo info, Regions region ) throws Exception
		{
			super.initialise( data, info, region );
			log.info("Initialised: " + this.region.getExtent() + ", height: " + height + ", width: " + width);
		} 
		
		};
		displaysPanel.setLayout( new GridLayout(2, 2));
		map.getMainPanel().setPreferredSize( new Dimension( 200, 400 ) );
		competitionPanel = modelPanel("Competition");
		allocationPanel = modelPanel("Allocation");
		demandPanel = modelPanel("Demand");
		agentsPanel = modelPanel("Agents");
		setLayout( new BorderLayout() );
		add( displaysPanel, BorderLayout.CENTER );
		add( map, BorderLayout.EAST );
	}
	
	JComponent modelPanel( String title )
	{
		JPanel comp = new JPanel();
		JScrollPane pane = new JScrollPane( comp );
		pane.setBorder( new TitledBorder( new EtchedBorder(), title ));
		comp.setLayout( new BorderLayout() );
		displaysPanel.add( pane );
		return comp;
	}
	
	
	public void update()
	{
		if( competitionDisplay != null ) competitionDisplay.update();
		if( demandDisplay != null ) demandDisplay.update();
	}

	@Override
	public void cellChanged( Cell c )
	{
		super.cellChanged( c );
		log.debug( "Cell changed! " + c);
		setCompetitivenessModel( c.getRegion().getCompetitionModel() );
		setAllocationModel( c.getRegion().getAllocationModel() );
		setDemandModel( c.getRegion().getDemandModel() );
		update();
	}
	
	public void setCompetitivenessModel( CompetitivenessModel c )
	{
		competition = c;
		if( competition != null ) addDisplay( competition, competitionPanel );
	}
	public void setAllocationModel( AllocationModel c )
	{
		allocation = c;
		if( allocation != null ) addDisplay( allocation, allocationPanel );
	}
	
	public void addDisplay( Displayable s, JComponent target )
	{
		if( currentSelection.get( target ) == s )
		{
			displays.get( s ).update();
			return;
		}
		target.removeAll();
		try 
		{
			if( ! displays.containsKey( s ))
			{
				Display compDisp = s.getDisplay();
				compDisp.initialise( data, info, region );
				displays.put( s, compDisp );
			}
			Display com = displays.get( s );
			com.update();
			target.add( com.getDisplay(), BorderLayout.CENTER );
			target.invalidate();
			repaint();
			currentSelection.put( target, s );
		} catch( Exception e )
		{
			log.error("Couldn't set s display: " + e.getMessage() );
			e.printStackTrace();
		}
	}
	
	public void setDemandModel( DemandModel c )
	{
		demand = c;
		if( demand != null ) addDisplay( demand, demandPanel );
	}

	public JComponent getPanel()
	{
		return map.getMainPanel();
	}


	public void initialise( ModelData data, RunInfo info, Regions region ) throws Exception
	{
		super.initialise( data, info, region );
		map.initialise( data, info, region );
		map.update();
		cellChanged( region.getAllCells().iterator().next() );
	}
	
	public void setModelDisplays( ModelDisplays d )
	{
		super.setModelDisplays( d );
		d.registerDisplay( map );
	}

}
