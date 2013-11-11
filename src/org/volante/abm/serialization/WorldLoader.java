package org.volante.abm.serialization;

import java.io.IOException;
import java.util.*;

import org.simpleframework.xml.*;
import org.volante.abm.data.*;
import org.volante.abm.schedule.RunInfo;

import com.csvreader.CsvReader;

public class WorldLoader
{
	@ElementList(required=false,inline=true,entry="region")
	List<RegionLoader> loaders = new ArrayList<RegionLoader>();
	@ElementList(required=false,inline=true,entry="regionFile")
	List<String> regionFiles = new ArrayList<String>();
	@ElementList(required=false,inline=true,entry="regionCSV")
	List<String> regionCSV = new ArrayList<String>();
	
	@Attribute(required=false)
	String idColumn = "ID";
	@Attribute(required=false)
	String competitionColumn = "Competition";
	@Attribute(required=false)
	String allocationColumn = "Allocation";
	@Attribute(required=false)
	String demandColumn = "Demand";
	@Attribute(required=false)
	String potentialColumn = "Agents";
	@Attribute(required=false)
	String cellColumn = "Cell Initialisers";
	@Attribute(required=false)
	String agentColumn = "Agent Initialisers";
	
	@Attribute(required=false)
	boolean world = true;
	
	ABMPersister persister = ABMPersister.getInstance();
	ModelData modelData = new ModelData();
	RunInfo info = new RunInfo();
	
	public WorldLoader() {}
	public WorldLoader( ModelData data, ABMPersister persister )
	{
		this.modelData = data;
		this.persister = persister;
	}

	public void initialise( RunInfo info ) throws Exception
	{
		this.info = info;
		for( String l : regionFiles ) loaders.add( persister.readXML( RegionLoader.class, l ) );
		for( String c : regionCSV ) loaders.addAll( allLoaders( c ));
	}
	
	public RegionSet getWorld() throws Exception
	{
		RegionSet rs = world ? new World() : new RegionSet();
		for( RegionLoader rl : loaders ) rs.addRegion( loadRegion( rl ) );
		return rs;
	}
	
	Region loadRegion( RegionLoader l ) throws Exception
	{
		l.setPersister( persister );
		l.setModelData( modelData );
		l.initialise( info );
		return l.getRegion();
	}
	
	Set<RegionLoader> allLoaders( String csvFile ) throws IOException
	{
		Set<RegionLoader> loaders = new HashSet<RegionLoader>();
		CsvReader reader = persister.getCSVReader( csvFile );
		while( reader.readRecord() ) loaders.add( loaderFromCSV( reader ));
		return loaders;
	}
	
	RegionLoader loaderFromCSV( CsvReader reader ) throws IOException
	{
		return new RegionLoader( reader.get(idColumn), reader.get(competitionColumn), reader.get(allocationColumn),
				reader.get(demandColumn), reader.get(potentialColumn), reader.get(cellColumn), null );
	}
	
	public void setModelData( ModelData data ) { this.modelData = data; }
}
