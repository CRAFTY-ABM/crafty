/**
 * 
 */
package org.volante.abm.decision.bo;


import java.util.Map;

import org.volante.abm.agent.DeliberativeInnovationAgent;

import de.cesr.lara.components.LaraBehaviouralOption;
import de.cesr.lara.components.decision.LaraDecisionConfiguration;

/**
 * @author Sascha Holzhauer
 *
 */
public class InnovationBo extends
		LaraBehaviouralOption<DeliberativeInnovationAgent, InnovationBo> {

	public InnovationBo(String key, DeliberativeInnovationAgent agent) {
		super(key, agent);
	}


	@Override
	public InnovationBo getModifiedBO(DeliberativeInnovationAgent agent,
			Map preferenceUtilities) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map getSituationalUtilities(LaraDecisionConfiguration dBuilder) {
		// TODO Auto-generated method stub
		return null;
	}
}
