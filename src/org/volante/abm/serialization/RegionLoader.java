package org.volante.abm.serialization;

import java.awt.geom.Rectangle2D;
import java.util.*;

import org.apache.log4j.Logger;
import org.simpleframework.xml.*;
import org.volante.abm.agent.*;
import org.volante.abm.data.*;
import org.volante.abm.example.*;
import org.volante.abm.institutions.*;
import org.volante.abm.models.*;
import org.volante.abm.schedule.RunInfo;
import org.volante.abm.update.Updater;

import com.google.common.collect.*;

/**
 * Class to load Regions from serialised data. Needs to load:
 * * competitiveness models
 * * demand model
 * * allocation model
 * * baseCapitals for all cells
 * * initial agents for all cells
 * @author dmrust
 *
 */
@Root(name="region")
public class RegionLoader
{
	@Attribute(name="id")
	String id = "Unknown";
	
	@Element(required=false)
	String competitionFile = "";
	@Element(required=false)
	CompetitivenessModel competition;
	
	@Element(required=false)
	AllocationModel allocation;
	@Element(required=false)
	String allocationFile = "";
	
	@Element(required=false)
	DemandModel demand;
	@Element(required=false)
	String demandFile = "";
	
	@Element(required=false)
	PotentialAgentList potentialAgents = new PotentialAgentList();
	@ElementList(required=false,inline=true,entry="agentFile")
	List<String> agentFileList = new ArrayList<String>();
	
	@ElementList(inline=true,required=false,empty=false,entry="cellInitialiser")
	List<CellInitialiser> cellInitialisers = new ArrayList<CellInitialiser>();
	@ElementList(required=false,inline=true,entry="cellInitialiserFile")
	List<String> cellInitialiserFiles = new ArrayList<String>();
	
	@ElementList(inline=true,required=false,empty=false,entry="agentInitialiser")
	List<AgentInitialiser> agentInitialisers = new ArrayList<AgentInitialiser>();
	@ElementList(required=false,inline=true,entry="agentInitialiserFile")
	List<String> agentInitialiserFiles = new ArrayList<String>();
	
	@ElementList(inline=true,required=false,entry="updater")
	List<Updater> updaters = new ArrayList<Updater>();
	@ElementList(inline=true,required=false,entry="updaterFile")
	List<String> updaterFiles = new ArrayList<String>();
	
	@ElementList(inline=true,required=false,entry="institution")
	List<Institution> institutions = new ArrayList<Institution>();
	@ElementList(inline=true,required=false,entry="institutionFile")
	List<String> institutionFiles = new ArrayList<String>();
	
	
	Logger log = Logger.getLogger( getClass() );
	
	ABMPersister persister;
	ModelData modelData;
	RunInfo runInfo = new RunInfo();
	Region region;
	Map<String, PotentialAgent> agentsByID = new HashMap<String, PotentialAgent>();
	Map<Integer, PotentialAgent> agentsBySerialID = new HashMap<Integer, PotentialAgent>();
	Table<Integer, Integer, Cell> cellTable = TreeBasedTable.create();

	public RegionLoader()
	{
		this( null, null );
	}
	
	public RegionLoader( ModelData data, ABMPersister persister )
	{
		this.persister = persister;
		this.modelData = data;
	}
	
	public RegionLoader( String id, String competition, String allocation, String demand, String potentialAgents, String cellInitialisers, String agentInitialisers )
	{
		this.id = id;
		this.competitionFile = competition;
		this.allocationFile = allocation;
		this.demandFile = demand;
		this.agentFileList.addAll( ABMPersister.splitTags( potentialAgents ) );
		this.cellInitialiserFiles.addAll( ABMPersister.splitTags( cellInitialisers ) );
		
		if( agentInitialisers != null )
			this.agentInitialiserFiles.addAll( ABMPersister.splitTags( agentInitialisers ) );
	}
	
	public void initialise( RunInfo info ) throws Exception
	{
		this.runInfo = info;
		if( modelData == null) modelData = new ModelData();
		if( persister == null ) persister = ABMPersister.getInstance();
		
		region = new Region();
		region.setID( id );
		persister.setRegion( region );
		
		loadAgentTypes();
		loadModels();
		initialiseCells();
		passInfoToRegion();
		initialiseAgents();
		loadInstitutions();
		loadUpdaters();
	}
	

	public void loadAgentTypes() throws Exception
	{
		for( String potentialAgentFile : agentFileList )
			potentialAgents.agents.addAll( 
					persister.readXML( PotentialAgentList.class, potentialAgentFile ).agents );
		for( PotentialAgent p : potentialAgents.agents )
		{
			agentsByID.put( p.getID(), p );
			agentsBySerialID.put( p.getSerialID(), p );
		}
		for( PotentialAgent a : agentsByID.values() )
		{
			log.info("Initialise agent type: " + a.getID() );
			a.initialise( modelData, runInfo, region );
		}
	}
	
	public void setAgent( Cell c, String agentType )
	{
		if( agentType.matches( "\\d+" ))
		{
			int idNum = Integer.parseInt( agentType );
			if( agentsBySerialID.containsKey( idNum ))
				setAgent( c, agentsBySerialID.get( idNum ));
		}
		else if( agentsByID.containsKey( agentType ))
			setAgent( c, agentsByID.get( agentType ) );
		else if (agentType.matches( "\\s*"))
		{}
		// Ignore blank agents
		else
			log.error( "Couldn't find agent by id: "+ agentType );
	}
	
	public void setAgent( Cell c, PotentialAgent pa )
	{
		region.setInitialOwnership( pa.createAgent( region, c ), c );
	}
	
	public void loadModels() throws Exception
	{
		if( allocation == null )
			allocation = persister.readXML( AllocationModel.class, allocationFile );
		if( demand == null )
			demand = persister.readXML( DemandModel.class, demandFile );
		if( competition == null )
			competition = persister.readXML( CompetitivenessModel.class, competitionFile );
		runInfo.getSchedule().register( allocation );
		runInfo.getSchedule().register( demand );
		runInfo.getSchedule().register( competition );
	}
	
	
	private void loadUpdaters() throws Exception
	{
		for( String updaterFile : updaterFiles ) 
			updaters.add( persister.readXML( Updater.class, updaterFile ) );
		for( Updater u : updaters )
		{
			u.initialise( modelData, runInfo, region );
			runInfo.getSchedule().register( u );
		}
	}
	
	private void loadInstitutions() throws Exception
	{
		for( String institutionFile : institutionFiles ) 
			institutions.add( persister.readXML( Institution.class, institutionFile ) );
		if( institutions.size() > 0 )
		{
			Institutions in = new Institutions();
			for( Institution i : institutions ) in.addInstitution( i );
			region.setInstitutions( in );
			in.initialise( modelData, runInfo, region );
			runInfo.getSchedule().register( in );
		}
		
	}

	
	public void initialiseCells() throws Exception
	{
		for( String s : cellInitialiserFiles ) cellInitialisers.add( persister.readXML( CellInitialiser.class, s ) );
		for( CellInitialiser ci : cellInitialisers ) ci.initialise( this );
		region.cellsCreated();
		log.info( "Loaded " + cellTable.size() + " cells from " + cellInitialisers.size() + " loaders");
	}
	
	public void initialiseAgents() throws Exception
	{
		for( String s : agentInitialiserFiles ) agentInitialisers.add( persister.readXML( AgentInitialiser.class, s ) );
		for( AgentInitialiser ci : agentInitialisers ) ci.initialise( this );
		region.makeUnmanagedCellsAvailable();
	}
	
	public void passInfoToRegion() throws Exception
	{
		region.setDemandModel( demand );
		region.setAllocationModel( allocation );
		region.setCompetitivenessModel( competition );
		region.addPotentialAgents( potentialAgents.agents );
		region.initialise( modelData, runInfo, null );
	}
	
	public Cell getCell( int x, int y )
	{
		if( cellTable.contains( x, y ))
			return cellTable.get( x, y );
		Cell c = new Cell(x,y);
		c.initialise( modelData, runInfo, region );
		region.addCell( c );
		cellTable.put( x, y, c );
		region.setInitialOwnership( Agent.NOT_MANAGED, c );
		return c;
	}

	/*
	 * Getters and setters
	 */
	public String getCompetitionFile() { return competitionFile; } 
	public void setCompetitionFile( String competitionFile ) { this.competitionFile = competitionFile; } 
	public CompetitivenessModel getCompetition() { return competition; } 
	public void setCompetition( CompetitivenessModel competition ) { this.competition = competition; } 
	public AllocationModel getAllocation() { return allocation; } 
	public void setAllocation( AllocationModel allocation ) { this.allocation = allocation; }
	public String getAllocationFile() { return allocationFile; } 
	public void setAllocationFile( String allocationFile ) { this.allocationFile = allocationFile; } 
	public DemandModel getDemand() { return demand; } 
	public void setDemand( DemandModel demand ) { this.demand = demand; }
	public String getDemandFile() { return demandFile; } 
	public void setDemandFile( String demandFile ) { this.demandFile = demandFile; } 
	public ABMPersister getPersister() { return persister; } 
	public void setPersister( ABMPersister persister ) { this.persister = persister; }
	public void setModelData( ModelData modelData ) { this.modelData = modelData; }
	public void setRunInfo( RunInfo runInfo ) { this.runInfo = runInfo; }
	public Region getRegion() { return region; }
	
	public static interface CellInitialiser
	{
		public void initialise( RegionLoader rl ) throws Exception;
	}
	public static interface AgentInitialiser
	{
		public void initialise( RegionLoader rl ) throws Exception;
	}
	
	public void setDefaults()
	{
		demand = new RegionalDemandModel();
		competition = new SimpleCompetitivenessModel();
		allocation = new SimpleAllocationModel();
	}
}
