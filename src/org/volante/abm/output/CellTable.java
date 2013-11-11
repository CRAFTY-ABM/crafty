package org.volante.abm.output;

import java.text.DecimalFormat;

import org.simpleframework.xml.Attribute;
import org.volante.abm.data.*;
import org.volante.abm.schedule.RunInfo;

public class CellTable extends TableOutputter<Cell>
{
	@Attribute(required=false)
	boolean addTick = true;
	@Attribute(required=false)
	boolean addRegion = true;
	@Attribute(required=false)
	boolean addCellRegion = true;
	@Attribute(required=false)
	boolean addServices = true;
	@Attribute(required=false)
	boolean addCapitals = true;
	@Attribute(required=false)
	boolean addLandUse = true;
	@Attribute(required=false)
	boolean addLandUseIndex = true;
	@Attribute(required=false)
	boolean addAgent = true;
	@Attribute(required=false)
	boolean addXY = true;
	@Attribute(required=false)
	boolean addCompetitiveness = true;
	
	@Attribute(required=false)
	String doubleFormat = "0.000";
	
	DecimalFormat doubleFmt = new DecimalFormat("0.000");
	
	public void setOutputManager(Outputs outputs)
	{
		super.setOutputManager( outputs );
		doubleFmt = new DecimalFormat(doubleFormat);
		if( addTick ) addColumn( new TickColumn<Cell>() );
		if( addRegion ) addColumn( new RegionColumn<Cell>() );
		if( addCellRegion ) addColumn( new CellRegionColumn() );
		if( addXY )
		{
			addColumn( new CellXColumn());
			addColumn( new CellYColumn());
		}
		if( addServices ) for( Service s : outputs.modelData.services ) addColumn( new CellServiceColumn( s ));
		if( addCapitals ) for( Capital s : outputs.modelData.capitals ) addColumn( new CellCapitalColumn( s ));
		if( addAgent ) addColumn( new CellAgentColumn());
		if( addCompetitiveness ) addColumn( new CellCompetitivenessColumn() );
	}

	public Iterable<Cell> getData( Regions r ) { return r.getAllCells(); }
	public String getDefaultOutputName() { return "Cell"; }

	public static class CellXColumn implements TableColumn<Cell>
	{
		public String getHeader() { return "X"; }
		public String getValue( Cell t, ModelData data, RunInfo info, Regions r ) { return t.getX()+""; }
	}
	public static class CellYColumn implements TableColumn<Cell>
	{
		public String getHeader() { return "Y"; }
		public String getValue( Cell t, ModelData data, RunInfo info, Regions r ) { return t.getY()+""; }
	}
	public static class CellRegionColumn implements TableColumn<Cell>
	{
		public String getHeader() { return "CellRegion"; }
		public String getValue( Cell t, ModelData data, RunInfo info, Regions r ) { return t.getRegionID(); }
	}
	public static class CellLandUseColumn implements TableColumn<Cell>
	{
		public String getHeader() { return "LandUse"; }
		public String getValue( Cell t, ModelData data, RunInfo info, Regions r ) { return "Not implemented"; }
	}
	public static class CellAgentColumn implements TableColumn<Cell>
	{
		public String getHeader() { return "Agent"; }
		public String getValue( Cell t, ModelData data, RunInfo info, Regions r ) { return t.getOwnerID(); }
	}
	public class CellServiceColumn implements TableColumn<Cell>
	{
		Service service;
		public CellServiceColumn( Service s ) { this.service = s; }
		public String getHeader() { return "Service:"+service.getName(); }
		public String getValue( Cell t, ModelData data, RunInfo info, Regions r ) 
			{ return doubleFmt.format( t.getSupply().getDouble( service ) );}
	}
	
	public class CellCapitalColumn implements TableColumn<Cell>
	{
		Capital capital;
		public CellCapitalColumn( Capital s ) { this.capital = s; }
		public String getHeader() { return "Capital:"+capital.getName(); }
		public String getValue( Cell t, ModelData data, RunInfo info, Regions r ) 
			{ return doubleFmt.format( t.getEffectiveCapitals().getDouble( capital ) );}
	}
	
	public class CellCompetitivenessColumn implements TableColumn<Cell>
	{
		public String getHeader() { return "Competitiveness"; }
		public String getValue( Cell t, ModelData data, RunInfo info, Regions r ) 
			{ return doubleFmt.format( t.getOwner().getCompetitiveness() ); }
	}
}
