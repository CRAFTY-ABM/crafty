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

import static com.moseph.modelutils.Utilities.sample;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.exception.NumberIsTooSmallException;
import org.apache.commons.math3.exception.util.DummyLocalizable;
import org.apache.commons.math3.genetics.AbstractListChromosome;
import org.apache.commons.math3.genetics.Chromosome;
import org.apache.commons.math3.genetics.ElitisticListPopulation;
import org.apache.commons.math3.genetics.FixedGenerationCount;
import org.apache.commons.math3.genetics.GeneticAlgorithm;
import org.apache.commons.math3.genetics.InvalidRepresentationException;
import org.apache.commons.math3.genetics.MutationPolicy;
import org.apache.commons.math3.genetics.OnePointCrossover;
import org.apache.commons.math3.genetics.Population;
import org.apache.commons.math3.genetics.StoppingCondition;
import org.apache.commons.math3.genetics.TournamentSelection;
import org.simpleframework.xml.Attribute;
import org.volante.abm.agent.PotentialAgent;
import org.volante.abm.data.Region;
import org.volante.abm.optimization.GeneticAlgorithmAllocationModel.LandUseChromosome;
import org.volante.abm.param.RandomPa;

/**
 * Uses a GA to optimise land use allocation
 * 
 * It's probably pretty slow - haven't done much to optimize it, more a proof of 
 * concept - but it should work. It starts optimising from the current land use,
 * so with some tweaking of the number of generations and the mutation rate you 
 * can choose either to gradually improve the current solution, or start from an
 * entirely random sample.
 * 
 * The fitness function is:
 * * calculate potential supply for a set of agents
 * * calculate the Regional residual demand at this level of supply
 * * convert this to a per-cell value (i.e. divide by ncells)
 * * calculate Utility for this according to the competitiveness function. This gives a negative utility, we try to increase as much as possible.
 * @author dmrust
 *
 */
public class GeneticAlgorithmAllocationModel extends OptimizationAllocationModel<LandUseChromosome>
{
	/**
	 * Maxiumum number of generations to run the algorithm for
	 */
	@Attribute(required=false)
	int numGenerations = 1000;
	/**
	 * Number of chromosomes in each generation
	 */
	@Attribute(required=false)
	int numChromosomes = 50;
	/**
	 * How often do chromosomes mutate
	 */
	@Attribute(required=false)
	double chromosomeMutationRate = 0.4;
	/**
	 * When a chromosome mutates, what proportion of land use cells are changed
	 */
	@Attribute(required=false)
	double cellMutationRate = 0.4;
	/**
	 * When making the initial population, what proportion of cells in each solution are mutated
	 */
	@Attribute(required=false)
	double initialMutationRate = 0.4;
	
	/**
	 * What proportion of chromosomes are selected based on elitism
	 * (see http://commons.apache.org/math/apidocs/org/apache/commons/math3/genetics/ElitisticListPopulation.html)
	 */
	@Attribute(required=false)
	double elitismRate = 0.5;
	/**
	 * If the score within the last convergenceGenerations generations isn't better by this amount
	 * assume that we've converged
	 */
	@Attribute(required=false)
	double convergenceProportion = 0.005;
	/**
	 * Check scores for this many generations to see if solution has converged
	 */
	@Attribute(required=false)
	int convergenceGenerations = 30;
	
	GeneticAlgorithm ga;

	@Override
	public void setupRun()
	{
		// TODO Auto-generated method stub
		ga = new GeneticAlgorithm( new OnePointCrossover<PotentialAgent>(),
				1, new AgentMutation(), chromosomeMutationRate, new TournamentSelection( 2 ) )
		{
			@Override
			public Population nextGeneration( Population current )
			{
				Population gen = super.nextGeneration( current );
				double score = gen.getFittestChromosome().fitness();
				updateScore( score );
				return gen;
			}
		};

	}
	
	@Override
	LandUseChromosome doRun()
	{
		log.info("Starting GA optimisation");
		Population initial = getInitialPopulation( region );
		StoppingCondition stopCond = new ConvergenceMaxStoppingCondition( numGenerations, convergenceProportion, convergenceGenerations );
		Population finalPopulation = ga.evolve(initial, stopCond);
		LandUseChromosome bestFinal = (LandUseChromosome) finalPopulation.getFittestChromosome();
		log.info("Best: " + bestFinal.getData());
		return bestFinal;
	}
	
	@Override
	void applySolution(LandUseChromosome solution)
	{
		applyList( solution.getData() );
	}
	
	public Population getInitialPopulation( Region r )
	{
		List<Chromosome> chromosomes = new ArrayList<Chromosome>(numChromosomes);
		List<PotentialAgent> currentLandUse = currentLandUseList();
		for( int i = 0; i < numChromosomes; i++ ) {
			chromosomes.add( new LandUseChromosome(mutatedLandUseList( currentLandUse, initialMutationRate) ));
		}
		return new ElitisticListPopulation( chromosomes, numChromosomes, elitismRate );
	}
	
	public List<PotentialAgent> mutatedLandUseList( List<PotentialAgent> initial, double rate )
	{
		List<PotentialAgent> agents = new ArrayList<PotentialAgent>( initial.size() );
		Collection<PotentialAgent> available = region.getPotentialAgents();

		// RANU make more efficient
		for( PotentialAgent p : initial ) {
			if( region.getRandom().getURService().getGenerator(RandomPa.RANDOM_SEED_RUN_ALLOCATION.name()).nextDouble() < rate ) {
				agents.add( sample( available, region.getRandom().getURService(), RandomPa.RANDOM_SEED_RUN_ALLOCATION.name() ) );
			} else {
				agents.add( p );
			}
		}
		return agents;
	}
	
	public List<PotentialAgent> randomLandUseList()
	{
		return mutatedLandUseList( currentLandUseList(), 1 );
	}
	



	
	@Override
	public String getOptimisationType() { return "Genetical Algorithms"; }
	
	class LandUseChromosome extends AbstractListChromosome<PotentialAgent>
	{
		double fitness = -Double.MAX_VALUE;
		
		public LandUseChromosome( List<PotentialAgent> representation ) throws InvalidRepresentationException
		{
			super( representation );
		}

		@Override
		@SuppressWarnings("deprecation")
		public double fitness()
		{
			//Immutable, so can cache fitness
			if( fitness != -Double.MAX_VALUE )
			 {
				return fitness;
				//log.info( "Agents: " + getRepresentation() );
			}
			
			fitness = calculateFitness( getRepresentation() );
			return fitness;
		}
		
		@Override
		protected void checkValidity( List<PotentialAgent> chromosomeRepresentation ) throws InvalidRepresentationException { }
		@Override
		public AbstractListChromosome<PotentialAgent> newFixedLengthChromosome( List<PotentialAgent> chromosomeRepresentation )
		{
			return new LandUseChromosome( chromosomeRepresentation );
		}
		
		//Should be immutable list...
		List<PotentialAgent> getData() { return getRepresentation(); }
		
	}
	
	public class AgentMutation implements MutationPolicy
	{
		@Override
		public Chromosome mutate( Chromosome original ) throws MathIllegalArgumentException
		{
			if( !( original instanceof LandUseChromosome ) ) {
				throw new MathIllegalArgumentException( new DummyLocalizable("Incompatible Chromosome"+original.getClass()) );
			}
			LandUseChromosome chrom = (LandUseChromosome) original;
			List<PotentialAgent> agents = new ArrayList<PotentialAgent>( chrom.getLength() );

			// RANU make more efficient
			for( PotentialAgent p : chrom.getData() ) {
				if (region.getRandom().getURService()
						.getGenerator(RandomPa.RANDOM_SEED_RUN_ALLOCATION.name()).nextDouble() < cellMutationRate) {
					agents.add(sample(region.getPotentialAgents(), region.getRandom()
							.getURService(), RandomPa.RANDOM_SEED_RUN_ALLOCATION.name()));
				} else {
					agents.add( p );
				}
			}
			return new LandUseChromosome( agents);
		}
		
	}
	
	public class ConvergenceMaxStoppingCondition extends FixedGenerationCount
	{
		ConvergenceChecker convergence;

		public ConvergenceMaxStoppingCondition( int maxGenerations, double convergence, int gens ) throws NumberIsTooSmallException
		{
			super( maxGenerations );
			this.convergence = new ConvergenceChecker( convergence, gens );
		}

		@Override
		public boolean isSatisfied( Population population )
		{
			if( super.isSatisfied( population ) )
			{
				setState("Max Generations");
				return true ;
			}
			double score = population.getFittestChromosome().fitness();
			convergence.addScore( score );
			
			if( convergence.isSatisfied() )
			{
				setState( "Converged" );
				return true;
			}
			return false;
		}
		
	}

}
