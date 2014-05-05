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
import java.util.List;

import mpi.MPI;

import org.apache.log4j.Logger;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.volante.abm.data.Capital;
import org.volante.abm.data.LandUse;
import org.volante.abm.data.ModelData;
import org.volante.abm.data.Region;
import org.volante.abm.data.RegionSet;
import org.volante.abm.data.Service;
import org.volante.abm.example.SingleMarketWorldSynchronisationModel;
import org.volante.abm.models.WorldSynchronisationModel;
import org.volante.abm.output.Outputs;
import org.volante.abm.schedule.DefaultSchedule;
import org.volante.abm.schedule.RunInfo;
import org.volante.abm.schedule.Schedule;
import org.volante.abm.schedule.WorldSyncSchedule;
import org.volante.abm.visualisation.DefaultModelDisplays;
import org.volante.abm.visualisation.ModelDisplays;


/**
 * The scenario loader is responsible for setting up the following things:
 * <ul>
 * <li>ModelData, with appropriate Capitals, Services etc.</li>
 * <li>RunInfo with scenario and run id
 * <li>A RegionSet to run</li>
 * <li>A schedule</li>
 * <li>Outputs</li>
 * </ul>
 * 
 * If a {@link WorldLoader} is defined this initialise the {@link RegionSet}
 * first before {@link RegionLoader}s directly defined in the scenario XML file
 * add {@link Region}s to that set.
 * 
 * @author dmrust
 * 
 */
public class ScenarioLoader {
	ModelData modelData = new ModelData();
	RunInfo info = new RunInfo();
	RegionSet regions = new RegionSet();
	ABMPersister			persister		= null;

	/**
	 * Scenario name (default: "Unknown")
	 */
	@Attribute(name = "scenario", required = false)
	String scenario = "Unknown";
	
	/**
	 * World Name (default: "World")
	 */
	@Attribute(name = "world", required = false)
	String worldName = "World";

	/**
	 * run Identifier (default: "")
	 */
	@Attribute(name = "runID", required = false)
	String					runID			= "SET_INTERNAL";
	
	/**
	 * startTick (int, default: 2000)
	 */
	@Attribute(name = "startTick", required = false)
	int startTick = 2000;

	/**
	 * endTick (int, default: 2015)
	 */
	@Attribute(name = "endTick", required = false)
	int						endTick			= 2015;
	
	@Element(name = "schedule", required = false)
	Schedule schedule = new DefaultSchedule();

	@Element(name = "worldSyncModel", required = false)
	WorldSynchronisationModel	worldSyncModel	= new SingleMarketWorldSynchronisationModel();

	@Element(name = "capitals", required = false)
	DataTypeLoader<Capital>	capitals		= null;

	@Element(name = "services", required = false)
	DataTypeLoader<Service>	services		= null;

	@Element(name = "landUses", required = false)
	DataTypeLoader<LandUse>	landUses		= null;

	@Attribute(required = false)
	boolean useInstitutions = false;

	@ElementList(required = false, inline = true, entry = "region")
	List<RegionLoader> regionList = new ArrayList<RegionLoader>();
	@ElementList(required = false, inline = true, entry = "regionFile")
	List<String> regionFileList = new ArrayList<String>();

	@Element(required = false)
	WorldLoader worldLoader = null;

	@Element(required = false)
	String worldLoaderFile = null;

	@Element(required = false)
	Outputs outputs = new Outputs();

	@Element(required = false)
	String outputFile = null;

	Logger log = Logger.getLogger(getClass());

	@Element(required = false)
	ModelDisplays			displays		= null;

	/**
	 * @param info
	 * @throws Exception
	 */
	public void initialise(RunInfo info) throws Exception {
		info.setSchedule(schedule);

		this.info = info;
		persister = info.getPersister();
		persister.setContext("s", scenario);
		persister.setContext("w", worldName);
		info.setUseInstitutions(useInstitutions);
		if (capitals != null) {
			log.info("Loading captials");
			modelData.capitals = capitals.getDataTypes(persister);
		}
		log.info("Capitals: " + modelData.capitals);
		if (services != null) {
			log.info("Loading Services");
			modelData.services = services.getDataTypes(persister);
		}
		log.info("Services: " + modelData.services);
		if (landUses != null) {
			log.info("Loading LandUses");
			modelData.landUses = landUses.getDataTypes(persister);
		}
		log.info("LandUses: " + modelData.landUses);

		info.setScenario(scenario);
		info.setRunID(runID);

		if (worldLoader == null && worldLoaderFile != null) {
			worldLoader = persister.readXML(WorldLoader.class, worldLoaderFile);
		}
		if (worldLoader != null) {
			worldLoader.setModelData(modelData);
			worldLoader.initialise(info);

			// regions for parallel processing have been selected here:
			regions = worldLoader.getWorld();
		}

		if (worldSyncModel != null) {
			worldSyncModel.initialise(modelData, info);
			if (schedule instanceof WorldSyncSchedule) {
				((WorldSyncSchedule) schedule).setWorldSyncModel(worldSyncModel);
			} else {
				log.warn("WorldSynchronisationModel could not be assigned to schedule!");
			}
		}

		log.info("About to load regions");
		for (String s : regionFileList) {
			// <- LOGGING
			log.warn("This way of initialising regions for parallel computing is untested!");
			// LOGGING ->

			regionList.add(persister.readXML(RegionLoader.class, s));
		}
		for (RegionLoader rl : regionList) {
			rl.initialise(info);
			if (MPI.COMM_WORLD.Rank() == rl.getUid()) {
				Region r = rl.getRegion();
				regions.addRegion(r);

				log.info("Run region " + r + " on rank " + MPI.COMM_WORLD.Rank());
			}
		}

		if (outputFile != null) {
			outputs = persister.readXML(Outputs.class, outputFile);
		}
		info.setOutputs(outputs);

		schedule.setStartTick(startTick);
		schedule.setEndTick(endTick);

		schedule.initialise(modelData, info, null);

		log.info("Final extent: " + regions.getExtent());
		regions.initialise(modelData, info, null);
		if (regions.getAllRegions().iterator().hasNext()) {
			outputs.initialise(modelData, info, regions.getAllRegions()
					.iterator().next()); // TODO: fix initialisation
		}
		else {
			outputs.initialise(modelData, info, null); // TODO: fix
		}
		// initialisation
		if (displays == null) {
			displays = new DefaultModelDisplays();
		}
		displays.initialise(modelData, info, regions);
	}

	public void setSchedule(Schedule sched) {
		this.schedule = sched;
	}

	/**
	 * TODO doc
	 * 
	 * @param runID
	 */
	public void setRunID(String runID) {
		if (this.runID.equals("SET_INTERNAL")) {
			this.runID = runID;
		}
	}

	public RegionSet getRegions() {
		return regions;
	}
}
