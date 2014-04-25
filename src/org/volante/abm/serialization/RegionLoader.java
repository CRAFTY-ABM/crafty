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


import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import org.volante.abm.agent.Agent;
import org.volante.abm.agent.PotentialAgent;
import org.volante.abm.data.Cell;
import org.volante.abm.data.ModelData;
import org.volante.abm.data.Region;
import org.volante.abm.example.RegionalDemandModel;
import org.volante.abm.example.SimpleAllocationModel;
import org.volante.abm.example.SimpleCompetitivenessModel;
import org.volante.abm.institutions.Institution;
import org.volante.abm.institutions.Institutions;
import org.volante.abm.models.AllocationModel;
import org.volante.abm.models.CompetitivenessModel;
import org.volante.abm.models.DemandModel;
import org.volante.abm.param.RandomPa;
import org.volante.abm.schedule.RunInfo;
import org.volante.abm.update.Updater;

import com.google.common.collect.Table;
import com.google.common.collect.TreeBasedTable;

import de.cesr.parma.core.PmParameterManager;


/**
 * Class to load Regions from serialised data. Needs to load: * competitiveness models * demand
 * model * allocation model * baseCapitals for all cells * initial agents for all cells
 * 
 * @author dmrust
 * 
 */
@Root(name = "region")
public class RegionLoader {

	static int						currentUid				= 0;

	@Attribute(name = "id")
	String							id						= "Unknown";

	/**
	 * Required for parallel mpiJava computing:
	 */
	@Element(required = true)
	int								pid						= 0;

	@Element(required = false)
	String							competitionFile			= "";
	@Element(required = false)
	CompetitivenessModel			competition;

	@Element(required = false)
	AllocationModel					allocation;
	@Element(required = false)
	String							allocationFile			= "";

	@Element(required = false)
	DemandModel						demand;
	@Element(required = false)
	String							demandFile				= "";

	@Element(required = false)
	PotentialAgentList				potentialAgents			= new PotentialAgentList();

	@ElementList(required = false, inline = true, entry = "agentFile")
	List<String>					agentFileList			= new ArrayList<String>();

	@ElementList(inline = true, required = false, empty = false, entry = "cellInitialiser")
	List<CellInitialiser>			cellInitialisers		= new ArrayList<CellInitialiser>();
	@ElementList(required = false, inline = true, entry = "cellInitialiserFile")
	List<String>					cellInitialiserFiles	= new ArrayList<String>();

	@ElementList(inline = true, required = false, empty = false, entry = "agentInitialiser")
	List<AgentInitialiser>			agentInitialisers		= new ArrayList<AgentInitialiser>();
	@ElementList(required = false, inline = true, entry = "agentInitialiserFile")
	List<String>					agentInitialiserFiles	= new ArrayList<String>();

	@ElementList(inline = true, required = false, entry = "updater")
	List<Updater>					updaters				= new ArrayList<Updater>();
	@ElementList(inline = true, required = false, entry = "updaterFile")
	List<String>					updaterFiles			= new ArrayList<String>();

	@ElementList(inline = true, required = false, entry = "institution")
	List<Institution>				institutions			= new ArrayList<Institution>();
	@ElementList(inline = true, required = false, entry = "institutionFile")
	List<String>					institutionFiles		= new ArrayList<String>();

	@Element(required = false)
	int								randomSeed				= Integer.MIN_VALUE;

	Logger							log						= Logger.getLogger(getClass());

	ABMPersister					persister;
	ModelData						modelData;
	RunInfo							runInfo					= new RunInfo();
	Region							region;
	Map<String, PotentialAgent>		agentsByID				= new LinkedHashMap<String, PotentialAgent>();
	Map<Integer, PotentialAgent>	agentsBySerialID		= new LinkedHashMap<Integer, PotentialAgent>();
	Table<Integer, Integer, Cell>	cellTable				= TreeBasedTable.create();

	public RegionLoader() {
		this(null, null);
	}

	public RegionLoader(ModelData data, ABMPersister persister) {
		this.persister = persister;
		this.modelData = data;
	}

	public RegionLoader(String pid, String id, String competition, String allocation,
			String demand, String potentialAgents, String cellInitialisers,
			String agentInitialisers) {

		this.pid = Integer.parseInt(pid);

		this.id = id;
		this.competitionFile = competition;
		this.allocationFile = allocation;
		this.demandFile = demand;
		this.agentFileList.addAll(ABMPersister.splitTags(potentialAgents));
		this.cellInitialiserFiles.addAll(ABMPersister
				.splitTags(cellInitialisers));

		if (agentInitialisers != null) {
			this.agentInitialiserFiles.addAll(ABMPersister
					.splitTags(agentInitialisers));
		}
	}

	public void initialise(RunInfo info) throws Exception {
		this.runInfo = info;
		if (modelData == null) {
			modelData = new ModelData();
		}
		if (persister == null) {
			persister = ABMPersister.getInstance();
		}

		region = new Region();
		region.setID(id);
		persister.setRegion(region);

		loadAgentTypes();
		loadModels();
		initialiseCells();
		passInfoToRegion();
		initialiseAgents();
		loadInstitutions();
		loadUpdaters();
	}

	public void loadAgentTypes() throws Exception {
		for (String potentialAgentFile : agentFileList) {
			// <- LOGGING
			log.info("Agent file: " + potentialAgentFile);
			// LOGGING ->

			potentialAgents.agents.addAll(persister.readXML(
					PotentialAgentList.class, potentialAgentFile).agents);
		}
		for (PotentialAgent p : potentialAgents.agents) {
			agentsByID.put(p.getID(), p);
			agentsBySerialID.put(p.getSerialID(), p);
		}
		for (PotentialAgent a : agentsByID.values()) {
			log.info("Initialise agent type: " + a.getID());
			a.initialise(modelData, runInfo, region);
		}
	}

	public void setAgent(Cell c, String agentType) {
		if (agentType.matches("\\d+")) {
			int idNum = Integer.parseInt(agentType);
			if (agentsBySerialID.containsKey(idNum)) {
				setAgent(c, agentsBySerialID.get(idNum));
			}
		} else if (agentsByID.containsKey(agentType)) {
			setAgent(c, agentsByID.get(agentType));
		} else if (agentType.matches("\\s*")) {
		}
		// Ignore blank agents
		else {
			log.error("Couldn't find agent by id: " + agentType);
		}
	}

	public void setAgent(Cell c, PotentialAgent pa) {
		region.setInitialOwnership(pa.createAgent(region, c), c);
	}

	public void loadModels() throws Exception {
		if (allocation == null) {
			allocation = persister.readXML(AllocationModel.class,
					allocationFile);
		}
		if (demand == null) {
			demand = persister.readXML(DemandModel.class, demandFile);
		}
		if (competition == null) {
			competition = persister.readXML(CompetitivenessModel.class,
					competitionFile);
		}
		runInfo.getSchedule().register(allocation);
		runInfo.getSchedule().register(demand);
		runInfo.getSchedule().register(competition);
	}

	private void loadUpdaters() throws Exception {
		for (String updaterFile : updaterFiles) {
			updaters.add(persister.readXML(Updater.class, updaterFile));
		}
		for (Updater u : updaters) {
			u.initialise(modelData, runInfo, region);
			runInfo.getSchedule().register(u);
		}
	}

	private void loadInstitutions() throws Exception {
		for (String institutionFile : institutionFiles) {
			institutions.add(persister.readXML(Institution.class,
					institutionFile));
		}
		if (institutions.size() > 0) {
			Institutions in = new Institutions();
			for (Institution i : institutions) {
				in.addInstitution(i);
			}
			region.setInstitutions(in);
			in.initialise(modelData, runInfo, region);
			runInfo.getSchedule().register(in);
		}

	}

	public void initialiseCells() throws Exception {
		for (String s : cellInitialiserFiles) {
			cellInitialisers.add(persister.readXML(CellInitialiser.class, s));
		}
		for (CellInitialiser ci : cellInitialisers) {
			ci.initialise(this);
		}
		region.cellsCreated();
		log.info("Loaded " + cellTable.size() + " cells from "
				+ cellInitialisers.size() + " loaders");
	}

	public void initialiseAgents() throws Exception {
		for (String s : agentInitialiserFiles) {
			agentInitialisers.add(persister.readXML(AgentInitialiser.class, s));
		}
		for (AgentInitialiser ci : agentInitialisers) {
			ci.initialise(this);
		}
		region.makeUnmanagedCellsAvailable();
	}

	public void passInfoToRegion() throws Exception {
		region.setDemandModel(demand);
		region.setAllocationModel(allocation);
		region.setCompetitivenessModel(competition);
		region.addPotentialAgents(potentialAgents.agents);
		PmParameterManager.getInstance(region).setParam(RandomPa.RANDOM_SEED, randomSeed);
		region.initialise(modelData, runInfo, null);
	}

	public Cell getCell(int x, int y) {
		if (cellTable.contains(x, y)) {
			return cellTable.get(x, y);
		}
		Cell c = new Cell(x, y);
		c.initialise(modelData, runInfo, region);
		region.addCell(c);
		cellTable.put(x, y, c);
		region.setInitialOwnership(Agent.NOT_MANAGED, c);
		return c;
	}

	/*
	 * Getters and setters
	 */
	public String getCompetitionFile() {
		return competitionFile;
	}

	public void setCompetitionFile(String competitionFile) {
		this.competitionFile = competitionFile;
	}

	public CompetitivenessModel getCompetition() {
		return competition;
	}

	public void setCompetition(CompetitivenessModel competition) {
		this.competition = competition;
	}

	public AllocationModel getAllocation() {
		return allocation;
	}

	public void setAllocation(AllocationModel allocation) {
		this.allocation = allocation;
	}

	public String getAllocationFile() {
		return allocationFile;
	}

	public void setAllocationFile(String allocationFile) {
		this.allocationFile = allocationFile;
	}

	public DemandModel getDemand() {
		return demand;
	}

	public void setDemand(DemandModel demand) {
		this.demand = demand;
	}

	public String getDemandFile() {
		return demandFile;
	}

	public void setDemandFile(String demandFile) {
		this.demandFile = demandFile;
	}

	public ABMPersister getPersister() {
		return persister;
	}

	public void setPersister(ABMPersister persister) {
		this.persister = persister;
	}

	public void setModelData(ModelData modelData) {
		this.modelData = modelData;
	}

	public void setRunInfo(RunInfo runInfo) {
		this.runInfo = runInfo;
	}

	public Region getRegion() {
		return region;
	}

	public static interface CellInitialiser {
		public void initialise(RegionLoader rl) throws Exception;
	}

	public static interface AgentInitialiser {
		public void initialise(RegionLoader rl) throws Exception;
	}

	public void setDefaults() {
		demand = new RegionalDemandModel();
		competition = new SimpleCompetitivenessModel();
		allocation = new SimpleAllocationModel();
	}

	public int getRandomSeed() {
		return this.randomSeed;
	}

	/**
	 * @return the pid
	 */
	public int getUid() {
		return pid;
	}
}
