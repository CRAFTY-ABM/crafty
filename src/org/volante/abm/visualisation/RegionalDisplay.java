package org.volante.abm.visualisation;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.TitledBorder;

import org.volante.abm.data.*;
import org.volante.abm.example.*;
import org.volante.abm.schedule.RunInfo;

public class RegionalDisplay extends AbstractDisplay
{
	Region current;
	Regions regions;
	
	ModelData data;
	RunInfo info;
	
	DoubleMapDisplay supply = new DoubleMapTextDisplay();
	DoubleMapDisplay demand = new DoubleMapTextDisplay();
	DoubleMapDisplay residual = new DoubleMapTextDisplay();


	public void initialise( ModelData data, RunInfo info, Regions region ) throws Exception
	{
		this.data = data;
		this.info = info;
		this.regions = region;
		current = regions.getAllRegions().iterator().next();
		setupDisplay();
	}
	
	public void update() { setRegion( current ); }
	
	public void setRegion( Region r )
	{
		this.current = r;
		residual.setMap( r.getDemandModel().getResidualDemand().toMap() );
		demand.setMap( r.getDemandModel().getDemand().toMap() );
		supply.setMap( r.getDemandModel().getSupply().toMap() );
	}
	
	public void setupDisplay()
	{
		setLayout( new BoxLayout( this, BoxLayout.Y_AXIS  ));
		JComponent aggregate = new Box( BoxLayout.X_AXIS );
		aggregate.setPreferredSize( new Dimension(750,200) );
		aggregate.setMinimumSize( new Dimension(750,200) );
		add( aggregate );
		
		JComponent sDist = supply.getDisplay();
		sDist.setBorder( new TitledBorder( "Supply" ));
		aggregate.add( sDist );
		
		JComponent dDist = demand.getDisplay();
		dDist.setBorder( new TitledBorder( "Demand" ));
		aggregate.add( dDist );
		
		JComponent rDist = residual.getDisplay();
		rDist.setBorder( new TitledBorder( "Residual" ));
		aggregate.add( rDist );
		
		aggregate.setBorder( new TitledBorder( "Overall Supply and Demand" ) );
		aggregate.invalidate();
		setBorder(new TitledBorder( "Main" ));
	}
	
	public JComponent getMainPanel()
	{
		JScrollPane pane = new JScrollPane( this );
		return pane;
	}
	
	public static void main( String[] args ) throws Exception
	{
		BasicTests bt = new BasicTests();
		Region r = bt.r1;
		RegionalDemandModel dem = new RegionalDemandModel();
		r.setDemandModel( dem );
		r.initialise( bt.modelData, bt.runInfo, r );
		RegionalDisplay rd = new RegionalDisplay();
		rd.initialise( bt.modelData, bt.runInfo, r );
		rd.setRegion( r );
		
		JFrame frame = new JFrame("Regional Display Test");
		frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
		
		frame.getContentPane().add( rd.getDisplay() );
		frame.setSize( new Dimension(600,1000));
		frame.setVisible( true );
	}

}
