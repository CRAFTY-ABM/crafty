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
package org.volante.abm.optimization;

import static org.junit.Assert.assertTrue;
import static org.volante.abm.example.SimpleCapital.NATURAL_CROPS;
import static org.volante.abm.example.SimpleCapital.NATURAL_FOREST;
import static org.volante.abm.example.SimpleCapital.NATURE_VALUE;
import static org.volante.abm.example.SimpleService.FOOD;
import static org.volante.abm.example.SimpleService.RECREATION;
import static org.volante.abm.example.SimpleService.TIMBER;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.volante.abm.agent.PotentialAgent;
import org.volante.abm.data.Capital;
import org.volante.abm.data.Cell;
import org.volante.abm.data.Region;
import org.volante.abm.example.BasicTestsUtils;
import org.volante.abm.example.RegionalDemandModel;
import org.volante.abm.example.SimpleCapital;
import org.volante.abm.param.RandomPa;

import cern.jet.random.engine.RandomEngine;

import com.google.common.collect.Multiset;

public class GeneticAlgorithmAllocationModelTest extends BasicTestsUtils
{
	/**
	 * Logger
	 */
	static private Logger	logger			= Logger.getLogger(GeneticAlgorithmAllocationModelTest.class);
	 
	//Order:
	// HUMAN INFRASTRUCTURE ECONOMIC NATURAL_GRASSLAND NATURAL_FOREST NATURAL_CROPS NATURE_VALUE
	double[] cropsOnly = { 1, 1, 1, 0, 0, 1, 0 };
	double[] forestOnly = { 1, 1, 1, 0, 1, 0, 0 };
	double[] both = { 1, 1, 1, 0, 1, 1, 0 };
	// HOUSING TIMBER FOOD RECREATION
	double[] timber = { 0, 50, 0, 0 };
	double[] food = { 0, 0, 50, 0 };
	double[] foodAndTimber = { 0, 50, 50, 0 };
	
	@Test
	public void testBasicOperation() throws Exception
	{
		GeneticAlgorithmAllocationModel alloc = new GeneticAlgorithmAllocationModel();
		RegionalDemandModel dem = new RegionalDemandModel();
		RandomEngine rEngine = r1.getRandom().getURService()
				.getGenerator(RandomPa.RANDOM_SEED_INIT.name());
		for( Cell c : r1.getCells() ) {
			c.setBaseCapitals( capitals(rEngine.nextDouble(), rEngine.nextDouble(), rEngine.nextDouble(), rEngine.nextDouble(), rEngine.nextDouble(), rEngine.nextDouble(), rEngine.nextDouble() ) );
		}
		dem.initialise( modelData, runInfo, r1 );
		r1.setDemandModel( dem );
		dem.setDemand( services(100,100,100,100) );
		alloc.addLogListener();
		alloc.initialise( modelData, runInfo, r1 );
		double initial = alloc.getCurrentFitness();
		alloc.allocateLand( r1 );
		double after = alloc.getCurrentFitness();
		assertTrue( "GA improves fitness...", after >= initial );
	}
	
	@Test
	public void testCorrectAssignment() throws Exception
	{
		Cell c00 = new Cell( 0, 0 );
		Cell c01 = new Cell( 0, 1 );
		Cell c10 = new Cell( 1, 0 );
		Cell c11 = new Cell( 1, 1 );
		Cell[] cells = { c00, c01, c10, c11 };
		
		PotentialAgent farm = getSingleProductionAgent( "Farmer", 0, 0, 10, FOOD, NATURAL_CROPS );
		PotentialAgent forest = getSingleProductionAgent( "Forester", 0, 0, 10, TIMBER, NATURAL_FOREST );
		Set<PotentialAgent> potential = new HashSet<PotentialAgent>( Arrays.asList( farm, forest ) );
		
		GeneticAlgorithmAllocationModel alloc = new GeneticAlgorithmAllocationModel();
		RegionalDemandModel dem = new RegionalDemandModel();
		Region r = setupWorld( alloc, competition, dem, potential, cells );
		
		r.setOwnership( forest.createAgent( r ), cells );
		alloc.addLogListener();
		
		setCapitals( cells, cropsOnly, cropsOnly, forestOnly, forestOnly );
		dem.setDemand( services(foodAndTimber) );
		
		double initial = alloc.getCurrentFitness();
		alloc.allocateLand( r );
		double after = alloc.getCurrentFitness();
		assertTrue( "GA improves fitness...", after >= initial );
		checkOwnership( cells, farm, farm, forest, forest );
		
		setCapitals( cells, both, both, both, both );
		dem.setDemand( services(food) );
		alloc.allocateLand( r );
		checkOwnership( cells, farm, farm, farm, farm );
		
		dem.setDemand( services(timber) );
		alloc.allocateLand( r );
		checkOwnership( cells, forest, forest, forest, forest );
	}
	
	@Test
	public void testBigWorld() throws Exception
	{
		int numX = 50;
		int numY = 50;
		Cell[] cells = new Cell[numX*numY];
		for( int x = 0; x < numX; x++ ) {
			for( int y = 0; y < numY; y++ ) {
				cells[x+y*numX] = new Cell( x, y );
			}
		}
		
		PotentialAgent farm = getSingleProductionAgent( "Farmer", -10, 0, 1, FOOD, NATURAL_CROPS );
		PotentialAgent forest = getSingleProductionAgent( "Forester", -10, 0, 1, TIMBER, NATURAL_FOREST );
		PotentialAgent nature = getSingleProductionAgent( "Nature", -10, 0, 1, RECREATION, NATURE_VALUE );
		Set<PotentialAgent> potential = new HashSet<PotentialAgent>( Arrays.asList( farm, forest, nature ) );
		
		GeneticAlgorithmAllocationModel alloc = new GeneticAlgorithmAllocationModel();
		alloc.convergenceProportion = 0.0005;
		alloc.chromosomeMutationRate = 0.8;
		alloc.cellMutationRate = 0.1;
		alloc.elitismRate = 0.05;
		//alloc.addLogListener();
		
		RegionalDemandModel dem = new RegionalDemandModel();
		
		Region r = setupWorld( alloc, competition, dem, potential, cells );
		//Make sure demand is the maximum of each service which could be supplied if the region was perfect
		dem.setDemand( services(0, numX*numY, numX*numY, numX*numY) );
		RandomEngine rEngine = r1.getRandom().getURService()
				.getGenerator(RandomPa.RANDOM_SEED_INIT.name());
		for( Cell c : cells ) {
			for( Capital cap : SimpleCapital.simpleCapitals ) {
				c.getModifiableBaseCapitals().putDouble( cap, rEngine.nextDouble() );
			}
		}
		
		double initial = alloc.getCurrentFitness();
		alloc.allocateLand( r );
		double after = alloc.getCurrentFitness();
		logger.error("Before: " + initial + " -> After: " + after);
		assertTrue( "GA improves fitness...", after >= initial );
		Multiset<PotentialAgent> randomSet = countAgents( r );
		logger.info(randomSet);
		logger.info("Demand: " + r.getDemandModel().getDemand().prettyPrint());
		logger.info("Supply: " + r.getDemandModel().getSupply().prettyPrint());
		
		dem.setDemand( services(0, 0, 3*numX*numY, 0 ));
		initial = alloc.getCurrentFitness();
		alloc.allocateLand( r );
		after = alloc.getCurrentFitness();
		logger.error("Before: " + initial + " -> After: " + after);
		assertTrue( "GA improves fitness...", after >= initial );
		Multiset<PotentialAgent> foodSet = countAgents( r );
		logger.info(foodSet);
		logger.info("Demand: " + r.getDemandModel().getDemand().prettyPrint());
		logger.info("Supply: " + r.getDemandModel().getSupply().prettyPrint());
		assertTrue( "More farmers when there's high food demand (Should always work!)", foodSet.count( farm ) > randomSet.count( farm ));
		assertTrue( "Less foresters when there's high food demand (Mostly works - stochastic)", foodSet.count( forest ) < randomSet.count( forest ));
		assertTrue( "Less nature when there's high food demand (often fails - there's normally less nature to start with)", foodSet.count( nature ) < randomSet.count( nature ));
		
	}
	

}
