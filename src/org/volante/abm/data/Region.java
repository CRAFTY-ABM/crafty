package org.volante.abm.data;

import java.awt.geom.Rectangle2D;

import java.util.*;


import org.apache.log4j.Logger;
import org.volante.abm.agent.*;
import org.volante.abm.institutions.Institutions;
import org.volante.abm.models.*;
import org.volante.abm.schedule.RunInfo;

import com.google.common.collect.*;
import com.moseph.modelutils.fastdata.*;

import static org.volante.abm.agent.Agent.*;
import static java.lang.Math.*;

public class Region implements Regions
{
	/*
	 * Main data fields
	 */
	Set<Cell> cells = new HashSet<Cell>();
	Set<Agent> agents = new HashSet<Agent>();
	Set<Agent> agentsToRemove = new HashSet<Agent>();
	AllocationModel allocation;
	CompetitivenessModel competition;
	DemandModel demand;
	Set<Cell> available = new HashSet<Cell>();
	Set<PotentialAgent> potentialAgents = new HashSet<PotentialAgent>();
	ModelData data;
	String id = "UnknownRegion";
	Institutions institutions = null;

	/*
	 * Unmodifiable versions to pass out as necessary
	 */
	Set<Agent> uAgents = Collections.unmodifiableSet( agents );
	Set<PotentialAgent> uPotentialAgents = Collections.unmodifiableSet( potentialAgents );
	Set<Cell> uCells = Collections.unmodifiableSet( cells );
	Set<Cell> uAvailable = Collections.unmodifiableSet( available );
	Set<Region> uRegions = Collections.unmodifiableSet(
			new HashSet<Region>( Arrays.asList(new Region[] { this } ) ) );
	Table<Integer, Integer, Cell> cellTable = null;
	
	Extent extent = new Extent();
	
	Logger log = Logger.getLogger(getClass());
	
	/*
	 * Constructors, with initial sets of cells for convenience
	 */
	public Region() {}
	public Region( AllocationModel allocation, CompetitivenessModel competition, DemandModel demand, Set<PotentialAgent> potential, Cell...initialCells ) 
	{ 
		this( initialCells );
		potentialAgents.addAll( potential );
		this.allocation = allocation;
		this.competition = competition;
		this.demand = demand;
	}
	public Region( Cell...initialCells ) { this( Arrays.asList( initialCells )); }
	public Region( Collection<Cell> initialCells ) 
	{ 
		cells.addAll(  initialCells ); 
		available.addAll( initialCells );
		for( Cell c : initialCells ) updateExtent(c);
	}
	
	/*
	 * Initialisation
	 */
	/**
	 * Sets of the Region from a ModelData. Currently just initialises
	 * each cell in the region
	 * @param data
	 */
	public void initialise( ModelData data, RunInfo info, Region r ) throws Exception
	{
		this.data = data;
		for( Cell c : cells ) c.initialise( data, info, this ); 
		allocation.initialise( data, info, this );
		competition.initialise( data, info, this );
		demand.initialise( data, info, this );
	}
	
	/*
	 * Function accessors
	 */
	public AllocationModel getAllocationModel() { return allocation; }
	@Deprecated //Deprecated to show it should only be used in tests - normally ask the Region
	public CompetitivenessModel getCompetitionModel() { return competition; }
	public DemandModel getDemandModel() { return demand; }
	public void setDemandModel( DemandModel d ) { this.demand = d; }
	public void setAllocationModel( AllocationModel d ) { this.allocation = d; }
	public void setCompetitivenessModel( CompetitivenessModel d ) { this.competition = d; }
	public void addPotentialAgents( Collection<PotentialAgent> agents ) { this.potentialAgents.addAll( agents ); }

	
	/*
	 * Cell methods
	 */
	public void addCell( Cell c ) 
	{ 
		cells.add(c); 
		updateExtent( c );
	}
	public Collection<Cell> getCells() { return uCells; }
	public Collection<Cell> getAvailable() { return uAvailable; }
	@Deprecated
	public void setAvailable( Cell c ) { available.add( c ); }

	/*
	 * Agent methods
	 */
	public Collection<Agent> getAgents() { return uAgents; }
	public Collection<PotentialAgent> getPotentialAgents() { return uPotentialAgents; }
	public void removeAgent( Agent a )
	{
		for( Cell c : a.getCells() )
		{
			c.setOwner( NOT_MANAGED );
			c.resetSupply();
			available.add( c );
			demand.agentChange( c );
		}
		agentsToRemove.add( a );
	}
	
	public void cleanupAgents()
	{
		for( Agent a : agentsToRemove ) {
			log.trace(" removing agent " + a.getID() + " at " + a.getCells());
			agents.remove( a );
		}
		agentsToRemove.clear();
	}
	
	/*
	 * Regions methods
	 */
	public Iterable<Region> getAllRegions() { return uRegions; }
	public Iterable<Agent> getAllAgents() { return uAgents; }
	public Iterable<Cell> getAllCells() { return uCells; }
	public Iterable<PotentialAgent> getAllPotentialAgents() { return uPotentialAgents; }
	
	/*
	 * Convenience methods
	 */
	/**
	 * Gets the competitiveness of the given services on the given cell for the current
	 * demand model and level of demand
	 * @param services
	 * @param c
	 * @return
	 */
	public double getCompetitiveness( PotentialAgent agent, Cell c )
	{
		if( hasInstitutions() )
		{
			UnmodifiableNumberMap<Service> provision = agent.getPotentialSupply( c );
			double comp = competition.getCompetitveness( demand, provision, c );
			return institutions.adjustCompetitiveness( agent, c, provision, comp );
		}
		else
			return getUnadjustedCompetitiveness( agent, c );
	}
	
	/**
	 * Just used for displays and checking to see the effect without institutions
	 * @param agent
	 * @param c
	 * @return
	 */
	public double getUnadjustedCompetitiveness( PotentialAgent agent, Cell c )
	{
		return competition.getCompetitveness( demand, agent.getPotentialSupply( c ), c );
	}
	
	
	/**
	 * Gets the competitiveness of the cell's current production for the current
	 * demand model and levels of demand
	 * @param c
	 * @return
	 */
	public double getCompetitiveness( Cell c )
	{
		if( hasInstitutions() )
		{
			double comp = competition.getCompetitveness( demand, c.getSupply(), c );
			PotentialAgent a = c.getOwner() == null ? null : c.getOwner().getType();
			return institutions.adjustCompetitiveness( a, c, c.getSupply(), comp );
		}
		else return getUnadjustedCompetitiveness( c );
	}
	
	/**
	 * Just used for displays and checking, so see the effect without institutions
	 * @param c
	 * @return
	 */
	public double getUnadjustedCompetitiveness( Cell c )
	{
		if( competition == null || demand == null ) return Double.NaN;
		return competition.getCompetitveness( demand, c.getSupply(), c );
	}
	
	/**
	 * Sets the ownership of all the cells to the given agent
	 * Adds the agent to the region, removes any agents with no cells left
	 * @param a
	 * @param cells
	 */
	public void setOwnership( Agent a, Cell...cells )
	{
		a.setRegion( this );
		for( Cell c : cells )
		{
			Agent cur = c.getOwner();
			log.trace(" removing agent " + cur + " from cell " + c);
			cur.removeCell( c );
			if( cur.toRemove()) {
				log.trace("also removing agent " + cur);
				agents.remove( cur );
			}
			log.trace(" adding agent " + a + " to cell");
			a.addCell( c );
			c.setOwner( a );
			a.updateSupply();
			a.updateCompetitiveness();
			available.remove( c );
			if( demand != null ) demand.agentChange( c ); //could be null in initialisation
			if (a.getCompetitiveness() < a.getGivingUp()) 
				log.error(" Cell below new "+a.getID()+"'s GivingUp threshold: comp = " + a.getCompetitiveness() + " GU = " + a.getGivingUp());
			log.trace(" owner is now " + a);
		}
		agents.add( a );
	}
	
	/**
	 * Similar to setOwnership, but doesn't assume that anything is working yet.
	 * Useful for adding an initial population of agents
	 * @param a
	 * @param cells
	 */
	public void setInitialOwnership( Agent a, Cell...cells )
	{
		for( Cell c : cells )
		{
			a.addCell( c );
			c.setOwner( a );
			if( a != Agent.NOT_MANAGED ) available.remove( c );
		}
		if( a != Agent.NOT_MANAGED ) agents.add( a );
	}
	
	/**
	 * Sets all of the unmanaged cells to be available. Bit of a hack
	 */
	public void makeUnmanagedCellsAvailable()
	{
		for( Cell c : cells )
		{
			if( c.getOwner() == null || c.getOwner() == Agent.NOT_MANAGED )
				available.add( c );
		}
	}
	
	public String getID() { return id; }
	public void setID( String id ) { this.id= id; }
	
	private void updateExtent( Cell c )
	{
		extent.update( c );
	}

	public Extent getExtent()
	{
		return extent;
	}
	
	/**
	 * Called afeter all cells in the region have been created, to allow building a table of them
	 */
	public void cellsCreated()
	{
		for( Cell c : cells )
			updateExtent( c );
		cellTable = TreeBasedTable.create(); //Would rather use the ArrayTable below, but requires setting up ranges, and the code below doesn't work
		/*
		cellTable = ArrayTable.create( 
				Ranges.open( extent.minY, extent.maxY ).asSet( DiscreteDomains.integers() ),
				Ranges.open( extent.minX, extent.maxX ).asSet( DiscreteDomains.integers() ) );
				*/
		for( Cell c : cells )
			cellTable.put( c.getY(), c.getX(), c );
	}
	
	/**
	 * Returns the cell with the given x and y coordinates. Returns null if
	 * no cells are present or the table has not been built yet.
	 * @param x
	 * @param y
	 * @return
	 */
	public Cell getCell( int x, int y )
	{
		if( cellTable == null ) return null;
		return cellTable.get( y, x );
	}
	
	public int getNumCells() { return cells.size(); }
	
	public boolean hasInstitutions() { return institutions != null; }
	public Institutions getInstitutions() { return institutions; }
	public void setInstitutions( Institutions inst ) { this.institutions = inst; }
	public ModelData getModelData() { return data; }
	
}
