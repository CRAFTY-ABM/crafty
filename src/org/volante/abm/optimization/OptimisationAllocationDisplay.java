/**
 * This file is part of
 * 
 * CRAFTY - Competition for Resources between Agent Functional TYpes
 *
 * Copyright (C) 2014 School of GeoScience, University of Edinburgh, Edinburgh, UK
 * 
 * CRAFTY is free software: You can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software 
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *  
 * CRAFTY is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * School of Geoscience, University of Edinburgh, Edinburgh, UK
 */
package org.volante.abm.optimization;


import info.monitorenter.gui.chart.Chart2D;
import info.monitorenter.gui.chart.IAxisLabelFormatter;
import info.monitorenter.gui.chart.labelformatters.LabelFormatterNumber;
import info.monitorenter.gui.chart.traces.Trace2DSimple;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GridLayout;
import java.text.DecimalFormat;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.volante.abm.models.AllocationModel.AllocationDisplay;
import org.volante.abm.optimization.OptimizationAllocationModel.OptimizationListener;
import org.volante.abm.visualisation.AbstractDisplay;


public class OptimisationAllocationDisplay extends AbstractDisplay implements AllocationDisplay,
		OptimizationListener {
	/**
	 * 
	 */
	private static final long						serialVersionUID	= -2758465882242773664L;
	JLabel											runNumberLabel		= new JLabel("No runs yet");
	JLabel											scoreNumberLabel	= new JLabel("No runs yet");
	JLabel											stateLabel			= new JLabel("Not started");
	private final OptimizationAllocationModel<?>	model;
	private Trace2DSimple							trace				= null;
	private Chart2D									chart				= null;
	int												numRuns				= 1;

	public OptimisationAllocationDisplay(OptimizationAllocationModel<?> model) {
		log.info("Setting up GA display");
		this.model = model;
		this.model.addListener(this);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(new JLabel(model.getOptimisationType()));
		setupDisplay();
		setupChart();
		add(chart);
	}

	public void setupDisplay() {
		JPanel p = new JPanel();
		p.setLayout(new GridLayout(3, 2));
		p.add(new JLabel("State"));
		p.add(stateLabel);
		p.add(new JLabel("Run"));
		p.add(runNumberLabel);
		p.add(new JLabel("Score"));
		p.add(scoreNumberLabel);
		add(p);
	}

	public void setupChart() {
		chart = new Chart2D();
		chart.getAxisX().getAxisTitle().setTitle("Run");
		chart.getAxisY().getAxisTitle().setTitle("Utility");
		IAxisLabelFormatter format = new LabelFormatterNumber(new DecimalFormat("2.0"));
		chart.getAxisX().setFormatter(format);
		chart.getAxisY().setFormatter(format);
		startNewTrace();

		// chart.setPreferredSize( new Dimension(300,150) );
		chart.setUseAntialiasing(true);
	}

	public void startNewTrace() {
		if (trace != null) {
			trace.setColor(Color.gray.darker());
			trace.setName("Run " + numRuns++);
		}
		trace = new Trace2DSimple("Overall Score");
		trace.setColor(Color.red);
		trace.setStroke(new BasicStroke(2.0f));
		chart.addTrace(trace);
	}

	public void updateChart(int run, double score) {
		trace.addPoint(run, score);
	}

	@Override
	public void setState(String state) {
		stateLabel.setText(state);
	}

	@Override
	public void updateBest(int run, double score) {
		runNumberLabel.setText(run + "");
		scoreNumberLabel.setText(score + "");
		updateChart(run, score);
	}

	@Override
	public void startRuns() {
		startNewTrace();
		runNumberLabel.setText("--");
		scoreNumberLabel.setText("--");
	}
}
