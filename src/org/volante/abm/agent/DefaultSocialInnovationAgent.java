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


import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.volante.abm.data.Cell;
import org.volante.abm.data.ModelData;
import org.volante.abm.data.Region;
import org.volante.abm.institutions.innovation.AdoptionObservation;
import org.volante.abm.institutions.innovation.Innovation;
import org.volante.abm.institutions.innovation.status.InnovationState;
import org.volante.abm.institutions.innovation.status.InnovationStates;
import org.volante.abm.institutions.innovation.status.InnovationStatus;
import org.volante.abm.institutions.innovation.status.SimpleInnovationStatus;
import org.volante.abm.models.ProductionModel;
import org.volante.abm.param.GeoPa;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import de.cesr.more.basic.agent.MAgentNetworkComp;
import de.cesr.more.basic.agent.MoreAgentNetworkComp;
import de.cesr.more.basic.edge.MoreEdge;
import de.cesr.more.basic.network.MoreNetwork;
import de.cesr.more.measures.MMeasureDescription;
import de.cesr.more.measures.node.MNodeMeasures;
import de.cesr.more.measures.node.MoreNodeMeasureSupport;
import de.cesr.parma.core.PmParameterManager;


/**
 * @author Sascha Holzhauer
 * 
 */
public class DefaultSocialInnovationAgent extends DefaultAgent implements
		SocialInnovationAgent, GeoAgent {

	/**
	 * Logger
	 */
	static private Logger										logger			= Logger.getLogger(DefaultSocialInnovationAgent.class);

	static public int											numberAdoptions	= 0;
	static public int											numberAgents	= 0;

	protected Map<Innovation, InnovationStatus>					innovations		= new LinkedHashMap<Innovation, InnovationStatus>();

	MoreAgentNetworkComp<SocialAgent, MoreEdge<SocialAgent>>	netComp			= new MAgentNetworkComp<SocialAgent, MoreEdge<SocialAgent>>(
																						this);
	protected MNodeMeasures										measures		= new MNodeMeasures();

	protected boolean initialAdoptionObservationPerformed = false;

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
		super(id, data);
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
		super(type, id, data, r, prod, givingUp, givingIn);
		numberAgents++;
	}

	/**
	 * @see org.volante.abm.agent.DefaultAgent#receiveNotification(de.cesr.more.basic.agent.MoreObservingNetworkAgent.NetworkObservation,
	 *      org.volante.abm.agent.Agent)
	 */
	@Override
	public void receiveNotification(NetworkObservation observation, Agent object) {
		if (observation instanceof AdoptionObservation) {
			Innovation innovation = ((AdoptionObservation) observation)
					.getInnovation();
			if (innovation.getAffectedAFTs().contains(this.getType().getID())) {
				this.makeAware(innovation);
			}
		} else {
			for (InnovationStatus istate : innovations.values()) {
				istate.setNetworkChanged(true);
			}
		}
	}

	/**
	 * 
	 */
	protected void initialAdoptionObservation() {
		for (SocialAgent neighbour : this.region.getNetwork().getPredecessors(
				this)) {
			if (neighbour instanceof SocialInnovationAgent) {
				for (Innovation i : ((SocialInnovationAgent) neighbour)
						.getInnovationsAwareOf()) {
					if (i.getAffectedAFTs().contains(this.getType().getID())) {
						this.makeAware(i);
					}
				}
			}
		}
	}

	/**
	 * Perceive social network regarding each innovation the agent is aware of.
	 * 
	 * @see org.volante.abm.agent.SocialAgent#perceiveSocialNetwork()
	 */
	@Override
	public void perceiveSocialNetwork() {
		if (!initialAdoptionObservationPerformed) {
			this.initialAdoptionObservation();
			this.initialAdoptionObservationPerformed = true;
		}
		for (Map.Entry<Innovation, InnovationStatus> entry : innovations.entrySet()) {
			if (entry.getValue().hasNetworkChanged()) {
				perceiveSocialNetwork(entry.getKey());
				entry.getValue().setNetworkChanged(false);
			}
		}
	}

	/**
	 * Observe and set the share of social network partners that adopted the given
	 * {@link Innovation}. Considers only incoming relations.
	 * 
	 * @param i
	 *        innovation to consider
	 */
	protected void perceiveSocialNetwork(Innovation i) {
		double shareAdopters = 0.0;
		for (Object predecessor : this.region.getNetwork().getPredecessors(this)) {
			if (predecessor instanceof InnovationAgent) {
				if (((InnovationAgent) predecessor).getState(i) == InnovationStates.ADOPTED) {
					shareAdopters += 1.0;
				}
			}
		}
		innovations.get(i).setNeighbourShare(
				this.region.getNetwork().getInDegree(this) == 0 ? 0 : shareAdopters
				/ this.region.getNetwork().getInDegree(this));
	}

	/********************************
	 * Innovation actions
	 *******************************/

	/**
	 * @see org.volante.abm.agent.InnovationAgent#considerInnovationsNextStep()
	 */
	@Override
	public void considerInnovationsNextStep() {
		for (Map.Entry<Innovation, InnovationStatus> entry : this.innovations.entrySet()) {
			if (entry.getValue().getState().equals(InnovationStates.AWARE)) {
				this.considerTrial(entry.getKey());
			} else if (entry.getValue().getState().equals(InnovationStates.TRIAL)) {
				this.considerAdoption(entry.getKey());
			} else if (entry.getValue().getState().equals(InnovationStates.ADOPTED)) {
				this.considerRejection(entry.getKey());
			}
		}
	}

	/**
	 * @see org.volante.abm.agent.InnovationAgent#makeAware(org.volante.abm.institutions.innovation.Innovation)
	 */
	@Override
	public void makeAware(Innovation innovation) {
		if (!this.innovations.containsKey(innovation)) {
			this.innovations.put(innovation, new SimpleInnovationStatus());
			this.innovations.get(innovation).aware();
		} else {
			this.innovations.get(innovation).setNetworkChanged(true);
		}
	}

	/**
	 * Checks whether the share of social network partners that currently apply the given innovation
	 * multiplied by the innocation's adoption factor is equal to or greater than a random number
	 * ]0,1[.
	 * 
	 * Checks whether this agent is in {@link InnovationStates#AWARE} mode and raises a warning
	 * otherwise.
	 * 
	 * @param innovation
	 */
	@Override
	public void considerTrial(Innovation innovation) {
		// TODO implement BOs
		if (innovations.get(innovation).getState() == InnovationStates.AWARE) {

			// <- LOGGING
			if (logger.isDebugEnabled()) {
				logger.debug("Probablilty to adopt: "
						+ innovations.get(innovation).getNeighbourShare() *
						innovation.getTrialThreshold(this) + "(social network partner share: "
						+ innovations.get(innovation).getNeighbourShare() + ")");
			}
			// LOGGING ->

			if (innovations.get(innovation).getNeighbourShare()
					+ innovation.getTrialNoise() >= innovation
						.getTrialThreshold(this)) {

				this.makeTrial(innovation);
			}
		} else {
			// <- LOGGING
			logger.warn(this + "> considered trial, but the innovation >" + innovation
					+ "< is not in State AWARE!");
			// LOGGING ->
		}
	}

	/**
	 * Sets {@link InnovationStates#TRIAL}, performs the innovation and makes social network
	 * partners aware.
	 * 
	 * @param innovation
	 */
	@Override
	public void makeTrial(Innovation innovation) {
		// <- LOGGING
		if (logger.isDebugEnabled()) {
			logger.debug(this + "> trials " + innovation);
		}
		// LOGGING ->

		this.innovations.get(innovation).trial();
		innovation.perform(this);

		if (this.region.getNetwork() != null) {
			for (Agent n : this.region.getNetwork().getSuccessors(this)) {
				if (n instanceof InnovationAgent
						&& innovation.getAffectedAFTs().contains(this.getType().getID())) {
					((InnovationAgent) n).makeAware(innovation);
				}
			}
		}
	}

	/**
	 * Checks whether this agent is in {@link InnovationStates#AWARE} or mode
	 * {@link InnovationStates#TRIAL} and raises a warning otherwise.
	 * 
	 * Adoption is steered by probability (applying
	 * {@link Innovation#getAdoptionThreshold(Agent)}.
	 * 
	 * 
	 * @see org.volante.abm.agent.InnovationAgent#considerAdoption(org.volante.abm.institutions.innovation.Innovation)
	 */
	@Override
	public void considerAdoption(Innovation innovation) {
		if (innovations.get(innovation).getState() == InnovationStates.AWARE ||
				innovations.get(innovation).getState() == InnovationStates.TRIAL) {

			if (innovations.get(innovation).getNeighbourShare()
					+ innovation.getAdoptionNoise() >= innovation
						.getAdoptionThreshold(this)) {
				this.makeAdopted(innovation);
			}
		} else {
			// <- LOGGING
			logger.warn(this + "> considered adoption, but the innovation >" + innovation
					+ "< is not in State AWARE or TRIAL!");
			// LOGGING ->
		}
	}

	/**
	 * Sets {@link InnovationStates#ADOPTED} and increases adoption counter.
	 * 
	 * @param innovation
	 */
	@Override
	public void makeAdopted(Innovation innovation) {
		// <- LOGGING
		if (logger.isDebugEnabled()) {
			logger.debug(this + "> adopts " + innovation);
		}
		// LOGGING ->

		this.innovations.get(innovation).adopt();
		numberAdoptions++;
	}

	/**
	 * Does nothing
	 * 
	 * @param innovation
	 */
	@Override
	public void considerRejection(Innovation innovation) {
	}

	/**
	 * @param innovation
	 */
	@Override
	public void rejectInnovation(Innovation innovation) {
		// <- LOGGING
		if (logger.isDebugEnabled()) {
			logger.debug(this + "> rejects " + innovation);
		}
		// LOGGING ->

		this.innovations.get(innovation).reject();
		innovation.unperform(this);
	}

	/********************************
	 * Basic agent methods
	 *******************************/

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
	 * @see org.volante.abm.agent.DefaultAgent#die()
	 */
	@Override
	public void die() {
		if (this.region.getNetworkService() != null && this.region.getNetwork() != null) {
			this.region.getNetworkService().removeNode(this.region.getNetwork(), this);
		}

		if (this.region.getGeography() != null
				&& this.region.getGeography().getGeometry(this) != null) {
			this.region.getGeography().move(this, null);
		}
	}

	/********************************
	 * GETTER and SETTER
	 *******************************/

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
	 * @see org.volante.abm.agent.InnovationAgent#getState(org.volante.abm.institutions.innovation.Innovation)
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
	 * @see de.cesr.more.rs.building.MoreMilieuAgent#getMilieuGroup()
	 */
	@Override
	public int getMilieuGroup() {
		return this.getType().getSerialID();
	}

	/**
	 * @see de.cesr.more.rs.building.MoreMilieuAgent#getAgentId()
	 */
	@Override
	public String getAgentId() {
		return this.id;
	}

	/**
	 * @see org.volante.abm.agent.InnovationAgent#removeInnovation(org.volante.abm.institutions.innovation.Innovation)
	 */
	@Override
	public void removeInnovation(Innovation innvoation) {
		this.innovations.remove(innvoation);
	}

	/**
	 * Unmodifiable set of innovations this agent is aware of.
	 * 
	 * @see org.volante.abm.agent.InnovationAgent#getInnovationsAwareOf()
	 */
	@Override
	public Set<Innovation> getInnovationsAwareOf() {
		return Collections.unmodifiableSet(this.innovations.keySet());
	}
}
