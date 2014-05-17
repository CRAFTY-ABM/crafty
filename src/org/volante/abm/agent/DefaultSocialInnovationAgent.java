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
 * Created by Sascha Holzhauer on 26.03.2014
 */
package org.volante.abm.agent;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.volante.abm.data.Cell;
import org.volante.abm.data.ModelData;
import org.volante.abm.data.Region;
import org.volante.abm.decision.innovations.Innovation;
import org.volante.abm.decision.innovations.InnovationState;
import org.volante.abm.decision.innovations.InnovationStates;
import org.volante.abm.decision.innovations.InnovationStatus;
import org.volante.abm.decision.innovations.SimpleInnovationStatus;
import org.volante.abm.models.ProductionModel;
import org.volante.abm.param.GeoPa;
import org.volante.abm.param.RandomPa;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import de.cesr.more.basic.agent.MAgentNetworkComp;
import de.cesr.more.basic.agent.MoreAgentNetworkComp;
import de.cesr.more.basic.edge.MoreEdge;
import de.cesr.more.basic.network.MoreNetwork;
import de.cesr.more.geo.building.network.MoreGeoNetworkService;
import de.cesr.more.measures.MMeasureDescription;
import de.cesr.more.measures.node.MNodeMeasures;
import de.cesr.more.measures.node.MoreNodeMeasureSupport;
import de.cesr.parma.core.PmParameterManager;

/**
 * @author Sascha Holzhauer
 *
 */
public class DefaultSocialInnovationAgent extends DefaultAgent implements SocialAgent,
		InnovationAgent, GeoAgent {

	/**
	 * Logger
	 */
	static private Logger logger = Logger.getLogger(DefaultSocialInnovationAgent.class);

	static public int numberAdoptions = 0;
	static public int					numberAgents		= 0;

	static final double PROBABILITY_ADOPTER = 0.05;

	protected Map<Innovation, InnovationStatus>					innovations			= new LinkedHashMap<Innovation, InnovationStatus>();

	MoreAgentNetworkComp<SocialAgent, MoreEdge<SocialAgent>>	netComp				=
																							new MAgentNetworkComp<SocialAgent, MoreEdge<SocialAgent>>(
																									this);
	protected MNodeMeasures										measures			= new MNodeMeasures();

	public DefaultSocialInnovationAgent() {
		super();
		numberAgents++;
	}

	/**
	 * @param id
	 *        agent id
	 * @param data
	 *        model data
	 */
	public DefaultSocialInnovationAgent(String id, ModelData data) {
		this.id = id;
		initialise(data);
		numberAgents++;
	}

	/**
	 * Mainly used for testing purposes
	 * 
	 * @param type
	 *        potential agent
	 * @param id
	 *        agent id
	 * @param data
	 *        model data
	 * @param r
	 *        region
	 * @param prod
	 *        production model
	 * @param givingUp
	 *        giving up threshold
	 * @param givingIn
	 *        giving in threshold
	 */
	public DefaultSocialInnovationAgent(PotentialAgent type, String id, ModelData data,
			Region r, ProductionModel prod, double givingUp, double givingIn) {
		this.type = type;
		this.region = r;
		this.production = prod;
		this.givingUp = givingUp;
		this.givingIn = givingIn;
		initialise(data);
		this.id = id;

		numberAgents++;
	}

	/**
	 * @see org.volante.abm.agent.SocialAgent#perceiveSocialNetwork()
	 */
	@Override
	public void perceiveSocialNetwork() {
		for (Innovation i : innovations.keySet()) {
			perceiveSocialNetwork(i);
		}
	}

	/**
	 * @param i
	 *        innovation to consider
	 */
	protected void perceiveSocialNetwork(Innovation i) {
		double shareAdopters = 0.0;
		for (Object successor : this.region.getNetwork().getPredecessors(this)) {
			if (successor instanceof InnovationAgent) {
				if (((InnovationAgent) successor).getState(i) == InnovationStates.ADOPTED) {
					shareAdopters += 1.0;
				}
			}
		}
		innovations.get(i).setAdoptedNeighbourShare(shareAdopters
				/ this.region.getNetwork().getInDegree(this));
	}

	/**
	 * @see org.volante.abm.agent.DefaultAgent#die()
	 */
	@Override
	public void die() {
		if (this.region.getNetworkService() != null) {
			this.region.getNetworkService().removeNode(this.region.getNetwork(), this);
			if (this.region.getNetworkService() instanceof MoreGeoNetworkService) {
				this.region.getGeography().move(this, null);
			}
		}
	}

	/**
	 * @see org.volante.abm.agent.SocialAgent#decideAdoption()
	 */
	@Override
	public void decideAdoption() {
		for (Map.Entry<Innovation, InnovationStatus> i : this.innovations.entrySet()) {

			// TODO implement BOs
			if (i.getValue().getState() == InnovationStates.AWARE) {
				if (i.getValue().getAdoptedNeighbourShare() >= this.region.getRandom()
							.getURService().getGenerator(RandomPa.RANDOM_SEED_RUN.name())
							.nextDouble()) {
					i.getValue().adopt();
					i.getKey().perform(this);
					numberAdoptions++;

					// <- LOGGING
					logger.info(this
							+ "> Adopted " + i.getKey());
					// LOGGING ->
				}
			}
		}
	}

	/**
	 * @see de.cesr.more.basic.agent.MoreNetworkAgent#setNetworkComp(de.cesr.more.basic.agent.MoreAgentNetworkComp)
	 */
	@Override
	public void setNetworkComp(MoreAgentNetworkComp<SocialAgent, MoreEdge<SocialAgent>> netComp) {
		this.netComp = netComp;
	}

	/**
	 * @see de.cesr.more.basic.agent.MoreNetworkAgent#getNetworkComp()
	 */
	@Override
	public MoreAgentNetworkComp<SocialAgent, MoreEdge<SocialAgent>> getNetworkComp() {
		return this.netComp;
	}

	/**
	 * @see de.cesr.more.measures.node.MoreNodeMeasureSupport#setNetworkMeasureObject(de.cesr.more.basic.network.MoreNetwork,
	 *      de.cesr.more.measures.MMeasureDescription, java.lang.Number)
	 */
	@Override
	public void setNetworkMeasureObject(
			MoreNetwork<? extends MoreNodeMeasureSupport, ?> network,
			MMeasureDescription key, Number value) {
		this.measures.setNetworkMeasureObject(network, key, value);
	}

	/**
	 * @see de.cesr.more.measures.node.MoreNodeMeasureSupport#getNetworkMeasureObject(de.cesr.more.basic.network.MoreNetwork,
	 *      de.cesr.more.measures.MMeasureDescription)
	 */
	@Override
	public Number getNetworkMeasureObject(
			MoreNetwork<? extends MoreNodeMeasureSupport, ?> network,
			MMeasureDescription key) {
		return this.measures.getNetworkMeasureObject(network, key);
	}

	/**
	 * @see org.volante.abm.agent.InnovationAgent#getState(org.volante.abm.decision.innovations.Innovation)
	 */
	@Override
	public InnovationState getState(Innovation innovation) {
		if (this.innovations.containsKey(innovation)) {
			return this.innovations.get(innovation).getState();
		} else {
			return InnovationStates.UNAWARE;
		}
	}

	/**
	 * @see org.volante.abm.agent.InnovationAgent#makeAware(org.volante.abm.decision.innovations.Innovation)
	 */
	@Override
	public void makeAware(Innovation innovation) {
		if (!this.innovations.containsKey(innovation)) {
			this.innovations.put(innovation, new SimpleInnovationStatus());
		}
	}

	/**
	 * TODO Check concept!
	 * 
	 * @param innovation
	 */
	@Override
	public void makeTrial(Innovation innovation) {
		// <- LOGGING
		logger.info(this + "> adopts " + innovation);
		// LOGGING ->

		this.innovations.get(innovation).adopt();
		for (Agent n : this.region.getNetwork().getSuccessors(this)) {
			if (n instanceof InnovationAgent) {
				((InnovationAgent) n).makeAware(innovation);
			}
		}
	}

	/**
	 * Preliminary!
	 * 
	 * @see org.volante.abm.agent.GeoAgent#addToGeography()
	 */
	@Override
	public void addToGeography() {
		Cell c = this.cells.iterator().next();
		Geometry geom = getRegion().getGeoFactory().createPoint(
				new Coordinate(c.getX()
						*
						((Double) PmParameterManager.getInstance(region).getParam(
								GeoPa.AGENT_COORD_FACTOR)).doubleValue(),
						c.getY()
								* ((Double) PmParameterManager.getInstance(region)
										.getParam(GeoPa.AGENT_COORD_FACTOR))
										.doubleValue()));
		this.getRegion().getGeography().move(this, geom);
	}

	/**
	 * @see org.volante.abm.agent.InnovationAgent#getProductionModel()
	 */
	@Override
	public ProductionModel getProductionModel() {
		return this.production;
	}

	/**
	 * @see org.volante.abm.agent.AbstractAgent#toString()
	 */
	@Override
	public String toString() {
		// TODO adapt to base cell when implemented
		if (this.cells.size() > 0) {
			Cell c = this.cells.iterator().next();
			return this.id + "_" + c.getX() + "-" + c.getY();
		} else {
			return this.id;
		}
	}

	/**
	 * @see de.cesr.more.rs.building.MoreMilieuAgent#getMilieuGroup()
	 */
	@Override
	public int getMilieuGroup() {
		return 1;
	}

	/**
	 * @see de.cesr.more.rs.building.MoreMilieuAgent#getAgentId()
	 */
	@Override
	public String getAgentId() {
		return this.id;
	}
}
