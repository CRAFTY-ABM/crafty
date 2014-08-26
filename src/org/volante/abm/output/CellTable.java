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


import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.volante.abm.data.Capital;
import org.volante.abm.data.Cell;
import org.volante.abm.data.ModelData;
import org.volante.abm.data.Regions;
import org.volante.abm.data.Service;
import org.volante.abm.example.SimpleProductionModel;
import org.volante.abm.schedule.RunInfo;


public class CellTable extends TableOutputter<Cell> {
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
	boolean			addGiThreshold				= false;

	@ElementList(required = false, inline = true, entry = "addServiceProductivity")
	List<String>	addServiceProductivities	= new ArrayList<String>();

	@Attribute(required = false)
	String			doubleFormat		= "0.000";

	DecimalFormat	doubleFmt			= null;

	@Override
	public void setOutputManager(Outputs outputs) {
		super.setOutputManager(outputs);

		DecimalFormatSymbols decimalSymbols = new DecimalFormat()
				.getDecimalFormatSymbols();
		decimalSymbols.setDecimalSeparator('.');
		doubleFmt = new DecimalFormat(doubleFormat, decimalSymbols);

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
		if (addAgent) {
			addColumn(new CellAgentColumn());
		}
		if (addCompetitiveness) {
			addColumn(new CellCompetitivenessColumn());
		}

		if (addGiThreshold) {
			addColumn(new CellGiThresholdColumn());
		}
	}

	@Override
	public Iterable<Cell> getData(Regions r) {
		return r.getAllCells();
	}

	@Override
	public String getDefaultOutputName() {
		return "Cell";
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
			return "Not implemented";
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
}
