package org.volante.abm.visualisation;

import static java.lang.Math.*;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.*;

import info.monitorenter.gui.chart.*;
import info.monitorenter.gui.chart.traces.Trace2DSimple;

import org.volante.abm.data.*;
import org.volante.abm.example.RegionalDemandModel;
import org.volante.abm.models.DemandModel.DemandDisplay;
import org.volante.abm.schedule.RunInfo;

import com.moseph.modelutils.fastdata.DoubleMap;

public class RegionalDemandDisplay extends AbstractDisplay implements DemandDisplay
{
	RegionalDemandModel model;
	DoubleMapDisplay supplyDisplay = new DoubleMapTextDisplay("Supply");
	DoubleMapDisplay demandDisplay = new DoubleMapTextDisplay("Demand");
	DoubleMapDisplay residualDisplay = new DoubleMapTextDisplay("Residual");
	DoubleMapDisplay marginalDisplay = new DoubleMapTextDisplay("Marginal Utilities");
	DoubleMapDisplay supplyPerCellDisplay = new DoubleMapTextDisplay("Supply");
	DoubleMapDisplay demandPerCellDisplay = new DoubleMapTextDisplay("Demand");
	DoubleMapDisplay residualPerCellDisplay = new DoubleMapTextDisplay("Residual");
	DoubleMapDisplay marginalPerCellDisplay = new DoubleMapTextDisplay("Marginal Util");

	
	public RegionalDemandDisplay( RegionalDemandModel model )
	{
		this.model = model;
	
	}

	public void update()
	{
		DoubleMap<Service> supp = model.getSupply().copy();
		DoubleMap<Service> dem = model.getDemand().copy();
		DoubleMap<Service> res = model.getResidualDemand().copy();
		supplyDisplay.setMap( supp.toMap() );
		demandDisplay.setMap( dem.toMap() );
		residualDisplay.setMap( res.toMap() );
		marginalDisplay.setMap( model.getMarginalUtilities().toMap() );
		supp.multiplyInto( 1.0/region.getNumCells(), supp );
		dem.multiplyInto( 1.0/region.getNumCells(), dem );
		res.multiplyInto( 1.0/region.getNumCells(), res );
		supplyPerCellDisplay.setMap( supp.toMap() );
		demandPerCellDisplay.setMap( dem.toMap() );
		residualPerCellDisplay.setMap( res.toMap() );
		marginalPerCellDisplay.setMap( model.getMarginalUtilities().toMap() );
	}
	


	@Override
	public void initialise( ModelData data, RunInfo info, Regions region ) throws Exception
	{
		super.initialise( data, info, region );
		setLayout( new BoxLayout( this, BoxLayout.X_AXIS ) );
		Box total = new Box( BoxLayout.Y_AXIS );
		Box perCell = new Box( BoxLayout.Y_AXIS );
		total.add( supplyDisplay.getDisplay() );
		total.add( demandDisplay.getDisplay() );
		total.add( residualDisplay.getDisplay() );
		total.add( marginalDisplay.getDisplay() );
		perCell.add( supplyPerCellDisplay.getDisplay() );
		perCell.add( demandPerCellDisplay.getDisplay() );
		perCell.add( residualPerCellDisplay.getDisplay() );
		perCell.add( marginalPerCellDisplay.getDisplay() );
		total.setBorder( new TitledBorder( new EtchedBorder(), "Total"));
		perCell.setBorder( new TitledBorder( new EtchedBorder(), "Per Cell"));
		add( total);
		add( perCell);
	}
	
	public static void main( String args[] )
	{
		RegionalDemandModel dm = new RegionalDemandModel();
		RegionalDemandDisplay display = dm.getDisplay();
		display.update();
		
		JFrame frame = new JFrame( "Regional Demand Display" );
		frame.getContentPane().add( display.getDisplay() );
		
		frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
		frame.setSize( new Dimension( 500, 600 ));
		frame.setVisible( true );
	}

}
