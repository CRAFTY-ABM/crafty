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
import java.util.Set;

import org.apache.log4j.Logger;
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

import com.csvreader.CsvWriter;


/**
 * Uses Observer pattern: Registers at those {@link AllocationModel}s that implement
 * {@link TakeoverMessenger}. This is useful since the component that knows about take-overs is an
 * exchangeable component and this way not every {@link AllocationModel} is required to implement
 * the service. Furthermore, this way take-overs only need to be reported in case there is a
 * {@link TakeoverObserver} registered.
 * 
 * NOTE: There is one instance of {@link TakeoverCellOutputter} that handles all regions, even if
 * <code>perRegion == true</code>.
 * 
 * NOTE: If <code>perRegion == false</code> there are columns for each AFT per region, even if
 * regions share the same set of AFTs.
 * 
 * NOTE: Assumes that AFT IDs do not leave out any number (i.e. max(AFT-ID) == length(AFTs))
 * 
 * @author Sascha Holzhauer
 * 
 */
public class TakeoverCellOutputter extends TableOutputter<RegionPotentialAgent> implements
		GloballyInitialisable,
		TakeoverObserver {

	/**
	 * Logger
	 */
	static private Logger logger = Logger.getLogger(TakeoverCellOutputter.class);

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
			// <- LOGGING
			if (logger.isDebugEnabled()) {
				logger.debug("TakeoverCellOutputter: Init (register takeover observer for) region " + r
						+ "(allocation model: " + r.getAllocationModel()
						+ ")");
			}
			// LOGGING ->

			if (r.getAllocationModel() instanceof TakeoverMessenger) {
				((TakeoverMessenger) r.getAllocationModel()).registerTakeoverOberserver(this);
			}
		}
	}

	public void startFile(String filename, Regions regions) throws IOException {
		endFile(regions);
		writers.put(regions, new CsvWriter(filename));

		Set<Region> regionsSet = new HashSet<Region>();
		for (Region reg : regions.getAllRegions()) {
			regionsSet.add(reg);
		}

		String[] headers = new String[columns.size()];

		int columnnum = -1;
		for (int i = 0; i < columns.size(); i++) {

			if (!(columns.get(i) instanceof TakeOverColumn)
					|| regionsSet.contains(((TakeOverColumn) columns.get(i)).getRegion())) {
				columnnum++;
				headers[columnnum] = columns.get(i).getHeader();
			}
		}

		String[] outputShort = new String[columnnum + 1];
		for (int i = 0; i <= columnnum; i++) {
			outputShort[i] = headers[i];
		}

		writers.get(regions).writeRecord(headers);
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

		addColumn(new TakeOverAftColumn());
	}

	public void initTakeOvers(Region region) {
		numTakeOvers.put(region, new int[region.getPotentialAgents().size()][region
				.getPotentialAgents().size()]);
		if (maxAftID + 1 < region.getPotentialAgents().size()) {
			for (int i = maxAftID + 1; i < region.getPotentialAgents().size(); i++) {
				for (PotentialAgent pa : region.getPotentialAgents()) {
					if (pa.getSerialID() == i) {
						addColumn(new TakeOverColumn(pa.getID()
								+ (this.perRegion ? "" : "[" + region.getID() + "]"), i,
								region));
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
		String[] output = new String[columns.size()];
		Set<Region> regions = new HashSet<Region>();
		for (Region reg : r.getAllRegions()) {
			regions.add(reg);
		}

		// <- LOGGING
		if (logger.isDebugEnabled()) {
			logger.debug("Write data for regions " + regions);
		}
		// LOGGING ->

		for (RegionPotentialAgent d : data) {
			if (regions.contains(d.getRegion())) {
				int columnnum = -1;
				for (int i = 0; i < columns.size(); i++) {
					// filter out TakeOverColumns that are not among requested regions (important
					// for region-specific files):
					if (!(columns.get(i) instanceof TakeOverColumn)
							|| regions.contains(((TakeOverColumn) columns.get(i)).getRegion())) {
						columnnum++;
						output[columnnum] = columns.get(i).getValue(d, modelData, runInfo,
										(columns.get(i) instanceof TakeOverColumn ? ((TakeOverColumn) columns
												.get(i)).getRegion() :
												r));
					}
				}

				String[] outputShort = new String[columnnum + 1];
				for (int i = 0; i <= columnnum; i++) {
					outputShort[i] = output[i];
				}

				writers.get(r).writeRecord(outputShort);
				writers.get(r).flush();
			}
		}
		
		// reset particular region:
		for (Region reg : r.getAllRegions()) {
			// <- LOGGING
			if (logger.isDebugEnabled()) {
				logger.debug("Reset region " + reg);
			}
			// LOGGING ->

			int[][] nums = numTakeOvers.get(reg);
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

	public class TakeOverAftColumn implements TableColumn<RegionPotentialAgent> {

		public TakeOverAftColumn() {
		}

		@Override
		public String getHeader() {
			return "AFT";
		}

		@Override
		public String getValue(RegionPotentialAgent pragent, ModelData data, RunInfo info,
				Regions rs) {
			return pragent.getPotentialAgent().getID()
					+ (TakeoverCellOutputter.this.perRegion ? "" : "["
							+ pragent.getRegion().getID() + "]");
		}
	}

	public class TakeOverColumn implements TableColumn<RegionPotentialAgent> {
		String	aftName	= "";
		int		id;
		Region	region;

		public TakeOverColumn(String aftName, int id, Region region) {
			this.aftName = aftName;
			this.id = id;
			this.region = region;
		}

		public Region getRegion() {
			return this.region;
		}

		@Override
		public String getHeader() {
			return this.aftName;
		}

		@Override
		public String getValue(RegionPotentialAgent pragent, ModelData data, RunInfo info,
				Regions rs) {
			if (pragent.getRegion() == rs.getAllRegions().iterator().next() &&
					numTakeOvers.containsKey(pragent.getRegion())
					&& numTakeOvers.get(pragent.getRegion()).length > id) {
				return "" + numTakeOvers.get(pragent.getRegion())[pragent.getPotentialAgent()
						.getSerialID()][id];
			} else {
				return "0";
			}
		}
	}
}
