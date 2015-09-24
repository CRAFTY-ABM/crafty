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
package org.volante.abm.data;


import static org.volante.abm.agent.Agent.NOT_MANAGED;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.volante.abm.agent.Agent;
import org.volante.abm.agent.PotentialAgent;
import org.volante.abm.agent.SocialAgent;
import org.volante.abm.institutions.Institutions;
import org.volante.abm.institutions.innovation.InnovationRegistry;
import org.volante.abm.models.AllocationModel;
import org.volante.abm.models.CompetitivenessModel;
import org.volante.abm.models.DemandModel;
import org.volante.abm.param.GeoPa;
import org.volante.abm.schedule.PreTickAction;
import org.volante.abm.schedule.RunInfo;
import org.volante.abm.serialization.ABMPersister;

import repast.simphony.space.gis.DefaultGeography;
import repast.simphony.space.gis.Geography;
import repast.simphony.space.gis.GeographyParameters;

import com.google.common.collect.Table;
import com.google.common.collect.TreeBasedTable;
import com.moseph.modelutils.fastdata.UnmodifiableNumberMap;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;

import de.cesr.more.basic.edge.MoreEdge;
import de.cesr.more.basic.network.MoreNetwork;
import de.cesr.more.building.network.MoreNetworkService;
import de.cesr.parma.core.PmParameterManager;


public class Region implements Regions, PreTickAction {

	/**
	 * Logger
	 */
	static private Logger	logger						= Logger.getLogger(Region.class);

	static final String		GEOGRAPHY_NAME_EXTENSION	= "_Geography";

	/*
	 * Main data fields
	 * 
	 * LinkedHashMaps are required to guarantee a defined order of agent creation
	 * which usually involves random number generation (cells > available > allocation)
	 */
	Set<Cell>				cells						= new LinkedHashSet<Cell>();
	Set<Agent>				agents						= new LinkedHashSet<Agent>();
	Set<Agent>				agentsToRemove				= new LinkedHashSet<Agent>();
	AllocationModel			allocation;
	CompetitivenessModel	competition;
	DemandModel				demand;
	Set<Cell>				available					= new LinkedHashSet<Cell>();
	Set<PotentialAgent>		potentialAgents				= new LinkedHashSet<PotentialAgent>();
	ModelData				data;
	RunInfo					rinfo;
	Institutions			institutions				= null;
	String					id							= "UnknownRegion";
	Map<String, String>		peristerContextExtra		= new HashMap<String, String>();
	
	boolean requiresEffectiveCapitalData = false;
	boolean hasCompetitivenessAdjustingInstitution = false;

	boolean skipInitialAllocation = false;

	Map<Object, RegionHelper>	helpers					= new LinkedHashMap<Object, RegionHelper>();

	InnovationRegistry		innovationRegistry			= new InnovationRegistry(this);

	/**
	 * @return the innovationRegistry
	 */
	public InnovationRegistry getInnovationRegistry() {
		return innovationRegistry;
	}

	Geography<Object>	geography;
	GeometryFactory		geoFactory;

	RegionalRandom			random			= null;

	/**
	 * @return the random
	 */
	public RegionalRandom getRandom() {
		return random;
	}

	/**
	 * @return the geoFactory
	 */
	public GeometryFactory getGeoFactory() {
		if (this.geoFactory == null) {
			// geometry factory with floating precision model (default)
			this.geoFactory =
						new GeometryFactory(new PrecisionModel(), 32632);
		}
		return geoFactory;
	}

	/**
	 * @return the geography
	 */
	public Geography<Object> getGeography() {
		if (this.geography == null) {
			// Causes the CRS factory to apply (longitude, latitude) order of
			// axis:
			// TODO
			// System.setProperty(GeoTools.FORCE_LONGITUDE_FIRST_AXIS_ORDER,
			// "true");
			GeographyParameters<Object> geoParams = new GeographyParameters<Object>();
			geoParams.setCrs((String) PmParameterManager
					.getParameter(GeoPa.CRS));

			String crsCode = geoParams.getCrs();
			this.geography = new DefaultGeography<Object>(this.id
					+ GEOGRAPHY_NAME_EXTENSION,
					crsCode);

			this.geography.setAdder(geoParams.getAdder());

			// <- LOGGING
			if (logger.isDebugEnabled()) {
				logger.debug("Geography CRS: " + this.geography.getCRS());
			}
			// LOGGING ->
		}
		return this.geography;
	}

	/**
	 * @return the rinfo
	 */
	public RunInfo getRinfo() {
		return rinfo;
	}

	MoreNetworkService<SocialAgent, ? extends MoreEdge<SocialAgent>> networkService;

	/**
	 * @return the networkService
	 */
	public MoreNetworkService<SocialAgent, ? extends MoreEdge<SocialAgent>> getNetworkService() {
		return networkService;
	}

	/**
	 * Sets the network service.
	 * 
	 * @param networkService
	 *        the networkService to set
	 */
	public void setNetworkService(
			MoreNetworkService<SocialAgent, ? extends MoreEdge<SocialAgent>> networkService) {
		this.networkService = networkService;
	}

	MoreNetwork<SocialAgent, MoreEdge<SocialAgent>> network;

	/**
	 * @return the network
	 */
	public MoreNetwork<SocialAgent, MoreEdge<SocialAgent>> getNetwork() {
		return network;
	}

	/**
	 * @param network
	 *        the network to set
	 */
	public void setNetwork(
			MoreNetwork<SocialAgent, MoreEdge<SocialAgent>> network) {
		this.network = network;
	}

	/**
	 * 
	 */
	public void perceiveSocialNetwork() {
		if (this.getNetwork() != null) {

			logger.info("Perceive social network in " + this);

			for (Agent a : this.getAgents()) {
				if (a instanceof SocialAgent) {
					((SocialAgent) a).perceiveSocialNetwork();
				}
			}

			for (RegionHelper helper : this.helpers.values()) {
				if (helper instanceof SocialRegionHelper) {
					((SocialRegionHelper) helper).socialNetworkPerceived();
				}
			}
		}
	}

	/*
	 * Unmodifiable versions to pass out as necessary
	 */
	Set<Agent>						uAgents				= Collections.unmodifiableSet(agents);
	Set<PotentialAgent>				uPotentialAgents	= Collections
																.unmodifiableSet(potentialAgents);
	Set<Cell>						uCells				= Collections.unmodifiableSet(cells);
	Set<Cell>						uAvailable			= Collections.unmodifiableSet(available);
	Set<Region>						uRegions			= Collections
																.unmodifiableSet(
																new HashSet<Region>(
																		Arrays.asList(new Region[] { this })));
	Table<Integer, Integer, Cell>	cellTable			= null;

	Extent							extent				= new Extent();

	Logger							log					= Logger.getLogger(getClass());

	/*
	 * Constructors, with initial sets of cells for convenience
	 */
	public Region() {
		PmParameterManager pm = PmParameterManager.getNewInstance(this);
		pm.setDefaultPm(PmParameterManager.getInstance(null));
		this.random = new RegionalRandom(this);
		this.random.init();
		this.peristerContextExtra.put(ABMPersister.REGION_CONTEXT_KEY, this.id);
	}

	public Region(AllocationModel allocation, CompetitivenessModel competition, DemandModel demand,
			Set<PotentialAgent> potential, Cell... initialCells) {
		this(initialCells);
		potentialAgents.addAll(potential);
		this.allocation = allocation;
		this.competition = competition;
		this.demand = demand;
	}

	public Region(Cell... initialCells) {
		this(Arrays.asList(initialCells));
	}

	public Region(Collection<Cell> initialCells) {
		this();
		cells.addAll(initialCells);
		available.addAll(initialCells);
		for (Cell c : initialCells) {
			updateExtent(c);
		}
	}

	/*
	 * Initialisation
	 */
	/**
	 * Sets of the Region from a ModelData. Currently just initialises each cell in the region
	 * 
	 * @param data
	 */
	@Override
	public void initialise(ModelData data, RunInfo info, Region r) throws Exception {
		// <- LOGGING
		logger.info("Initialise region " + this + "...");
		// LOGGING ->

		this.data = data;
		this.rinfo = info;
		for (Cell c : cells) {
			c.initialise(data, info, this);
		}
		allocation.initialise(data, info, this);
		competition.initialise(data, info, this);
		demand.initialise(data, info, this);
	}

	/*
	 * Function accessors
	 */
	public AllocationModel getAllocationModel() {
		return allocation;
	}

	@Deprecated
	// Deprecated to show it should only be used in tests - normally ask the Region
	public CompetitivenessModel getCompetitionModel() {
		return competition;
	}

	public DemandModel getDemandModel() {
		return demand;
	}

	public void setDemandModel(DemandModel d) {
		this.demand = d;
	}

	public void setAllocationModel(AllocationModel d) {
		this.allocation = d;
	}

	public void setCompetitivenessModel(CompetitivenessModel d) {
		this.competition = d;
	}

	public void addPotentialAgents(Collection<PotentialAgent> agents) {
		this.potentialAgents.addAll(agents);
	}

	/*
	 * Cell methods
	 */
	public void addCell(Cell c) {
		cells.add(c);
		updateExtent(c);
	}

	public Collection<Cell> getCells() {
		return uCells;
	}

	public Collection<Cell> getAvailable() {
		return uAvailable;
	}

	@Deprecated
	public void setAvailable(Cell c) {
		available.add(c);
	}

	/*
	 * Agent methods
	 */
	public Collection<Agent> getAgents() {
		return uAgents;
	}

	public Collection<PotentialAgent> getPotentialAgents() {
		return uPotentialAgents;
	}

	public void removeAgent(Agent a) {
		for (Cell c : a.getCells()) {
			c.setOwner(NOT_MANAGED);
			c.resetSupply();
			available.add(c);
			demand.agentChange(c);
		}
		agentsToRemove.add(a);
	}

	public void cleanupAgents() {
		for (RegionHelper helper : this.helpers.values()) {
			if (helper instanceof CleanupRegionHelper) {
				((CleanupRegionHelper) helper).cleanUp(this, agentsToRemove);
			}
		}

		for (Agent a : agentsToRemove) {
			log.trace(" removing agent " + a.getID() + " at " + a.getCells());

			agents.remove(a);
			for (RegionHelper helper : this.helpers.values()) {
				if (helper instanceof PopulationRegionHelper) {
					((PopulationRegionHelper) helper).agentRemoved(a);
				}
			}
		}
		agentsToRemove.clear();
	}

	/*
	 * Regions methods
	 */
	@Override
	public Iterable<Region> getAllRegions() {
		return uRegions;
	}

	@Override
	public Iterable<Agent> getAllAgents() {
		return uAgents;
	}

	@Override
	public Iterable<Cell> getAllCells() {
		return uCells;
	}

	@Override
	public Iterable<PotentialAgent> getAllPotentialAgents() {
		return uPotentialAgents;
	}

	/*
	 * Convenience methods
	 */
	/**
	 * Gets the competitiveness of the given services on the given cell for the
	 * current demand model and level of demand
	 * 
	 * @param agent
	 * @param c
	 * @return competitiveness for the given potential agent on the given cell
	 */
	public double getCompetitiveness(PotentialAgent agent, Cell c) {
		if (hasCompetitivenessAdjustingInstitution()) {
			UnmodifiableNumberMap<Service> provision = agent.getPotentialSupply(c);
			// same as getUnadjustedCompetitiveness() but this way omits
			// calculating provision twice:
			double comp = competition.getCompetitiveness(demand, provision, c);
			// same as getUnadjustedCompetitiveness() but this way omits
			// calculating provision twice:
			return institutions.adjustCompetitiveness(agent, c, provision, comp);
		} else {
			return getUnadjustedCompetitiveness(agent, c);
		}
	}

	/**
	 * Just used for displays and checking to see the effect without
	 * institutions
	 * 
	 * @param agent
	 * @param c
	 * @return unadjusted competitiveness for the given potential agent on the
	 *         given cell
	 */
	public double getUnadjustedCompetitiveness(PotentialAgent agent, Cell c) {
		return competition.getCompetitiveness(demand, agent.getPotentialSupply(c), c);
	}

	/**
	 * Gets the competitiveness of the cell's current production for the current
	 * demand model and levels of demand
	 * 
	 * @param c
	 * @return get competitiveness of given cell
	 */
	public double getCompetitiveness(Cell c) {
		double comp = getUnadjustedCompetitiveness(c);
		if (hasCompetitivenessAdjustingInstitution()) {
			PotentialAgent a = c.getOwner() == null ? null : c.getOwner().getType();
			return institutions.adjustCompetitiveness(a, c, c.getSupply(), comp);
		} else {
			return comp;
		}
	}

	/**
	 * Just used for displays and checking, so see the effect without
	 * institutions
	 * 
	 * @param c
	 * @return unadjusted competitiveness for the given cell
	 */
	public double getUnadjustedCompetitiveness(Cell c) {
		if (competition == null || demand == null) {
			return Double.NaN;
		}
		return competition.getCompetitiveness(demand, c.getSupply(), c);
	}

	public double getUnadjustedCompetitiveness(UnmodifiableNumberMap<Service> supply) {
		if (competition == null || demand == null) {
			return Double.NaN;
		}
		return competition.getCompetitiveness(demand, supply);
	}

	/**
	 * Sets the ownership of all the cells to the given agent Adds the agent to the region, removes
	 * any agents with no cells left
	 * 
	 * @param a
	 * @param cells
	 */
	public void setOwnership(Agent a, Cell... cells) {
		a.setRegion(this);
		for (Cell c : cells) {
			Agent cur = c.getOwner();
			log.trace(" removing agent " + cur + " from cell " + c);
			cur.removeCell(c);
			if (cur.toRemove()) {
				log.trace("also removing agent " + cur);
				agents.remove(cur);

				for (RegionHelper helper : this.helpers.values()) {
					if (helper instanceof PopulationRegionHelper) {
						((PopulationRegionHelper) helper).agentRemoved(cur);
					}
				}

				cur.die();
			}
			log.trace(" adding agent " + a + " to cell");
			a.addCell(c);
			c.setOwner(a);
			a.updateSupply();
			a.updateCompetitiveness();
			available.remove(c);
			if (demand != null) {
				demand.agentChange(c); // could be null in initialisation
			}
			if (log.isDebugEnabled() && a.getCompetitiveness() < a.getGivingUp()) {
				log.debug(" Cell below new " + a.getID() + "'s GivingUp threshold: comp = "
						+ a.getCompetitiveness() + " GU = " + a.getGivingUp());
			}
			log.trace(" owner is now " + a);
		}
		agents.add(a);
	}

	/**
	 * Similar to setOwnership, but doesn't assume that anything is working yet. Useful for adding
	 * an initial population of agents
	 * 
	 * @param a
	 * @param cells
	 */
	public void setInitialOwnership(Agent a, Cell... cells) {
		for (Cell c : cells) {
			a.addCell(c);
			c.setOwner(a);
			if (a != Agent.NOT_MANAGED) {
				available.remove(c);
			}
		}
		if (a != Agent.NOT_MANAGED) {
			agents.add(a);
		}
	}

	/**
	 * Sets all of the unmanaged cells to be available. Bit of a hack
	 */
	public void makeUnmanagedCellsAvailable() {
		for (Cell c : cells) {
			if (c.getOwner() == null || c.getOwner() == Agent.NOT_MANAGED) {
				available.add(c);
			}
		}
	}

	@Override
	public String getID() {
		return id;
	}

	public void setID(String id) {
		this.id = id;
		this.peristerContextExtra.put(ABMPersister.REGION_CONTEXT_KEY, id);
	}

	private void updateExtent(Cell c) {
		extent.update(c);
	}

	@Override
	public Extent getExtent() {
		return extent;
	}

	/**
	 * Called after all cells in the region have been created, to allow building a table of them
	 */
	public void cellsCreated()
	{
		log.info("Update Extent...");
		for (Cell c : cells) {
			// <- LOGGING
			if (log.isDebugEnabled()) {
				log.debug("Update extent by cell " + c);
			}
			// LOGGING ->

			updateExtent(c);
		}
		cellTable = TreeBasedTable.create(); // Would rather use the ArrayTable below, but requires
												// setting up ranges, and the code below doesn't
												// work
		/*
		 * cellTable = ArrayTable.create( Ranges.open( extent.minY, extent.maxY ).asSet(
		 * DiscreteDomains.integers() ), Ranges.open( extent.minX, extent.maxX ).asSet(
		 * DiscreteDomains.integers() ) );
		 */
		for (Cell c : cells) {
			cellTable.put(c.getY(), c.getX(), c);
		}
	}

	/**
	 * Returns the cell with the given x and y coordinates. Returns null if no
	 * cells are present or the table has not been built yet.
	 * 
	 * @param x
	 * @param y
	 * @return cell of given coordinates
	 */
	public Cell getCell(int x, int y) {
		if (cellTable == null) {
			return null;
		}
		return cellTable.get(y, x);
	}

	@Override
	public int getNumCells() {
		return cells.size();
	}

	public boolean hasInstitutions() {
		return institutions != null;
	}

	public boolean doesRequireEffectiveCapitalData() {
		return requiresEffectiveCapitalData;
	}

	public boolean hasCompetitivenessAdjustingInstitution() {
		return hasCompetitivenessAdjustingInstitution;
	}

	public void setRequiresEffectiveCapitalData() {
		this.requiresEffectiveCapitalData = true;
	}

	public void setHasCompetitivenessAdjustingInstitution() {
		this.hasCompetitivenessAdjustingInstitution = true;
	}

	public Institutions getInstitutions() {
		return institutions;
	}

	public void setInstitutions(Institutions inst) {
		this.institutions = inst;
	}

	public ModelData getModelData() {
		return data;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return this.getID();
	}

	/**
	 * @see org.volante.abm.schedule.PreTickAction#preTick()
	 */
	@Override
	public void preTick() {
		for (RegionHelper helper : this.helpers.values()) {
			if (helper instanceof PreTickRegionHelper) {
				((PreTickRegionHelper) helper).preTick();
				;
			}
		}

		for (Service s : data.services) {
			rinfo.getParamRepos().addParameter(this, "Demand_" + s,
					demand.getDemand().get(s));
		}
	}

	/**
	 * @param id
	 * @param rHelper
	 * @return see {@link HashMap#put(Object, Object)}
	 */
	public RegionHelper registerHelper(Object id, RegionHelper rHelper) {
		return this.helpers.put(id, rHelper);
	}

	/**
	 * @param id
	 * @return region helper with given ID
	 */
	public RegionHelper getHelper(Object id) {
		return this.helpers.get(id);
	}

	public Map<String, String> getPeristerContextExtra() {
		return this.peristerContextExtra;
	}

	public boolean isSkipInitialAllocation() {
		return skipInitialAllocation;
	}

	public void setSkipInitialAllocation(boolean skipInitialAllocation) {
		this.skipInitialAllocation = skipInitialAllocation;
	}
}
