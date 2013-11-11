package org.volante.abm.visualisation;

import static java.lang.Math.*;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.*;

import javax.swing.*;
import javax.swing.text.NumberFormatter;

import info.monitorenter.gui.chart.*;
import info.monitorenter.gui.chart.labelformatters.LabelFormatterNumber;
import info.monitorenter.gui.chart.traces.Trace2DSimple;

import org.volante.abm.data.*;
import org.volante.abm.example.CurveCompetitivenessModel;
import org.volante.abm.models.CompetitivenessModel.CompetitivenessDisplay;
import org.volante.abm.schedule.RunInfo;

import com.moseph.modelutils.curve.Curve;

public class CurveCompetitivenessDisplay extends AbstractDisplay implements CompetitivenessDisplay
{
	double width = 2;
	int numPoints = 100;
	CurveCompetitivenessModel model;
	Map<Service, Chart2D> charts = new HashMap<Service, Chart2D>();
	Box chartBox = new Box(BoxLayout.Y_AXIS);
	ModelData data;
	private RunInfo info;
	private Regions region;
	
	public CurveCompetitivenessDisplay( CurveCompetitivenessModel model ) 
	{
		this.model = model;
		add( chartBox );
	}

	public Chart2D getNewChart( Service c )
	{
		Chart2D chart = new Chart2D();
		chart.getAxisX().getAxisTitle().setTitle( null );
		chart.getAxisY().getAxisTitle().setTitle( null );
		IAxisLabelFormatter format = new LabelFormatterNumber( new DecimalFormat( "0.0E0"  ) );
		chart.getAxisX().setFormatter( format );
		chart.getAxisY().setFormatter( format );
		ITrace2D trace = new Trace2DSimple(c.getName() + " Utility for Residual Demand");
		trace.setColor( Color.red );
		trace.setStroke( new BasicStroke( 3.0f ) );
		chart.addTrace( trace );
		
		ITrace2D trace2 = new Trace2DSimple("Current");
		trace2.setColor( Color.green );
		trace2.setStroke( new BasicStroke( 3.0f ) );
		chart.addTrace( trace2 );
		
		chart.setPreferredSize( new Dimension(300,150) );
		chart.setUseAntialiasing( true );
		return chart;
	}
	
	public void updateChart( Curve c, Chart2D chart, Service s )
	{
		double current = region.getAllRegions().iterator().next().getDemandModel().getResidualDemand().get( s )/region.getNumCells();
		ITrace2D trace = chart.getTraces().first();
		trace.removeAllPoints();
		double cWidth = max( width, abs( current )/10 );
		double minY = Double.MAX_VALUE;
		double maxY = Double.MIN_VALUE;
		double min = current - cWidth;
		double max = current + cWidth;
		for( double i = min; i < max; i+=abs( (max-min)/numPoints ) )
		{
			double val = c.sample( i );
			trace.addPoint( i, val );
			minY = min( minY, val );
			maxY = max( maxY, val );
		}
		ITrace2D trace2 = chart.getTraces().last();
		trace2.removeAllPoints();
		trace2.addPoint( current, minY );
		trace2.addPoint( current, maxY );
	}

	public void update()
	{
		Map<Service, Curve> curves = model.getCurves();
		for( Service s : curves.keySet() )
		{
			if( ! charts.containsKey( s ))
			{
				charts.put( s, getNewChart( s ) );
				chartBox.add( charts.get(s));
				chartBox.invalidate();
			}
			updateChart( curves.get( s ), charts.get( s ), s );
		}
	}

	public void initialise( ModelData data, RunInfo info, Regions region ) throws Exception
	{
		this.data = data;
		this.info = info;
		this.region = region;
	}

}
