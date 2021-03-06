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
package org.volante.abm.serialization;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.volante.abm.data.ModelData;
import org.volante.abm.data.Region;
import org.volante.abm.data.RegionSet;
import org.volante.abm.schedule.RunInfo;

import com.csvreader.CsvReader;


public class WorldLoader {

	/**
	 * Logger
	 */
	static private Logger	logger				= Logger.getLogger(WorldLoader.class);

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
	String institutionsColumn = "Institutions";
	@Attribute(required = false)
	String cellColumn = "Cell Initialisers";
	@Attribute(required=false)
	String agentColumn = "Agent Initialisers";

	ABMPersister persister = ABMPersister.getInstance();
	ModelData modelData = new ModelData();
	RunInfo					info				= null;
	
	public WorldLoader() {}
	public WorldLoader( ModelData data, ABMPersister persister )
	{
		this.modelData = data;
		this.persister = persister;
	}

	public void initialise( RunInfo info ) throws Exception
	{
		this.info = info;
		for( String l : regionFiles ) {
			// TODO override persister method
			loaders.add(persister.readXML(RegionLoader.class, l, null));
		}
		for( String c : regionCSV ) {
			loaders.addAll(allLoaders(BatchRunParser.parseString(c, info)));
		}
	}
	
	public RegionSet getWorld() throws Exception
	{
		RegionSet rs = new RegionSet();
		for( RegionLoader rl : loaders ) {
			rs.addRegion( loadRegion( rl ) );
		}
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
		Set<RegionLoader> loaders = new LinkedHashSet<RegionLoader>();
		// TODO override persister method
		CsvReader reader = persister.getCSVReader(csvFile, null);

		while( reader.readRecord() ) {
			if (reader.getColumnCount() <= 1) {
				logger.warn("There was no column detected in your CSV world XML file " + csvFile
						+ ". It is most" +
						"likely that columns are not separated by column!");
			}
			loaders.add( loaderFromCSV( reader ));
		}
		return loaders;
	}
	
	RegionLoader loaderFromCSV( CsvReader reader ) throws IOException
	{
		RegionLoader rl = new RegionLoader(
				BatchRunParser.parseString(reader.get(idColumn), info),
				BatchRunParser.parseString(reader.get(competitionColumn), info),
				BatchRunParser.parseString(reader.get(allocationColumn), info),
				BatchRunParser.parseString(reader.get(demandColumn), info),
				BatchRunParser.parseString(reader.get(potentialColumn), info),
				BatchRunParser.parseString(reader.get(cellColumn), info),
				null,
				BatchRunParser.parseString(reader.get(institutionsColumn), info));
		return rl;
	}
	
	public void setModelData( ModelData data ) { this.modelData = data; }
}
