package org.volante.abm.optimization;

import static java.lang.Math.abs;

import java.util.Arrays;

import org.apache.commons.math3.exception.NumberIsTooSmallException;
import org.apache.commons.math3.genetics.*;
import org.apache.log4j.Logger;

public class ConvergenceChecker
{
	int generations = 10;
	double[] scores;
	private double convergence;
	int currentGeneration = 0;
	double currentScore = -Double.MAX_VALUE;
	int maxGens = Integer.MAX_VALUE;

	Logger log = Logger.getLogger( getClass() );

	public ConvergenceChecker( double convergence, int gens )
	{
		this.generations = gens;
		this.convergence = convergence;
		scores = new double[gens];
		Arrays.fill( scores, -Double.MAX_VALUE );
	}
	
	public ConvergenceChecker( double convergence, int gens, int maxGens )
	{
		this( convergence, gens );
		this.maxGens = maxGens;
	}

	public void addScore( double score )
	{
		scores[currentGeneration % generations] = score;
		currentGeneration++;
		currentScore = score;
	}

	public boolean isSatisfied()
	{
		if( currentGeneration < generations )
			return false;
		if( currentGeneration >= maxGens )
			return true;
		double min = Double.MAX_VALUE;
		for ( double d : scores )
			min = Math.min( d, min );
		double propChange = abs( ( currentScore - min ) / currentScore );
		if( propChange < convergence )
		{
			log.debug( String.format( "From %d generations, Score: %3f, Min: %3f, change: %3f", generations, currentScore, min, propChange ) );
			return true;
		}
		return false;
	}

}
