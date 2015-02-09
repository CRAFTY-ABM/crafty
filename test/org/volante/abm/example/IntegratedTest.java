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
 */
package org.volante.abm.example;

import static org.volante.abm.agent.Agent.NOT_MANAGED_COMPETITION;
import static org.volante.abm.agent.Agent.NOT_MANAGED_ID;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.volante.abm.agent.PotentialAgent;
import org.volante.abm.data.Cell;
import org.volante.abm.data.ModelData;
import org.volante.abm.data.Region;
import org.volante.abm.data.RegionSet;
import org.volante.abm.models.ProductionModel;
import org.volante.abm.models.WorldSynchronisationModel;
import org.volante.abm.schedule.DefaultSchedule;
import org.volante.abm.schedule.RunInfo;
import org.volante.abm.schedule.WorldSyncSchedule;
import org.volante.abm.testutils.NullWorldSynchronisationModel;

/**
 * A simple test of the integrated system. A couple of potential agents,
 * a few cells, setting demand for different services, and making sure that
 * the agents get swapped out as they should
 * @author dmrust
 *
 */
public class IntegratedTest extends BasicTestsUtils
{
	public static double [] farmingProduction = new double[] { 0, 0, 1, 0 };
	public static double [][] farmingCapital = new double[][] {
			{0, 0, 0, 0, 0, 0, 0}, //Housing
			{0, 0, 0, 0, 0, 0, 0}, //Timber
			{0, 0, 0, 0, 0, 1, 0}, //Food
			{0, 0, 0, 0, 0, 0, 0} //Recreation
	};
	
	public static double [] forestProduction = new double[] { 0, 1, 0, 0 };
	public static double [][] forestCapital = new double[][] {
			{0, 0, 0, 0, 0, 0, 0}, //Housing
			{0, 0, 0, 0, 1, 0, 0}, //Timber
			{0, 0, 0, 0, 0, 0, 0}, //Food
			{0, 0, 0, 0, 0, 0, 0} //Recreation
	};
	ProductionModel farmingProdModel = new SimpleProductionModel( farmingCapital, farmingProduction );
	ProductionModel forestProdModel = new SimpleProductionModel( forestCapital, forestProduction );
	
	ModelData data = new ModelData();
	Cell c1 = new Cell();
	Cell c2 = new Cell();
	Cell c3 = new Cell();
	Cell c4 = new Cell();
	Set<Cell> cells = new HashSet<Cell>( Arrays.asList( c1, c2, c3, c4 ) );
	SimpleAllocationModel allocation = new SimpleAllocationModel();
	StaticPerCellDemandModel demand = new StaticPerCellDemandModel();
	SimpleCompetitivenessModel competition = new SimpleCompetitivenessModel();
	SimplePotentialAgent farming = new SimplePotentialAgent("Farming", data, farmingProdModel, 1, 1 );
	SimplePotentialAgent forest = new SimplePotentialAgent("Forest", data, forestProdModel, 1, 1 );
	Set<PotentialAgent> agents = new HashSet<PotentialAgent>( Arrays.asList( farming, forest ) );
	
	Region r1 = new Region( allocation, competition, demand, agents, c1, c2, c3, c4 );
	RegionSet w;

	public IntegratedTest() {
		w = new RegionSet(r1);
		try {
			w.initialise(data, new RunInfo(), null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void integratedTest() throws Exception
	{
		competition.setRemoveCurrentLevel( true );
		for( Cell c : cells ) {
			c.setBaseCapitals( capitals( 1, 1, 1, 1, 1, 1, 1 ) );
		}
		DefaultSchedule sched = new DefaultSchedule( w );

		WorldSynchronisationModel worldSyncModel = new NullWorldSynchronisationModel();
		((WorldSyncSchedule) sched).setWorldSyncModel(worldSyncModel);

		sched.initialise( modelData, runInfo, null );
		sched.tick();
		assertUnmanaged( c1, c2, c3, c4  );
		
		demand.setDemand( c1, services( 0, 0, 10, 0 ));
		demand.updateSupply();
		assertEqualMaps( services(0,0,1,0), farming.getPotentialSupply( c1 ));
		assertEqualMaps( demand.getDemand( c1 ), services(0,0,10,0));
		assertEqualMaps( demand.getResidualDemand( c1 ), services(0,0,10,0));
		sched.tick();
		print(services(0,0,10,0).prettyPrint());
		assertUnmanaged(  c2, c3, c4  );
		assertAgent( "Farming", 10, c1 );
		
		demand.setDemand( c1, services( 0, 0, 5, 0 ));
		sched.tick();
		assertAgent( "Farming", 5, c1 );

		demand.setDemand( c1, services( 0, 0, 0.5, 0 ));
		sched.tick();
		assertUnmanaged( c1, c2, c3, c4  );
		assertAgent( NOT_MANAGED_ID, NOT_MANAGED_COMPETITION, c1 );

		demand.setDemand( c1, services( 0, 5, 0, 0 ));
		demand.setDemand( c2, services( 0, 5, 0, 0 ));
		sched.tick();
		assertUnmanaged(  c3, c4  );
		assertAgent( "Forest", 5, c1, c2 );
		
		demand.setDemand( c1, services( 0, 0, 5, 0 ));
		sched.tick();
		assertUnmanaged(  c3, c4  );
		assertAgent( "Forest", 5, c2 );
		assertAgent( "Farming", 5, c1 );
	}

}
