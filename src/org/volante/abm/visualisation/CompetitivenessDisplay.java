package org.volante.abm.visualisation;

import java.awt.event.ActionListener;
import java.util.*;

import javax.swing.JFrame;

import org.volante.abm.agent.PotentialAgent;
import org.volante.abm.data.*;

public class CompetitivenessDisplay extends DatatypeDisplay<PotentialAgent> implements Display, ActionListener
{
	PotentialAgent agentType = null;

	public double getVal( Cell c )
	{
		if( agentType == null ) return Double.NaN;
		return c.getRegion().getCompetitiveness( agentType, c );
	}

	public Collection<String> getNames()
	{
		Set<String> names = new HashSet<String>();
		for( PotentialAgent a : region.getAllPotentialAgents() ) names.add( a.getID() );
		return names;
	}
	
	public void setupType( String type )
	{
		agentType = null;
		for( PotentialAgent a : region.getAllPotentialAgents() )
			if( a.getID().equals( type )) agentType = a;
	}
	
	
	

	

}
