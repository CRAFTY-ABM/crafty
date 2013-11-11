package org.volante.abm.update;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

import org.simpleframework.xml.*;
import org.volante.abm.agent.PotentialAgent;
import org.volante.abm.data.*;
import org.volante.abm.schedule.RunInfo;
import org.volante.abm.serialization.*;
import org.volante.abm.update.AgentTypeUpdater.CapitalUpdateFunction;

import com.csvreader.CsvReader;
import com.google.common.collect.*;

/**
 * Updates the capitals on a cell using a function for each agent
 * @author dmrust
 *
 */
public class AgentTypeUpdater extends AbstractUpdater
{
	Multimap<PotentialAgent, CapitalUpdateFunction> functions = HashMultimap.create();
	
	@ElementMap(inline=true,required=false,attribute=true,key="agent",entry="agentUpdate",value="function")
	Map<String, CapitalUpdateFunction> serialFunctions = new HashMap<String, AgentTypeUpdater.CapitalUpdateFunction>();
	
	/**
	 * Points to a csv file with capitals along the top and agents down the side
	 * First column should be the same as the "agentColumn" attribute, defaults to "Agent"
	 */
	@ElementList(required=false,inline=true,entry="csvFile")
	ArrayList<String> csvFiles = new ArrayList<String>();
	
	@Attribute(required=false)
	String agentColumn = "Agent";
	
	//Used internally to get agents by name
	Map<String, PotentialAgent> agents = new HashMap<String, PotentialAgent>();

	public void preTick()
	{
		for( Cell cell : region.getAllCells() )
			for( CapitalUpdateFunction f : functions.get( cell.getOwner().getType() ) )
				f.apply( cell );
	}

	public void initialise( ModelData data, RunInfo info, Region extent ) throws Exception
	{
		super.initialise( data, info, extent );
		for( PotentialAgent a : extent.getAllPotentialAgents() ) agents.put( a.getID(), a );
		//Load in the serialised stuff
		for( Entry<String, CapitalUpdateFunction> e : serialFunctions.entrySet() )
			if( agents.containsKey( e.getKey() ))
				functions.put( agents.get( e.getKey() ), e.getValue() );
		
		//Read in csv files if we have any
		for( String file : csvFiles ) readCSVFile( file );
		
		//And init the functions in case they need it
		for( CapitalUpdateFunction c : functions.values() ) c.initialise( data, info, extent );
	}
	
	public void readCSVFile( String CSVFile ) throws Exception
	{
		ABMPersister pers = info.getPersister();
		if( CSVFile != null && pers.csvFileOK( getClass(), CSVFile, agentColumn ))
		{
			CsvReader reader = pers.getCSVReader( CSVFile );
			while( reader.readRecord() )
			{
				String agent = reader.get(agentColumn);
				if( agent != null && agent != "" && agents.containsKey( agent ))
				{
					PotentialAgent ag = agents.get( agent );
					for( Capital c : data.capitals )
					{
						String val = reader.get( c.getName() );
						if( val != null && val != "" )
							functions.put( ag, getCSVFunction( c, Double.parseDouble( val ) ) );
					}
				}
			}
		}
	}
	
	/**
	 * Creates a new function for the value from a csv file. Defaults to proportional change functions.
	 * Override to use a different kind of function
	 * @param c
	 * @param value
	 * @return
	 */
	public CapitalUpdateFunction getCSVFunction( Capital c, double value )
	{
		return new ProportionalChangeFunction( c, value );
	}

	/**
	 * A function which updates the level of capital in the given cell
	 * @author dmrust
	 *
	 */
	public static interface CapitalUpdateFunction extends Initialisable { public void apply( Cell c ); }
	
}
