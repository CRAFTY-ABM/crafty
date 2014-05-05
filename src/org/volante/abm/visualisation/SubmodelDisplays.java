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


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.volante.abm.data.Cell;
import org.volante.abm.data.ModelData;
import org.volante.abm.data.Regions;
import org.volante.abm.models.AllocationModel;
import org.volante.abm.models.CompetitivenessModel;
import org.volante.abm.models.DemandModel;
import org.volante.abm.schedule.RunInfo;


public class SubmodelDisplays extends AbstractDisplay {
	private static final long		serialVersionUID	= -3289966236130005751L;

	JComponent						competitionPanel	= null;
	JComponent						allocationPanel		= null;
	JComponent						demandPanel			= null;
	JComponent						agentsPanel			= null;

	Display							competitionDisplay	= null;
	CompetitivenessModel			competition			= null;
	Display							demandDisplay		= null;
	DemandModel						demand				= null;
	AllocationModel					allocation			= null;

	CellDisplay						map					= null;

	Map<Displayable, Display>		displays			= new HashMap<Displayable, Display>();
	Map<JComponent, Displayable>	currentSelection	= new HashMap<JComponent, Displayable>();
	JPanel							displaysPanel		= new JPanel();

	public SubmodelDisplays() {
		map = new CellDisplay()
		{
			private static final long	serialVersionUID	= -3240414837859608881L;

			@Override
			public int getColourForCell(Cell c) {
				return Color.gray.getRGB();
			}

			@Override
			public void initialise(ModelData data, RunInfo info, Regions region) throws Exception
		{
			super.initialise(data, info, region);
			log.info("Initialised: " + this.region.getExtent() + ", height: " + height
					+ ", width: " + width);
		}

		};
		displaysPanel.setLayout(new GridLayout(2, 2));
		map.getMainPanel().setPreferredSize(new Dimension(200, 400));
		competitionPanel = modelPanel("Competition");
		allocationPanel = modelPanel("Allocation");
		demandPanel = modelPanel("Demand");
		agentsPanel = modelPanel("Agents");
		setLayout(new BorderLayout());
		add(displaysPanel, BorderLayout.CENTER);
		add(map, BorderLayout.EAST);
	}

	JComponent modelPanel(String title) {
		JPanel comp = new JPanel();
		JScrollPane pane = new JScrollPane(comp);
		pane.setBorder(new TitledBorder(new EtchedBorder(), title));
		comp.setLayout(new BorderLayout());
		displaysPanel.add(pane);
		return comp;
	}

	@Override
	public void update() {
		if (competitionDisplay != null) {
			competitionDisplay.update();
		}
		if (demandDisplay != null) {
			demandDisplay.update();
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public void cellChanged(Cell c) {
		super.cellChanged(c);
		log.debug("Cell changed! " + c);
		setCompetitivenessModel(c.getRegion().getCompetitionModel());
		setAllocationModel(c.getRegion().getAllocationModel());
		setDemandModel(c.getRegion().getDemandModel());
		update();
	}

	public void setCompetitivenessModel(CompetitivenessModel c) {
		competition = c;
		if (competition != null) {
			addDisplay(competition, competitionPanel);
		}
	}

	public void setAllocationModel(AllocationModel c) {
		allocation = c;
		if (allocation != null) {
			addDisplay(allocation, allocationPanel);
		}
	}

	public void addDisplay(Displayable s, JComponent target) {
		if (currentSelection.get(target) == s) {
			displays.get(s).update();
			return;
		}
		target.removeAll();
		try {
			if (!displays.containsKey(s)) {
				Display compDisp = s.getDisplay();
				compDisp.initialise(data, info, region);
				displays.put(s, compDisp);
			}
			Display com = displays.get(s);
			com.update();
			target.add(com.getDisplay(), BorderLayout.CENTER);
			target.invalidate();
			repaint();
			currentSelection.put(target, s);
		} catch (Exception e) {
			log.error("Couldn't set s display: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public void setDemandModel(DemandModel c) {
		demand = c;
		if (demand != null) {
			addDisplay(demand, demandPanel);
		}
	}

	@Override
	public JComponent getPanel() {
		return map.getMainPanel();
	}

	@Override
	public void initialise(ModelData data, RunInfo info, Regions region) throws Exception {
		super.initialise(data, info, region);
		map.initialise(data, info, region);
		map.update();
		cellChanged(region.getAllCells().iterator().next());
	}

	@Override
	public void setModelDisplays(ModelDisplays d) {
		super.setModelDisplays(d);
		d.registerDisplay(map);
	}

}
