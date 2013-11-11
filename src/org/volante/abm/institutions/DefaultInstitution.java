package org.volante.abm.institutions;

import java.util.*;
import java.util.Map.Entry;

import org.simpleframework.xml.ElementMap;
import org.volante.abm.agent.PotentialAgent;
import org.volante.abm.data.*;
import org.volante.abm.schedule.RunInfo;

import com.moseph.modelutils.fastdata.*;

public class DefaultInstitution extends AbstractInstitution
{
	DoubleMap<Service> subsidies;
	DoubleMap<Capital> adjustments;
	Map<PotentialAgent,Double> agentSubsidies = new HashMap<PotentialAgent, Double>();
	
	@ElementMap(inline=true,required=false,entry="subsidy",attribute=true,key="service")
	Map<String,Double> serialSubsidies = new HashMap<String, Double>();
	@ElementMap(inline=true,required=false,entry="adjustment",attribute=true,key="capital")
	Map<String,Double> serialAdjustments = new HashMap<String, Double>();
	@ElementMap(inline=true,required=false,entry="agentSubsidy",attribute=true,key="agent")
	Map<String,Double> serialAgentSubsidies = new HashMap<String, Double>();

	public void adjustCapitals( Cell c )
	{
		DoubleMap<Capital> adjusted = c.getModifiableEffectiveCapitals();
		adjustments.addInto( adjusted );
	}

	public double adjustCompetitiveness( PotentialAgent agent, Cell location, UnmodifiableNumberMap<Service> provision, double competitiveness )
	{
		double subsidy = provision.dotProduct( subsidies );
		competitiveness += subsidy;
		if( agentSubsidies.containsKey( agent ) ) competitiveness += agentSubsidies.get( agent );
		return competitiveness;

	}

	public boolean isAllowed( PotentialAgent agent, Cell location )
	{
		return true;
	}
	
	public void setAdjustment( UnmodifiableNumberMap<Capital> s ) { adjustments.copyFrom( s ); }
	public void setSubsidies( UnmodifiableNumberMap<Service> s ) { subsidies.copyFrom( s ); }
	public void setSubsidy( PotentialAgent a, double value ) { agentSubsidies.put( a, value ); }

	public void initialise( ModelData data, RunInfo info, Region extent ) throws Exception
	{
		super.initialise( data, info, extent );
		subsidies = data.serviceMap();
		adjustments = data.capitalMap();
		for( Entry<String, Double> e : serialSubsidies.entrySet() )
			if( data.services.contains( e.getKey() ))
				subsidies.put( data.services.forName( e.getKey() ), e.getValue() );
		for( Entry<String, Double> e : serialAdjustments.entrySet() )
			if( data.capitals.contains( e.getKey() ))
				adjustments.put( data.capitals.forName( e.getKey() ), e.getValue() );
		Map<String, PotentialAgent> agents = new HashMap<String, PotentialAgent>();
		for( PotentialAgent p : extent.getAllPotentialAgents() ) agents.put( p.getID(), p );
		for( Entry<String, Double> e : serialAgentSubsidies.entrySet() )
		{
			if( agents.containsKey( e.getKey() ))
				agentSubsidies.put( agents.get( e.getKey() ), e.getValue() );
		}
		
	}

}
