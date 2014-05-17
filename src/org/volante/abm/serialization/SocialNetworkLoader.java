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
import org.volante.abm.param.RandomPa;
import org.volante.abm.schedule.RunInfo;

import de.cesr.more.basic.edge.MoreEdge;
import de.cesr.more.building.edge.MoreEdgeFactory;
import de.cesr.more.building.network.MoreNetworkService;
import de.cesr.more.geo.building.edge.MDefaultGeoEdgeFactory;
import de.cesr.more.geo.building.network.MoreGeoNetworkService;
import de.cesr.more.param.MNetworkBuildingPa;
import de.cesr.more.param.MRandomPa;
import de.cesr.more.param.reader.MMilieuNetDataCsvReader;
import de.cesr.more.param.reader.MMilieuNetLinkDataCsvReader;
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
	static private Logger				logger					= Logger.getLogger(SocialNetworkLoader.class);

	@Attribute(name = "name")
	String	name	= "Unknown";

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

	Region	region;

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
	public void initialise(ModelData data, RunInfo info, Region extent) throws Exception {
		this.region = extent;

		// read parameter
		this.pm = PmParameterManager.getInstance(region);

		this.pm.copyParamValue(RandomPa.RANDOM_SEED_INIT_NETWORK, MRandomPa.RANDOM_SEED);

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

		for (Map.Entry<String, Object> param : params.entrySet()) {
			PmParameterDefinition p = PmParameterManager.parse(param.getKey());
			if (Class.class.isAssignableFrom(p.getType())
					&& param.getValue() instanceof String) {
				pm.setParam(p, Class.forName(((String) param.getValue()).trim()));
			} else {
				pm.setParam(p, param.getValue());
			}
		}

		MoreNetworkService<Agent, MoreEdge<Agent>> networkService = initNetworkInitialiser();

		logger.info("Init social network for " + this.region.getAgents().size()
				+ " agents in region " + this + " using " + networkService);

		this.region.setNetworkService(networkService);

		for (Agent a : this.region.getAgents()) {
			if (a instanceof GeoAgent
					&& region.getNetworkService() instanceof MoreGeoNetworkService) {
				((DefaultSocialInnovationAgent) a).addToGeography();
			}
		}

		this.region.setNetwork(networkService.buildNetwork(this.region
				.getAgents()));
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
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException exception) {
			exception.printStackTrace();
		}

		return networkInitializer;
	}
}
