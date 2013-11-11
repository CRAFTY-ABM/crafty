package org.volante.abm.serialization;

import java.util.*;

import org.simpleframework.xml.*;
import org.volante.abm.agent.PotentialAgent;

@Root
public class PotentialAgentList
{
	@ElementList(inline=true,required=false,entry="agent",empty=false)
	List<PotentialAgent> agents = new ArrayList<PotentialAgent>();

}
