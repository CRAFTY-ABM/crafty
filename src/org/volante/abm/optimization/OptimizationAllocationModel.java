package org.volante.abm.optimization;

import java.util.*;

import org.apache.log4j.*;
import org.apache.log4j.lf5.LogLevel;
import org.volante.abm.agent.*;
import org.volante.abm.data.*;
import org.volante.abm.example.SimplePotentialAgent;
import org.volante.abm.models.AllocationModel;
import org.volante.abm.models.AllocationModel.AllocationDisplay;
import org.volante.abm.optimization.GeneticAlgorithmAllocationModel.LandUseChromosome;
import org.volante.abm.schedule.RunInfo;

import com.moseph.modelutils.fastdata.DoubleMap;

/**
 * Base class for AllocationModels which do some kind of iterative optimization
 * to allocate land
 * @author dmrust
 *
 */
public abstract class OptimizationAllocationModel<T> implements AllocationModel
{
	Region region = null;
	ModelData data = null;
	RunInfo info = null;
	
	Logger log = Logger.getLogger( getClass() );
	List<OptimizationListener> listeners = new ArrayList<OptimizationListener>();
	int runNumber = 0;
	final List<Double> scores = new ArrayList<Double>();

	public void initialise( ModelData data, RunInfo info, Region extent ) throws Exception
	{
		this.region = extent;
		this.data = data;
		this.info = info;
	}

	public void allocateLand( Region r )
	{
		this.region = r;
		scores.clear();
		setState("Initialising");
		setupRun();
		runNumber = 0;
		sendStartRuns();
		setState("Starting Run");
		T solution = doRun();
		//setState("Applying Solution");
		applySolution(solution);
	}
	
	abstract void setupRun();
	abstract T doRun();
	abstract void applySolution( T solution );
	public abstract String getOptimisationType();
	
	void applyList(List<PotentialAgent> solution)
	{
		for( Cell c : region.getAllCells() )
		{
			PotentialAgent p  = getAgentForCell( c, solution );
			if( p != null ) region.setOwnership( p.createAgent(region), c );
		}
	}
	
	public double getCurrentFitness() { return calculateFitness( currentLandUseList() ); }
		
	public double calculateFitness( List<PotentialAgent> agents )
	{
		DoubleMap<Service> demand = region.getDemandModel().getDemand();
		DoubleMap<Service> residual = data.serviceMap();
		DoubleMap<Service> totalSupply = getTotalSupply( agents );
		totalSupply.subtractInto( demand, residual );
		//Per cell residual demand
		DoubleMap<Service> perCellResidual = data.serviceMap();
		residual.multiplyInto( 1.0/region.getCells().size(), perCellResidual );
		
		//The Competition model's assessment of the residual per cell demand is a valuation of the
		//Services not provided
		//This doesn't take Institutions into account
		double fitness = region.getCompetitionModel().getCompetitveness( region.getDemandModel(), perCellResidual );
		return fitness;
		}
	
	public DoubleMap<Service> getTotalSupply( List<PotentialAgent> agents )
	{
		DoubleMap<Service> supply = data.serviceMap();
		List<Cell> ca = new ArrayList<Cell>();
		for( Cell c : region.getAllCells() ) ca.add( c );
		for( Cell c : ca )
		{
			SimplePotentialAgent a = (SimplePotentialAgent) getAgentForCell( c, agents );
			if( a != null )
			{
				a.getPotentialSupply( c ).addInto( supply );
			}
		}
		return supply;
	}
	

	
	PotentialAgent getAgentForCell( Cell c, List<PotentialAgent> agents )
	{
		int index = cellToIndex(c);
		return agents.get( index );
	}
	
	int cellToIndex( Cell c )
	{
		Extent e = region.getExtent();
		//log.debug(e + ": " + region.getCells());
		//log.debug( String.format( "mx: %3d, my: %3d, cx: %3d, cy: %3d", e.getMinX(), e.getMinY(), c.getX(), c.getY() ) );
		int regWidth = e.getWidth();
		int cxInd = c.getX() - e.getMinX();
		int cyInd = c.getY() - e.getMinY();
		int index =  cxInd + (cyInd)*regWidth;
		//log.debug( String.format("cx: %3d, cy: %3d, rw: %3d, ind:%3d", cxInd, cyInd, regWidth, index) );
		return index;
	}
	
	public List<PotentialAgent> currentLandUseList()
	{
		Set<String> agentNames = new HashSet<String>();
		for( PotentialAgent p : region.getPotentialAgents() ) agentNames.add(p.getID());
		int length = region.getExtent().getHeight() * region.getExtent().getWidth();
		List<PotentialAgent> l = new ArrayList<PotentialAgent>( length );
		for( int i = 0; i < length; i++ ) l.add(i,null);
		for( Cell c : region.getAllCells())
		{
			int index = cellToIndex(c);
			PotentialAgent current = c.getOwner().getType();
			if( current == null ) System.err.println("GA Got null agent in cell " + c );
			if( agentNames.contains(current.getID()))
				l.set( index, current );
			else
				if( current.getID() != Agent.NOT_MANAGED_ID )
					log.error("Unknown agent: " + current.getID() );
		}
		return l;
	}
	
	
	
	void updateScore( double score )
	{
		setState("Running");
		log.info("Run: " + runNumber + " Score: " + score);
		for( OptimizationListener l : listeners ) l.updateBest(  runNumber,  score );
		runNumber++;
	}
	
	void setState( String state )
	{
		for( OptimizationListener l : listeners ) l.setState( state );
	}
	
	void sendStartRuns()
	{
		for( OptimizationListener l : listeners ) l.startRuns();
	}
	
	@SuppressWarnings("deprecation")
	public void addLogListener() { addListener( new LogOptimizationListener(Priority.DEBUG) ); }

	
	public AllocationDisplay getDisplay()
	{
		return new OptimisationAllocationDisplay( this );
	}
	
	public void addListener( OptimizationListener listener ) { listeners.add( listener ); }
	
	public static interface OptimizationListener
	{
		public void setState( String state );
		public void updateBest( int run, double score );
		public void startRuns();
	}
	
	public class LogOptimizationListener implements OptimizationListener
	{
		Priority level;
		public LogOptimizationListener( Priority level ) { this.level = level; }
		public void setState( String state ) { log.log( level, "State: " + state); }
		public void updateBest( int run, double score ) { log.log(level, String.format("Run %4d. Best score: %6f",run,score)); }
		public void startRuns() { log.log( level, "Starting..." ); }
	}
}
