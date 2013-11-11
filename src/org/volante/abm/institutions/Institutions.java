package org.volante.abm.institutions;

import java.util.*;

import org.apache.log4j.Logger;
import org.simpleframework.xml.Root;
import org.volante.abm.agent.*;
import org.volante.abm.data.*;
import org.volante.abm.schedule.*;

import com.moseph.modelutils.fastdata.*;

@Root
public class Institutions implements Institution, PreTickAction
{
	Set<Institution> institutions = new HashSet<Institution>();
	Region region;
	ModelData data;
	RunInfo info;
	Logger log = Logger.getLogger( getClass() );
	
	public void addInstitution( Institution i ) { institutions.add( i ); }
	
	public boolean isAllowed( PotentialAgent a, Cell c )
	{
		for( Institution i : institutions ) if( ! i.isAllowed( a, c )) return false;
		return true;
	}

	public void adjustCapitals( Cell c )
	{
		for( Institution i : institutions ) i.adjustCapitals( c );
	}

	public double adjustCompetitiveness( PotentialAgent agent, Cell location, UnmodifiableNumberMap<Service> provision, double competitiveness )
	{
		for( Institution i : institutions ) competitiveness = i.adjustCompetitiveness( agent, location, provision, competitiveness );
		return competitiveness;
	}
	
	public void update() 
	{ 
		for( Institution i : institutions ) i.update(); 
	}
	
	public void updateCapitals()
	{
		log.info("Adjusting capitals for Region");
		for( Cell c : region.getAllCells() ) adjustCapitals( c );
	}

	public void initialise( ModelData data, RunInfo info, Region extent ) throws Exception
	{
		this.data = data;
		this.info = info;
		this.region = extent;
		for( Institution i : institutions ) i.initialise( data, info, extent );
	}

	public void preTick()
	{
		update();
	}
}
