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
package org.volante.abm.visualisation;


import java.awt.Dimension;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;

import org.volante.abm.agent.Agent;
import org.volante.abm.agent.PotentialAgent;
import org.volante.abm.data.ModelData;
import org.volante.abm.data.Region;
import org.volante.abm.data.Regions;
import org.volante.abm.example.SimpleAllocationModel;
import org.volante.abm.models.AllocationModel.AllocationDisplay;
import org.volante.abm.schedule.RunInfo;

public class SimpleAllocationDisplay extends AbstractDisplay implements AllocationDisplay
{
	private static final long	serialVersionUID	= -3347503064117103098L;

	SimpleAllocationModel		model				= null;

	Region						r;


	Map<String, JLabel>			disps				= new HashMap<String, JLabel>();

	public SimpleAllocationDisplay(SimpleAllocationModel model) {
		this.model = model;
	}

	@Override
	public void update() {
		int[] pagentNumbers = new int[r.getPotentialAgents().size()];
		for (Agent a : r.getAgents()) {
			pagentNumbers[a.getType().getSerialID()]++;
		}

		// calculate overall sum
		int sum = 0;
		for (int i = 0; i < pagentNumbers.length; i++) {
			sum += pagentNumbers[i];
		}

		for (PotentialAgent p : r.getPotentialAgents()) {
			disps.get(p.getID()).setText(format((double) pagentNumbers[p.getSerialID()] / sum));
		}
		invalidate();
	}

	@Override
	public void initialise(ModelData data, RunInfo info, Regions region) throws Exception {
		super.initialise(data, info, region);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		JLabel modelName = new JLabel("SimpleAllocationModel");
		add(modelName);
		
		
		r = region.getAllRegions().iterator().next();
		
		
		for (PotentialAgent p : r.getPotentialAgents()) {
			Box b = new Box(BoxLayout.X_AXIS);
			JLabel lab = new JLabel(p.getID() + ": ");
			lab.setPreferredSize(new Dimension(170, 15));
			b.add(lab);

			disps.put(p.getID(), new JLabel(format(10.0)));
			// disp.setPreferredSize(new Dimension(80, 15));
			// disp.setMinimumSize(new Dimension(80, 15));
			b.add(disps.get(p.getID()));
			b.setAlignmentX(1);
			add(b);
		}
		this.update();
		invalidate();
	}

	public String format(double d) {
		return String.format("%7.4f", d);
	}
}
