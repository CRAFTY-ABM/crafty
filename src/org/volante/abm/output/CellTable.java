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
package org.volante.abm.output;


import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.volante.abm.data.Capital;
import org.volante.abm.data.Cell;
import org.volante.abm.data.ModelData;
import org.volante.abm.data.Region;
import org.volante.abm.data.Regions;
import org.volante.abm.data.Service;
import org.volante.abm.example.SimpleProductionModel;
import org.volante.abm.output.PreAllocationStorageCleanupRegionHelper.PreAllocData;
import org.volante.abm.schedule.RunInfo;
import org.volante.abm.serialization.GloballyInitialisable;


public class CellTable extends TableOutputter<Cell> implements GloballyInitialisable {
	@Attribute(required = false)
	boolean			addTick				= true;
	
	@Attribute(required = false)
	boolean			addRegion			= true;
	
	@Attribute(required = false)
	boolean			addCellRegion		= true;
	
	@Attribute(required = false)
	boolean			addServices			= true;
	
	@Attribute(required = false)
	boolean			addCapitals			= true;
	
	@Attribute(required = false)
	boolean			addLandUse			= true;
	
	@Attribute(required = false)
	boolean			addLandUseIndex		= true;
	
	@Attribute(required = false)
	boolean			addAgent			= true;
	
	@Attribute(required = false)
	boolean			addXY				= true;
	
	@Attribute(required = false)
	boolean			addCompetitiveness	= true;

	@Attribute(required = false)
	boolean			addPreAllocCompetitiveness	= false;

	@Attribute(required = false)
	boolean													addPreAllocLandUse			= false;
	
	@Attribute(required = false)
	boolean			addGiThreshold				= false;

	@Attribute(required = false)
	boolean			addPreAllocGuThreshold				= false;

	@ElementList(required = false, inline = true, entry = "addServiceProductivity")
	List<String>											addServiceProductivities	= new ArrayList<>();

	@Attribute(required = false)
	String													doubleFormat				= "0.000";

	@Attribute(required = false)
	int														maxIntegerDigits			= 10;

	DecimalFormat	doubleFmt			= null;

	Map<Region, PreAllocationStorageCleanupRegionHelper>	cleanupHelpers				= new HashMap<>();

	@Override
	public void initialise(ModelData data, RunInfo info, Regions regions) throws Exception {
		if (addPreAllocCompetitiveness | addPreAllocLandUse) {
			for (Region r : regions.getAllRegions()) {
				PreAllocationStorageCleanupRegionHelper helper = new PreAllocationStorageCleanupRegionHelper();
				cleanupHelpers.put(r, helper);
				r.registerHelper(this, helper);
			}
		}
	}

	@Override
	public void setOutputManager(Outputs outputs) {
		super.setOutputManager(outputs);

		NumberFormat f = NumberFormat.getInstance();
		if (f instanceof DecimalFormat) {
			((DecimalFormat) f).applyPattern(doubleFormat);
			((DecimalFormat) f).setMaximumIntegerDigits(maxIntegerDigits);
		}

		if (addTick) {
			addColumn(new TickColumn<Cell>());
		}
		if (addRegion) {
			addColumn(new RegionsColumn<Cell>());
		}
		if (addCellRegion) {
			addColumn(new CellRegionColumn());
		}
		if (addXY) {
			addColumn(new CellXColumn());
			addColumn(new CellYColumn());
		}
		if (addServices) {
			for (Service s : outputs.modelData.services) {
				addColumn(new CellServiceColumn(s));
			}
		}
		for (String serviceName : addServiceProductivities) {
			addColumn(new ServiceProductivityColumn(serviceName));
		}
		if (addCapitals) {
			for (Capital s : outputs.modelData.capitals) {
				addColumn(new CellCapitalColumn(s));
			}
		}
		if (addLandUse) {
			addColumn(new CellLandUseColumn());
		}
		if (addLandUseIndex) {
			addColumn(new CellLandUseIndexColumn());
		}
		if (addAgent) {
			addColumn(new CellAgentColumn());
		}
		if (addCompetitiveness) {
			addColumn(new CellCompetitivenessColumn());
		}

		if (addPreAllocCompetitiveness) {
			addColumn(new CellPreAllocCompetitivenessColumn());
		}

		if (addPreAllocLandUse) {
			addColumn(new CellPreAllocLandUseIndexColumn());
		}

		if (addGiThreshold) {
			addColumn(new CellGiThresholdColumn());
		}

		if (addPreAllocGuThreshold) {
			addColumn(new CellPreAllocGuThresholdColumn());
		}
	}

	/**
	 * @see org.volante.abm.output.TableOutputter#getData(org.volante.abm.data.Regions)
	 */
	@Override
	public Iterable<Cell> getData(Regions r) {
		return r.getAllCells();
	}

	/**
	 * @see org.volante.abm.output.AbstractOutputter#getDefaultOutputName()
	 */
	@Override
	public String getDefaultOutputName() {
		return "Cell";
	}

	public void writeData(Iterable<Cell> data, Regions r) throws IOException {
		super.writeData(data, r);
		for (PreAllocationStorageCleanupRegionHelper helper : cleanupHelpers.values()) {
			helper.clear();
		}
	}

	public static class CellXColumn implements TableColumn<Cell> {
		@Override
		public String getHeader() {
			return "X";
		}

		@Override
		public String getValue(Cell t, ModelData data, RunInfo info, Regions r) {
			return t.getX() + "";
		}
	}

	public static class CellYColumn implements TableColumn<Cell> {
		@Override
		public String getHeader() {
			return "Y";
		}

		@Override
		public String getValue(Cell t, ModelData data, RunInfo info, Regions r) {
			return t.getY() + "";
		}
	}

	public static class CellRegionColumn implements TableColumn<Cell> {
		@Override
		public String getHeader() {
			return "CellRegion";
		}

		@Override
		public String getValue(Cell t, ModelData data, RunInfo info, Regions r) {
			return t.getRegionID();
		}
	}

	public static class CellLandUseColumn implements TableColumn<Cell> {
		@Override
		public String getHeader() {
			return "LandUse";
		}

		@Override
		public String getValue(Cell t, ModelData data, RunInfo info, Regions r) {
			return t.getOwnerID();
		}
	}

	public static class CellLandUseIndexColumn implements TableColumn<Cell> {
		@Override
		public String getHeader() {
			return "LandUseIndex";
		}

		@Override
		public String getValue(Cell t, ModelData data, RunInfo info, Regions r) {
			return "" + (t.getOwner() != null ? t.getOwner().getType().getSerialID() : "None");
		}
	}

	public static class CellAgentColumn implements TableColumn<Cell> {
		@Override
		public String getHeader() {
			return "Agent";
		}

		@Override
		public String getValue(Cell t, ModelData data, RunInfo info, Regions r) {
			return t.getOwnerID();
		}
	}

	public class CellServiceColumn implements TableColumn<Cell> {
		Service	service;

		public CellServiceColumn(Service s) {
			this.service = s;
		}

		@Override
		public String getHeader() {
			return "Service:" + service.getName();
		}

		@Override
		public String getValue(Cell t, ModelData data, RunInfo info, Regions r) {
			return doubleFmt.format(t.getSupply().getDouble(service));
		}
	}

	public class ServiceProductivityColumn implements TableColumn<Cell> {
		Service	service;

		public ServiceProductivityColumn(String serviceName) {
			this.service = modelData.services.forName(serviceName);
		}

		@Override
		public String getHeader() {
			return "Productivity:" + service.getName();
		}

		@Override
		public String getValue(Cell t, ModelData data, RunInfo info, Regions r) {
			if (t.getOwner().getProductionModel() instanceof SimpleProductionModel) {
				return doubleFmt.format(((SimpleProductionModel) t.getOwner().getProductionModel())
						.
						getProductionWeights().getDouble(service));
			} else {
				return doubleFmt.format(Double.NaN);
			}
		}
	}

	public class CellCapitalColumn implements TableColumn<Cell> {
		Capital	capital;

		public CellCapitalColumn(Capital s) {
			this.capital = s;
		}

		@Override
		public String getHeader() {
			return "Capital:" + capital.getName();
		}

		@Override
		public String getValue(Cell t, ModelData data, RunInfo info, Regions r) {
			return doubleFmt.format(t.getEffectiveCapitals().getDouble(capital));
		}
	}

	public class CellCompetitivenessColumn implements TableColumn<Cell> {
		@Override
		public String getHeader() {
			return "Competitiveness";
		}

		@Override
		public String getValue(Cell t, ModelData data, RunInfo info, Regions r) {
			return doubleFmt.format(t.getOwner().getCompetitiveness());
		}
	}

	public class CellPreAllocCompetitivenessColumn implements TableColumn<Cell> {
		@Override
		public String getHeader() {
			return "PreAllocCompetitiveness";
		}

		@Override
		public String getValue(Cell t, ModelData data, RunInfo info, Regions r) {
			PreAllocData preAllocData = cleanupHelpers.get(r).getPreAllocData(t);
			return doubleFmt.format((preAllocData != null ? preAllocData.competitiveness
					: Double.NaN));
		}
	}

	public class CellPreAllocLandUseIndexColumn implements TableColumn<Cell> {
		@Override
		public String getHeader() {
			return "PreAllocLandUseIndex";
		}

		@Override
		public String getValue(Cell t, ModelData data, RunInfo info, Regions r) {
			PreAllocData preAllocData = cleanupHelpers.get(r).getPreAllocData(t);
			return "" + (preAllocData != null ? preAllocData.agentId : "None");
		}
	}

	public class CellGiThresholdColumn implements TableColumn<Cell> {
		@Override
		public String getHeader() {
			return "GivingInThreshold";
		}

		@Override
		public String getValue(Cell t, ModelData data, RunInfo info, Regions r) {
			return doubleFmt.format(t.getOwner().getGivingIn());
		}
	}

	public class CellPreAllocGuThresholdColumn implements TableColumn<Cell> {
		@Override
		public String getHeader() {
			return "PreAllocGivingUpThreshold";
		}

		@Override
		public String getValue(Cell t, ModelData data, RunInfo info, Regions r) {
			PreAllocData preAllocData = cleanupHelpers.get(r).getPreAllocData(t);
			return doubleFmt.format(preAllocData != null ? preAllocData.guThreshold : Double.NaN);
		}
	}
}
