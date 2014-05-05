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
 */
package org.volante.abm.optimization;

import static java.lang.Math.abs;

import java.util.Arrays;

import org.apache.log4j.Logger;

public class ConvergenceChecker
{
	int generations = 10;
	double[]		scores				= null;
	private double	convergence			= 0.0;							;
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
		if( currentGeneration < generations ) {
			return false;
		}
		if( currentGeneration >= maxGens ) {
			return true;
		}
		double min = Double.MAX_VALUE;
		for ( double d : scores ) {
			min = Math.min( d, min );
		}
		double propChange = abs( ( currentScore - min ) / currentScore );
		if( propChange < convergence )
		{
			log.debug( String.format( "From %d generations, Score: %3f, Min: %3f, change: %3f", generations, currentScore, min, propChange ) );
			return true;
		}
		return false;
	}
}
