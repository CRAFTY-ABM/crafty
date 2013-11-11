package org.volante.abm.optimization;

import static java.lang.Math.*;
import info.monitorenter.gui.chart.*;
import info.monitorenter.gui.chart.labelformatters.LabelFormatterNumber;
import info.monitorenter.gui.chart.traces.Trace2DSimple;

import javax.swing.*;

import java.awt.*;
import java.text.DecimalFormat;


import org.volante.abm.data.Service;
import org.volante.abm.models.AllocationModel.AllocationDisplay;
import org.volante.abm.optimization.OptimizationAllocationModel.OptimizationListener;
import org.volante.abm.visualisation.AbstractDisplay;

import com.moseph.modelutils.curve.Curve;

public class OptimisationAllocationDisplay extends AbstractDisplay implements AllocationDisplay, OptimizationListener
{
	JLabel runNumberLabel = new JLabel("No runs yet");
	JLabel scoreNumberLabel = new JLabel("No runs yet");
	JLabel stateLabel = new JLabel("Not started");
	private OptimizationAllocationModel<?> model;
	private Trace2DSimple trace;
	private Chart2D chart;
	int numRuns = 1;

	public OptimisationAllocationDisplay( OptimizationAllocationModel<?> model )
	{
		log.info("Setting up GA display");
		this.model = model;
		this.model.addListener( this );
		setLayout(new BoxLayout( this, BoxLayout.Y_AXIS ));
		add(new JLabel(model.getOptimisationType()));
		setupDisplay();
		setupChart();
		add( chart );
	}
	
	public void setupDisplay()
	{
		JPanel p = new JPanel();
		p.setLayout( new GridLayout(3, 2 ));
		p.add(new JLabel("State"));
		p.add(stateLabel);
		p.add(new JLabel("Run"));
		p.add(runNumberLabel);
		p.add(new JLabel("Score"));
		p.add(scoreNumberLabel);
		add( p );
	}
	
	public void setupChart()
	{
		chart = new Chart2D();
		chart.getAxisX().getAxisTitle().setTitle( "Run" );
		chart.getAxisY().getAxisTitle().setTitle( "Utility" );
		IAxisLabelFormatter format = new LabelFormatterNumber( new DecimalFormat( "2.0"  ) );
		//chart.getAxisX().setFormatter( format );
		//chart.getAxisY().setFormatter( format );
		startNewTrace();
		
		//chart.setPreferredSize( new Dimension(300,150) );
		chart.setUseAntialiasing( true );
	}
	
	public void startNewTrace()
	{
		if( trace != null )
		{
			trace.setColor( Color.gray.darker() );
			trace.setName( "Run " + numRuns++ );
		}
		trace = new Trace2DSimple("Overall Score");
		trace.setColor( Color.red );
		trace.setStroke( new BasicStroke( 2.0f ) );
		chart.addTrace( trace );
	}
	
	public void updateChart( int run, double score )
	{
		trace.addPoint( run, score );
	}


	public void setState( String state )
	{
		stateLabel.setText( state );
	}

	public void updateBest( int run, double score )
	{
		runNumberLabel.setText( run+"" );
		scoreNumberLabel.setText( score+"" );
		updateChart( run, score );
	}
	
	public void startRuns()
	{
		startNewTrace();
		runNumberLabel.setText( "--" );
		scoreNumberLabel.setText( "--" );
	}

}
