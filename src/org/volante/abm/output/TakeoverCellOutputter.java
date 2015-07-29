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
 * Created by Sascha Holzhauer on 29 Jul 2014
 */
package org.volante.abm.output;


import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.simpleframework.xml.Attribute;
import org.volante.abm.agent.Agent;
import org.volante.abm.agent.PotentialAgent;
import org.volante.abm.data.ModelData;
import org.volante.abm.data.Region;
import org.volante.abm.data.Regions;
import org.volante.abm.models.AllocationModel;
import org.volante.abm.models.utils.TakeoverMessenger;
import org.volante.abm.models.utils.TakeoverObserver;
import org.volante.abm.output.TakeoverCellOutputter.RegionPotentialAgent;
import org.volante.abm.schedule.RunInfo;
import org.volante.abm.serialization.GloballyInitialisable;


/**
 * Uses Observer pattern: Registers at those {@link AllocationModel}s that
 * implement {@link TakeoverMessenger}. This is useful since the component that
 * knows about take-overs is an exchangeable component and this way not every
 * {@link AllocationModel} is required to implement the service. Furthermore,
 * this way take-overs only need to be reported in case there is a
 * {@link TakeoverObserver} registered.
 * 
 * NOTE: Assumes that AFT IDs do not leave out any number (i.e. max(AFT-ID) ==
 * length(AFTs))
 * 
 * @author Sascha Holzhauer
 * 
 */
public class TakeoverCellOutputter extends TableOutputter<RegionPotentialAgent> implements
		GloballyInitialisable,
		TakeoverObserver {

	@Attribute(required = false)
	boolean					addTick			= true;

	@Attribute(required = false)
	boolean					addRegion		= true;

	int						maxAftID		= -1;

	Map<Region, int[][]>	numTakeOvers	= new HashMap<Region, int[][]>();

	/**
	 * @see org.volante.abm.serialization.GloballyInitialisable#initialise(org.volante.abm.data.ModelData,
	 *      org.volante.abm.schedule.RunInfo, org.volante.abm.data.Regions)
	 */
	@Override
	public void initialise(ModelData data, RunInfo info, Regions regions) throws Exception {
		for (Region r : regions.getAllRegions()) {
			if (r.getAllocationModel() instanceof TakeoverMessenger) {
				((TakeoverMessenger) r.getAllocationModel()).registerTakeoverOberserver(this);
			}
		}
	}

	/**
	 * @see org.volante.abm.output.AbstractOutputter#setOutputManager(org.volante.abm.output.Outputs)
	 */
	@Override
	public void setOutputManager(Outputs outputs) {
		super.setOutputManager(outputs);

		if (addTick) {
			addColumn(new TickColumn<RegionPotentialAgent>());
		}

		if (addRegion) {
			addColumn(new RegionsColumn<RegionPotentialAgent>());
		}

		addColumn(new TakeOverAfTColumn());
	}

	public void initTakeOvers(Region region) {
		numTakeOvers.put(region, new int[region.getPotentialAgents().size()][region
				.getPotentialAgents().size()]);
		if (maxAftID + 1 < region.getPotentialAgents().size()) {
			for (int i = maxAftID + 1; i < region.getPotentialAgents().size(); i++) {
				for (PotentialAgent pa : region.getPotentialAgents()) {
					if (pa.getSerialID() == i) {
						addColumn(new TakeOverColumn(pa.getID(), i));
					}
				}
			}
		}
	}

	/**
	 * @see org.volante.abm.models.utils.TakeoverObserver#setTakeover(org.volante.abm.data.Region,
	 *      org.volante.abm.agent.Agent, org.volante.abm.agent.Agent)
	 */
	public void setTakeover(Region region, Agent previousAgent, Agent newAgent) {
		numTakeOvers.get(region)[previousAgent.getType().getSerialID()][newAgent.getType()
				.getSerialID()]++;
	}

	/**
	 * @see org.volante.abm.output.TableOutputter#getData(org.volante.abm.data.Regions)
	 */
	@Override
	public Iterable<RegionPotentialAgent> getData(Regions r) {
		Collection<RegionPotentialAgent> pagents = new HashSet<RegionPotentialAgent>();
		for(Region region : r.getAllRegions()) {
			for (PotentialAgent pagent : region.getPotentialAgents()) {
				pagents.add(new RegionPotentialAgent(region, pagent));
			}
		}
		return pagents;
	}

	/**
	 * @see org.volante.abm.output.AbstractOutputter#getDefaultOutputName()
	 */
	@Override
	public String getDefaultOutputName() {
		return "TakeOvers";
	}

	/**
	 * @see org.volante.abm.output.TableOutputter#writeData(java.lang.Iterable,
	 *      org.volante.abm.data.Regions)
	 */
	public void writeData(Iterable<RegionPotentialAgent> data, Regions r) throws IOException {
		super.writeData(data, r);
		// reset:
		for (int[][] nums : numTakeOvers.values()) {
			for (int i = 0; i < nums.length; i++) {
				for (int j = 0; j < nums[i].length; j++) {
					nums[i][j] = 0;
				}
			}
		}
	}

	public class RegionPotentialAgent {
		PotentialAgent	pagent;
		Region			region;

		public RegionPotentialAgent(Region region, PotentialAgent pagent) {
			this.region = region;
			this.pagent = pagent;
		}

		public Region getRegion() {
			return this.region;
		}

		public PotentialAgent getPotentialAgent() {
			return this.pagent;
		}
	}

	public class TakeOverAfTColumn implements TableColumn<RegionPotentialAgent> {

		public TakeOverAfTColumn() {
		}

		@Override
		public String getHeader() {
			return "AFT";
		}

		@Override
		public String getValue(RegionPotentialAgent pragent, ModelData data, RunInfo info,
				Regions rs) {
			return pragent.getPotentialAgent().getID();
		}
	}

	public class TakeOverColumn implements TableColumn<RegionPotentialAgent> {
		String	aftName	= "";
		int		id;

		public TakeOverColumn(String aftName, int id) {
			this.aftName = aftName;
			this.id = id;
		}

		@Override
		public String getHeader() {
			return this.aftName;
		}

		@Override
		public String getValue(RegionPotentialAgent pragent, ModelData data, RunInfo info,
				Regions rs) {
			if (numTakeOvers.containsKey(pragent.getRegion())
					&& numTakeOvers.get(pragent.getRegion()).length > id) {
				return "" + numTakeOvers.get(pragent.getRegion())[pragent.getPotentialAgent()
						.getSerialID()][id];
			} else {
				return "0";
			}
		}
	}
}
