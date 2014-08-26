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
 * Created by Sascha Holzhauer on 28.03.2014
 */
package org.volante.abm.decision.innovations;


import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.volante.abm.agent.InnovationAgent;


/**
 * @author Sascha Holzhauer
 *
 */
public class EvaluatingInnovationStatus extends SimpleInnovationStatus {

	/**
	 * Logger
	 */
	static private Logger	logger					= Logger.getLogger(EvaluatingInnovationStatus.class);

	InnovationAgent	agent;
	List<Double>			competitivenessHistory	= new ArrayList<Double>();
	List<Double>			productionHistory		= new ArrayList<Double>();
	List<Double>			risidualHistory			= new ArrayList<Double>();

	int						tickOfTrialStart;
	double					evaluationFactor		= 1.05;

	/**
	 * @return the tickOfTrialStart
	 */
	public int getTickOfTrialStart() {
		return tickOfTrialStart;
	}

	public EvaluatingInnovationStatus(InnovationAgent agent, double evaluationFactor) {
		this.agent = agent;
		this.evaluationFactor = evaluationFactor;
		this.state = InnovationStates.AWARE;
	}

	/**
	 * @see org.volante.abm.decision.innovations.InnovationStatus#setAdopted(boolean)
	 */
	@Override
	public void trial() {
		super.trial();
		tickOfTrialStart = this.agent.getRegion().getRinfo().getSchedule().getCurrentTick();
	}

	@Override
	public void adopt() {
		// do nothing!
	}

	public void record() {
		competitivenessHistory.add(this.agent.getCompetitiveness());
		productionHistory.add(this.agent.supply(this.agent.getCells().iterator().next()).getDouble(
				this.agent.getRegion().getModelData().services.get(2)));
		if (this.competitivenessHistory.size() != this.agent.getRegion().getRinfo().getSchedule()
				.getCurrentTick() - this.tickOfTrialStart + 1) {
			logger.warn(this
					+ "> record() has been invoked more or less than every tick since tick of adoption!");
		}
	}

	public void evaluate() {
		assert getState() == InnovationStates.TRIAL;

		double competitivenessSum = 0.0;
		for (Double c : this.competitivenessHistory) {
			competitivenessSum += c;
		}
		if (competitivenessSum / (this.agent.getRegion().getRinfo().getSchedule().getCurrentTick() -
				this.tickOfTrialStart + 1) < competitivenessHistory.get(0) * evaluationFactor) {
			this.state = InnovationStates.REJECTED;
		} else {
			this.state = InnovationStates.ADOPTED;
		}
	}
}
