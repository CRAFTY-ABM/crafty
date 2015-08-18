package org.volante.abm.agent;

public interface PotentialAgentProductionObserver {
	
	/**
	 * To inform observers about changes in production.
	 * @param pa
	 */
	public void potentialAgentProductionChanged(PotentialAgent pa);

}
