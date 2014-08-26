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
 * Created by Sascha Holzhauer on 04.03.2014
 */
package org.volante.abm.serialization;


import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementMap;
import org.simpleframework.xml.ElementMapUnion;
import org.simpleframework.xml.Root;
import org.volante.abm.agent.Agent;
import org.volante.abm.agent.DefaultSocialInnovationAgent;
import org.volante.abm.agent.GeoAgent;
import org.volante.abm.data.ModelData;
import org.volante.abm.data.Region;
import org.volante.abm.param.GeoPa;
import org.volante.abm.param.RandomPa;
import org.volante.abm.param.SocialNetworkPa;
import org.volante.abm.schedule.PreTickAction;
import org.volante.abm.schedule.RunInfo;

import de.cesr.more.basic.edge.MoreEdge;
import de.cesr.more.building.edge.MoreEdgeFactory;
import de.cesr.more.building.network.MoreNetworkService;
import de.cesr.more.geo.building.edge.MDefaultGeoEdgeFactory;
import de.cesr.more.geo.building.edge.MGeoNotifyingNetworkEdgeModifier;
import de.cesr.more.geo.building.network.MoreGeoNetworkService;
import de.cesr.more.param.MBasicPa;
import de.cesr.more.param.MNetBuildBhPa;
import de.cesr.more.param.MNetBuildHdffPa;
import de.cesr.more.param.MNetworkBuildingPa;
import de.cesr.more.param.MRandomPa;
import de.cesr.more.param.reader.MMilieuNetDataCsvReader;
import de.cesr.more.param.reader.MMilieuNetLinkDataCsvReader;
import de.cesr.more.util.io.MoreIoUtilities;
import de.cesr.parma.core.PmParameterDefinition;
import de.cesr.parma.core.PmParameterManager;


/**
 * @author Sascha Holzhauer
 *
 */
@Root(name = "socialNetwork")
public class SocialNetworkLoader {

	/**
	 * Logger
	 */
	static private Logger	logger					= Logger.getLogger(SocialNetworkLoader.class);

	@Attribute(name = "name")
	String					name					= "Unknown";

	/**
	 * Location of ABT-specific CSV parameter file for network composition
	 */
	@Element(required = false, name = "abtParams")
	String					abtNetworkParamFile		= "";

	/**
	 * Location of ABT-specific CSV parameter file for social network initialisation
	 */
	@Element(required = false, name = "abtLinkParams")
	String					abtNetworkLinkParamFile	= "";

	@Element(required = false, name = "networkGeneratorClass")
	String					networkGeneratorClass	= "de.cesr.more.building.network.MWattsBetaSwBuilder.class";

	@ElementMapUnion({
			@ElementMap(inline = true, entry = "Integer", attribute = true, required = false, key = "param", valueType = Integer.class),
			@ElementMap(inline = true, entry = "Double", attribute = true, required = false, key = "param", valueType = Double.class),
			@ElementMap(inline = true, entry = "Float", attribute = true, required = false, key = "param", valueType = Float.class),
			@ElementMap(inline = true, entry = "Long", attribute = true, required = false, key = "param", valueType = Long.class),
			@ElementMap(inline = true, entry = "Character", attribute = true, required = false, key = "param", valueType = Character.class),
			@ElementMap(inline = true, entry = "Boolean", attribute = true, required = false, key = "param", valueType = Boolean.class),
			@ElementMap(inline = true, entry = "String", attribute = true, required = false, key = "param", valueType = String.class) })
	Map<String, Object>		params					= new HashMap<String, Object>();

	@Element(required = false, name = "DYN_EDGE_WEIGHT_UPDATER")
	String					edgeWeightUpdaterClass	= "de.cesr.more.manipulate.agent.MPseudoEgoNetworkProcessor";

	@Element(required = false, name = "DYN_EDGE_MANAGER")
	String					edgeManagerClass		= "de.cesr.more.manipulate.agent.MPseudoEgoNetworkProcessor";

	/**
	 * The tick (year) the social network is going to be initialised. Especially for simulations
	 * that do not define AFTs for each cell to start with it may be a good idea to initialise the
	 * network after some setting phase to omit unnecessary running time for network initialisation
	 * of a population that is going to change anyway. Of course, if the network is relevant for AFT
	 * allocation this is different.
	 */
	@Element(required = false, name = "initTick")
	int						initTick				= Integer.MIN_VALUE;

//	/**
//	 * The first milieu ID. This is relevant for reading link probabilities from CSV files.
//	 */
//	@Element(required = false, name = "firstMilieuId")
//	int						firstMilieuId			= 1;

	Region					region;

	PmParameterManager		pm;

	/**
	 * @return the pm
	 */
	public PmParameterManager getPm() {
		return pm;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the networkGeneratorClass
	 */
	public String getNetworkGeneratorClass() {
		return networkGeneratorClass;
	}

	/**
	 * Initialises parameters and creates the social network.
	 *
	 * @param data
	 * @param info
	 * @param extent
	 * @throws Exception
	 */
	public void initialise(ModelData data, final RunInfo info, Region extent) throws Exception {
		this.region = extent;

		// read parameter
		this.pm = PmParameterManager.getInstance(region);

		this.pm.copyParamValue(RandomPa.RANDOM_SEED_INIT_NETWORK, MRandomPa.RANDOM_SEED);
		
		this.pm.setParam(MNetBuildHdffPa.DISTANCE_FACTOR_FOR_DISTRIBUTION, new Double(1.0));
		
		// otherwise, non existing land manger types could be searched for forever
		this.pm.setParam(MNetBuildBhPa.DISTANT_FORCE_MILIEU, Boolean.FALSE);

		for (Map.Entry<String, Object> param : params.entrySet()) {
			PmParameterDefinition p = PmParameterManager.parse(param.getKey());
			
			// <- LOGGING
			if (logger.isDebugEnabled()) {
				logger.debug("Read param: " + p + " / " + param.getValue());
			}
			// LOGGING ->

			if (Class.class.isAssignableFrom(p.getType())
					&& param.getValue() instanceof String) {
				pm.setParam(p, Class.forName(((String) param.getValue()).trim()));
			} else {
				pm.setParam(p, param.getValue());
			}
		}
		
		// <- LOGGING
		pm.logParamValues(MBasicPa.values(), MNetworkBuildingPa.values(), GeoPa.values());
		// LOGGING ->
		
		if (abtNetworkParamFile != "") {
			pm.setParam(MNetworkBuildingPa.MILIEU_NETWORK_CSV_MILIEUS,
					info.getPersister().getFullPath(abtNetworkParamFile));
			new MMilieuNetDataCsvReader(pm).initParameters();
		}

		if (abtNetworkLinkParamFile != "") {
			pm.setParam(MNetworkBuildingPa.MILIEU_NETWORK_CSV_MILIEULINKS, info.getPersister()
					.getFullPath(abtNetworkLinkParamFile));
			new MMilieuNetLinkDataCsvReader(pm).initParameters();
		}

		final MoreNetworkService<Agent, MoreEdge<Agent>> networkService = initNetworkInitialiser();

		logger.info("Init social network inititialiser for " + this.region.getAgents().size()
				+ " agents in region " + this + " using " + networkService);

		this.region.setNetworkService(networkService);

		// allow scheduling later
		final int tick = (this.initTick == Integer.MIN_VALUE) ? info.getSchedule().getCurrentTick() + 1
				: this.initTick;

		// <- LOGGING
		if (logger.isDebugEnabled()) {
			logger.debug("Register Pretick action for tick " + tick + " at schedule " + info.getSchedule());
		}
		// LOGGING ->

		info.getSchedule().register(new PreTickAction() {

			@Override
			public void preTick() {
				if (info.getSchedule().getCurrentTick() == tick) {
					// <- LOGGING
					logger.info("Add " + region.getAgents().size() + " agents to geography...");
					// LOGGING ->
					
					for (Agent a : region.getAgents()) {
						if (a instanceof GeoAgent
								&& region.getNetworkService() instanceof MoreGeoNetworkService) {
							((DefaultSocialInnovationAgent) a).addToGeography();
						}
					}
					
					// <- LOGGING
					logger.info("Build social network...");
					// LOGGING ->
					region.setNetwork(networkService.buildNetwork(region
							.getAgents()));

					// output network
					if ((Boolean) pm.getParam(SocialNetworkPa.OUTPUT_NETWORK_AFTER_CREATION)) {
						MoreIoUtilities.outputGraph(
								region.getNetwork(),
								new File(
										info.getOutputs()
												.getOutputFilename(
														"Social-Network",
														"graphml",
														(String) pm
																.getParam(SocialNetworkPa.OUTPUT_NETWORK_AFTER_CREATION_TICKPATTERN),
														region)));
					}
				}
			}
		});
	}

	/**
	 * @return the network service
	 */
	@SuppressWarnings("unchecked")
	protected MoreNetworkService<Agent, MoreEdge<Agent>> initNetworkInitialiser() {
		MoreNetworkService<Agent, MoreEdge<Agent>> networkInitializer = null;
		try {
			networkInitializer = (MoreNetworkService<Agent, MoreEdge<Agent>>)
					Class.forName(networkGeneratorClass).getConstructor(
							MoreEdgeFactory.class, String.class, PmParameterManager.class)
							.newInstance(
									new MDefaultGeoEdgeFactory<Agent>(),
									this.name, this.pm);
			if (networkInitializer instanceof MoreGeoNetworkService) {
				((MoreGeoNetworkService<Agent, MoreEdge<Agent>>) networkInitializer).
						setGeography(region.getGeography());
				((MoreGeoNetworkService<Agent, MoreEdge<Agent>>) networkInitializer).
						setGeoRequestClass(Agent.class);
			}

		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			logger.error("Error while instanciating " + networkGeneratorClass);
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException exception) {
			exception.printStackTrace();
		}
		
		networkInitializer.setEdgeModifier(new MGeoNotifyingNetworkEdgeModifier());
		return networkInitializer;
	}
}
