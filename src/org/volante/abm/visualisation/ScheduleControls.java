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
 * 
 */
package org.volante.abm.visualisation;


import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.volante.abm.schedule.Schedule;
import org.volante.abm.schedule.ScheduleStatusEvent;
import org.volante.abm.schedule.ScheduleStatusListener;


public class ScheduleControls extends JPanel implements ScheduleStatusListener {
	JButton			step;
	JButton			stepTillEnd;
	JTextField		runUntil;
	JButton			stop;
	AbstractAction	stepAction;
	AbstractAction	stepTillEndAction;
	AbstractAction	stopNextTick;
	Schedule		schedule;

	public ScheduleControls() {
		stepAction = getAction("Step", new Runnable() {
			@Override
			public void run() {
				schedule.setTargetToNextTick();
			}
		});
		stepTillEndAction = getAction("Step Until", new Runnable() {
			@Override
			public void run() {
				schedule.setTargetTick(Integer.parseInt(runUntil.getText()));
			}
		});
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		stopNextTick = getAction("Stop", new Runnable() {
			@Override
			public void run() {
				schedule.setTargetTick(schedule.getCurrentTick());
			}
		});

		step = new JButton(stepAction);
		stepTillEnd = new JButton(stepTillEndAction);
		stop = new JButton(stopNextTick);
		runUntil = new JTextField("...", 5);

		add(step);
		add(stepTillEnd);
		add(runUntil);
		add(stop);
	}

	public ScheduleControls(Schedule s) {
		this();
		setSchedule(s);
		// runUntil.setText( schedule.getEndTick()+"" );
	}

	public void setSchedule(Schedule s) {
		this.schedule = s;
		schedule.addStatusListener(this);
	}

	@Override
	public void scheduleStatus(ScheduleStatusEvent e) {

	}

	public AbstractAction getAction(String name, final Runnable run) {
		return new AbstractAction(name) {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				SwingUtilities.invokeLater(run);
			}
		};

	}
}
