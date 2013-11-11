package org.volante.abm.serialization;

import java.util.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;


import org.apache.log4j.Logger;
import org.simpleframework.xml.*;
import org.volante.abm.data.*;
import org.volante.abm.example.BasicTests;
import org.volante.abm.output.Outputs;
import org.volante.abm.schedule.*;
import org.volante.abm.visualisation.*;

/**
 * The scenario loader is responsible for setting up the following things:
 * * ModelData, with appropriate Capitals, Services etc.
 * * RunInfo with scenario and run id
 * * A RegionSet to run
 * * A schedule
 * * Outputs
 * @author dmrust
 *
 */
public class ScenarioLoader
{
	ModelData modelData = new ModelData();
	RunInfo info = new RunInfo();
	@Element(name="schedule",required=false)
	Schedule schedule = new DefaultSchedule();
	RegionSet regions = new RegionSet();
	ABMPersister persister;
	
	@Element(name="capitals",required=false)
	DataTypeLoader<Capital> capitals;
	@Element(name="services",required=false)
	DataTypeLoader<Service> services;
	@Element(name="landUses",required=false)
	DataTypeLoader<LandUse> landUses;
	@Attribute(name="scenario",required=false)
	String scenario = "Unknown";
	@Attribute(name="runID",required=false)
	String runID = "";
	@Attribute(name="world",required=false)
	String worldName = "World";
	@Attribute(required=false)
	boolean useInstitutions = false;
	
	@Attribute(name="startTick",required=false)
	int startTick = 2000;
	@Attribute(name="endTick",required=false)
	int endTick = 2010;
	
	@ElementList(required=false,inline=true,entry="region")
	List<RegionLoader> regionList = new ArrayList<RegionLoader>();
	@ElementList(required=false,inline=true,entry="regionFile")
	List<String> regionFileList = new ArrayList<String>();
	
	@Element(required=false)
	WorldLoader worldLoader = null;
	@Element(required=false)
	String worldLoaderFile = null;
	
	@Element(required=false)
	Outputs outputs = new Outputs();
	
	@Element(required=false)
	String outputFile = null;
	
	Logger log = Logger.getLogger( getClass() );
	
	@Element(required=false)
	ModelDisplays displays = new ModelDisplays();
	

	
	public void initialise( RunInfo info ) throws Exception
	{
		info.setSchedule(schedule);
		this.info = info;
		persister = info.getPersister();
		persister.setContext( "s", scenario );
		persister.setContext( "w", worldName );
		info.setUseInstitutions( useInstitutions );
		if( capitals != null )
		{
			log.info("Loading captials");
			modelData.capitals = capitals.getDataTypes( persister );
		}
		log.info( "Capitals: " + modelData.capitals );
		if( services != null )
		{
			log.info("Loading Services");
			modelData.services = services.getDataTypes( persister );
		}
		log.info( "Services: " + modelData.services );
		if( landUses != null )
		{
			log.info("Loading LandUses");
			modelData.landUses = landUses.getDataTypes( persister );
		}
		log.info("LandUses: " + modelData.landUses );
		
		info.setScenario( scenario );
		info.setRunID( runID );
		
		if( worldLoader == null && worldLoaderFile != null )
			worldLoader = persister.readXML( WorldLoader.class, worldLoaderFile );
		if( worldLoader != null )
		{
			worldLoader.setModelData( modelData );
			worldLoader.initialise( info );
			regions = worldLoader.getWorld();
		}
		
		if( outputFile != null )
			outputs = persister.readXML( Outputs.class, outputFile );
		info.setOutputs( outputs );
		
		schedule.setStartTick( startTick );

		schedule.initialise( modelData, info, null );
	
		log.info("About to load regions");
		for( String s : regionFileList )
			regionList.add( persister.readXML( RegionLoader.class, s ));
		for( RegionLoader r : regionList )
		{
			r.initialise( info );
			Region reg = r.getRegion();
			regions.addRegion( reg );
		}
		log.info("Final extent: "+ regions.getExtent());
		regions.initialise( modelData, info, null );
		if( regions.getAllRegions().iterator().hasNext() )
			outputs.initialise( modelData, info, regions.getAllRegions().iterator().next() ); //TODO: fix initialisation
		else
			outputs.initialise( modelData, info, null ); //TODO: fix initialisation
		displays.initialise( modelData, info, regions );
	}
	
	public void setSchedule( Schedule sched )
	{
		this.schedule = sched;
	}
	
	public  RegionSet getRegions() { return regions; }
	

	

}
