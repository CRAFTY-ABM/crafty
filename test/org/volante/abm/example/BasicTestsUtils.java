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


import static java.lang.Math.pow;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.volante.abm.agent.AbstractAgent;
import org.volante.abm.agent.Agent;
import org.volante.abm.agent.DefaultAgent;
import org.volante.abm.agent.PotentialAgent;
import org.volante.abm.data.Capital;
import org.volante.abm.data.Cell;
import org.volante.abm.data.ModelData;
import org.volante.abm.data.Region;
import org.volante.abm.data.RegionSet;
import org.volante.abm.data.Service;
import org.volante.abm.models.AllocationModel;
import org.volante.abm.models.CompetitivenessModel;
import org.volante.abm.models.DemandModel;
import org.volante.abm.models.WorldSynchronisationModel;
import org.volante.abm.param.RandomPa;
import org.volante.abm.schedule.RunInfo;
import org.volante.abm.schedule.WorldSyncSchedule;
import org.volante.abm.serialization.ABMPersister;
import org.volante.abm.testutils.NullWorldSynchronisationModel;

import cern.jet.random.engine.RandomEngine;

import com.google.common.base.Joiner;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.moseph.modelutils.fastdata.DoubleMap;
import com.moseph.modelutils.fastdata.IndexSet;
import com.moseph.modelutils.fastdata.Indexed;
import com.moseph.modelutils.fastdata.Named;
import com.moseph.modelutils.fastdata.NamedIndexSet;
import com.moseph.modelutils.fastdata.UnmodifiableNumberMap;


//  @formatter:off
/**
 *
 * Sets up a basic test environment and provides some features to compare data and syntactic sugar for tests.
 * 
 * Public members that make up the test environment:
 * 
 * - {@link ModelData} <code>modelData</code>
 * - {@link RunInfo} <code>runInfo</code>
 * 
 * - {@link SimplePotentialAgent} forestry
 * - {@link SimplePotentialAgent} farmering
 * - {@link Set<PotentialAgent>} potentialAgents
 * 
 * Models:
 * - {@link CompetitivenessModel} competition
 * - {@link AllocationModel allocation}
 * - {@link StaticPerCellDemandModel} demandR1
 * - {@link StaticPerCellDemandModel} demandR2
 * 
 * 2 region with models above, potential agents and 10 cells <code>c<Region><Number></code> per region:
 * - {@link Region} r1
 * - {@link Region} r2
 * - {@link Set<Region>} regions
 * - {@link RegionSet} w
 * 
 * 2 Agents:
 * - {@link AbstractAgent} a1
 * - {@link AbstractAgent} a2
 * 
 * - {@link ABMPersister} persister
 * - sets for each regions cells <code>r<region>cells</code>
 *  
 *  Static members:
 *  - {@link SimpleProductionModel} forestryProduction
 *  - {@link SimpleProductionModel} farmingProduction
 *  
 *  - {@link SimplePotentialAgent} forestry
 *  - {@link SimplePotentialAgent} farming
 *  - {@link Set<PotentialAgent>} potentialAgents
	
 * Considered  Capitals are:
 * HUMAN(0), INFRASTRUCTURE(1), ECONOMIC(2), NATURAL_GRASSLAND(3),
 * NATURAL_FOREST(4), NATURAL_CROPS(5), NATURE_VALUE(6)
 *  
 * Considered Services:
 * HOUSING(0), TIMBER(1), FOOD(2), RECREATION(3),
 * 
 * Further data as provided (all static):

 * @author Sascha Holzhauer
 *
 *<table>
 *	<th>Name</th><th>Content<th><th>Type</th>
 *	<tr>
 *		<td>extensiveFarmingProductionWeights</td>
 *		<td>Production weights for Extensive Farmers</td>
 *		<td>double []</td>
 *	</tr>
 *	<tr>
 *		<td>forestryProductionWeights</td>
 *		<td>Production weights for Foresters</td>
 *		<td>double []</td>
 *	</tr>
 *	<tr>
 *		<td>extensiveFarmingCapitalWeights</td>
 *		<td>Capital weights for Extensive Farmers</td>
 *		<td>double [][]</td>
 *	</tr>
 *	<tr>
 *		<td>forestryCapitalWeights</td>
 *		<td>Capital weights for Foresters</td>
 *		<td>double [][]</td>
 *	</tr>
 *	<tr>
 *		<td>cellCapitalsA</td>
 *		<td>Arbitrary DoubleMap A of cell capitals</td>
 *		<td>DoubleMap&lt;Capital&gt;</td>
 *	</tr>
 *	<tr>
 *		<td>cellCapitalsB</td>
 *		<td>Arbitrary DoubleMap B of cell capitals</td>
 *		<td>DoubleMap&lt;Capital&gt;</td>
 *	</tr>
 *	<tr>
 *		<td>extensiveFarmingOnCA</td>
 *		<td>Expected service provision from extensive farmers on a cell with cellCapitals A</td>
 *		<td>DoubleMap&lt;Service&gt;</td>
 *	</tr>
 *	<tr>
 *		<td>extensiveFarmingOnCB</td>
 *		<td>Expected service provision from extensive farmers on a cell with cellCapitals B</td>
 *		<td>DoubleMap&lt;Service&gt;</td>
 *	</tr>
 *	<tr>
 *		<td>forestryOnCA</td>
 *		<td>Expected service provision from foresters on a cell with cellCapitals A</td>
 *		<td>DoubleMap&lt;Service&gt;</td>
 *	</tr>
 *	<tr>
 *		<td>forestryOnCA</td>
 *		<td>Expected service provision from foresters farmers on a cell with cellCapitals B</td>
 *		<td>DoubleMap&lt;Service&gt;</td>
 *	</tr>
 *	<tr>
 *		<td>forestryGivingUp</td>
 *		<td>Giving up threshold for foresters (0.5)</td>
 *		<td>double</td>
 *	</tr>
 *	<tr>
 *		<td>forestryGivingIn</td>
 *		<td>Giving in threshold for foresters (0.5)</td>
 *		<td>double</td>
 *	</tr>
 *	<tr>
 *		<td>farmingGivingUp</td>
 *		<td>Giving up threshold for farming (0.5)</td>
 *		<td>double</td>
 *	</tr>
 *	<tr>
 *		<td>farmingGivingIn</td>
 *		<td>Giving in threshold for farming (0.5)</td>
 *		<td>double</td>
 *	</tr>
 *	<tr>
 *		<td>r1cells</td>
 *		<td>Sets of cells of region 1</td>
 *		<td>Set&lt;Cell&gt;</td>
 *	</tr>
 *	<tr>
 *		<td>r2cells</td>
 *		<td>Sets of cells of region 2</td>
 *		<td>Set&lt;Cell&gt;</td>
 *	</tr>
 * </table>
 */
// @formatter:on
public class BasicTestsUtils
{

	/**
	 * Logger
	 */
	static private Logger				logger								= Logger.getLogger(BasicTestsUtils.class);

	protected static int randomNumberCounter = 0;

	public static ModelData				modelData							= new ModelData();							// Setup
																														// with
																														// simple
																														// versions
																														// of
																														// all
																														// attributes
	public static RunInfo				runInfo								= new RunInfo();

	/**
	 * A whole bunch of cells to play with
	 */
	public Cell							c11, c12, c13, c14, c15, c16, c17, c18, c19;
	public Cell							c21, c22, c23, c24, c25, c26, c27, c28, c29;

	// And the sets which will go into each region
	public Set<Cell>					r1cells;
	public Set<Cell>					r2cells;

	/*
	 * Example settings with known values; cell baseCapitals, weights for low intensity agriculture
	 * and forestry and the service provision expected from each on each cell
	 */

	public static double[]				extensiveFarmingProductionWeights	= new double[] { 1, 0,
																			7, 4 };
	public static double[][]			extensiveFarmingCapitalWeights		= new double[][] {
			{ 1, 1, 0.5, 0, 0, 0, 0 }, // Housing
			{ 0, 0, 0, 0, 0, 0, 0 }, // Timber
			{ 0.5, 0.5, 0.5, 1, 0, 1, 0 }, // Food
			{ 0, 1, 0, 0, 0, 0, 1 }										// Recreation
																			};

	public static double[]				forestryProductionWeights			= new double[] { 0, 10,
																			0, 4 };
	public static double[][]			forestryCapitalWeights				= new double[][] {
			{ 0, 0, 0, 0, 0, 0, 0 }, // Housing
			{ 0.3, 0.5, 0.4, 0, 1, 0, 0 }, // Timber
			{ 0, 0, 0, 0, 0, 0, 0 }, // Food
			{ 0.3, 0.3, 0, 0, 0, 0, 1 }									// Recreation
																			};

	/**
	 * Arbitrary {@link DoubleMap} A of cell capitals
	 */
	public static DoubleMap<Capital>	cellCapitalsA						= capitals(0.5, 0.7,
																					0.7, 0.4, 0.3,
																					0.7, 0.1);

	/**
	 * Arbitrary {@link DoubleMap} B of cell capitals
	 */
	public static DoubleMap<Capital>	cellCapitalsB						= capitals(0.1, 0.3,
																					0.3, 0.2, 0.8,
																					0.7, 0.9);

	/**
	 * Expected service provision from extensive farmers on a cell with cellCapitals A.
	 * 
	 * Production of each service should be the production weight multiplied by each capital to the
	 * power of the relevant capitalWeight (using {@link SimpleProductionModel}).
	 */
	public static DoubleMap<Service>	extensiveFarmingOnCA				= services(
																					1
																							* 0.5
																							* 0.7
																							* pow(0.7,
																									0.5)
																							* 1 * 1
																							* 1,
																					0 * 1 * 1 * 1
																							* 1 * 1
																							* 1 * 1,
																					7
																							* pow(0.5,
																									0.5)
																							* pow(0.7,
																									0.5)
																							* pow(0.7,
																									0.5)
																							* 0.4
																							* 1
																							* 0.7
																							* 1,
																					4 * 1 * 0.7 * 1
																							* 1 * 1
																							* 1
																							* 0.1);

	/**
	 * Expected service provision from extensive farmers on a cell with cellCapitals B.
	 * 
	 * Production of each service should be the production weight multiplied by each capital to the
	 * power of the relevant capitalWeight (using {@link SimpleProductionModel}).
	 */
	public static DoubleMap<Service>	extensiveFarmingOnCB				= services(
																					0.1 * 0.3 * pow(
																							0.3,
																							0.5),
																					0,
																					7
																							* pow(0.1,
																									0.5)
																							* pow(0.3,
																									0.5)
																							* pow(0.3,
																									0.5)
																							* 0.2
																							* 0.7,
																					4 * 0.3 * 0.9);

	/**
	 * Expected service provision from foresters on a cell with cellCapitals A.
	 * 
	 * Production of each service should be the production weight multiplied by each capital to the
	 * power of the relevant capitalWeight (using {@link SimpleProductionModel}).
	 */
	public static DoubleMap<Service>	forestryOnCA						= services(
																					0 * 1 * 1 * 1
																							* 1 * 1
																							* 1,
																					10
																							* pow(0.5,
																									0.3)
																							* pow(0.7,
																									0.5)
																							* pow(0.7,
																									0.4)
																							* 1
																							* 0.3
																							* 1 * 1,
																					0 * 1 * 1 * 1
																							* 1 * 1
																							* 1,
																					4
																							* pow(0.5,
																									0.3)
																							* pow(0.7,
																									0.3)
																							* 1 * 1
																							* 1 * 1
																							* 0.1);

	/**
	 * Expected service provision from foresters on a cell with cellCapitals B.
	 * 
	 * Production of each service should be the production weight multiplied by each capital to the
	 * power of the relevant capitalWeight (using {@link SimpleProductionModel}).
	 */
	public static DoubleMap<Service>	forestryOnCB						= services(
																					0 * 1 * 1 * 1
																							* 1 * 1
																							* 1,
																					10
																							* pow(0.1,
																									0.3)
																							* pow(0.3,
																									0.5)
																							* pow(0.3,
																									0.4)
																							* 1
																							* 0.8
																							* 1 * 1,
																					0 * 1 * 1 * 1
																							* 1 * 1
																							* 1,
																					4
																							* pow(0.1,
																									0.3)
																							* pow(0.3,
																									0.3)
																							* 1 * 1
																							* 1 * 1
																							* 0.9);

	/**
	 * Default competition and allocation functions
	 */
	public CompetitivenessModel			competition							= new SimpleCompetitivenessModel();
	public AllocationModel				allocation							= new SimpleAllocationModel();
	public StaticPerCellDemandModel		demandR1							= new StaticPerCellDemandModel();
	public StaticPerCellDemandModel		demandR2							= new StaticPerCellDemandModel();

	public static SimpleProductionModel	forestryProduction					= new SimpleProductionModel(
																					forestryCapitalWeights,
																					forestryProductionWeights);
	public static SimpleProductionModel	farmingProduction					= new SimpleProductionModel(
																					extensiveFarmingCapitalWeights,
																					extensiveFarmingProductionWeights);
	public static final double			forestryGivingUp					= 0.5;
	public static final double			forestryGivingIn					= 0.5;
	public static final double			farmingGivingUp						= 0.5;
	public static final double			farmingGivingIn						= 0.5;
	public static SimplePotentialAgent forestry = new VariantPotentialAgent(
																					"Forestry",
																					modelData,
																					forestryProduction,
																					forestryGivingUp,
																					forestryGivingIn);
	public static SimplePotentialAgent farming = new VariantPotentialAgent(
																					"Farming",
																					modelData,
																					farmingProduction,
																					farmingGivingUp,
																					farmingGivingIn);
	public static Set<PotentialAgent>	potentialAgents						= new HashSet<PotentialAgent>(
																					Arrays.asList(new PotentialAgent[] {
																					forestry,
			farming																}));

	public Region						r1;
	public Region						r2;
	public Set<Region>					regions								= new HashSet<Region>(
																					Arrays.asList(new Region[] {
																					r1, r2 }));

	public RegionSet					w;
	public AbstractAgent				a1;
	public AbstractAgent				a2;

	public ABMPersister					persister							= ABMPersister
																					.getInstance();
	Logger								log									= Logger.getLogger(getClass());

	/**
	 * Initialises region set <code>w</code>
	 */
	public BasicTestsUtils() {
		initTestEnvironment();
	}

	/**
	 * Setup persister by default to look in test-data. Init cells.
	 */
	@Before
	public void setupPersister()
	{
		runInfo = new RunInfo();
		log.info("Reset RunInfo");

		persister.setBaseDir("test-data");
		initTestEnvironment();
	}

	/**
	 * Inits cells, regions, agents, cell sets, regions set, regionset.
	 */
	public void initTestEnvironment()
	{
		log.info("Reinitialising Cells");
		c11 = new Cell(1, 1);
		c12 = new Cell(1, 2);
		c13 = new Cell(1, 3);
		c14 = new Cell(1, 4);
		c15 = new Cell(1, 5);
		c16 = new Cell(1, 6);
		c17 = new Cell(1, 7);
		c18 = new Cell(1, 8);
		c19 = new Cell(1, 9);

		c21 = new Cell(2, 1);
		c22 = new Cell(2, 2);
		c23 = new Cell(2, 3);
		c24 = new Cell(2, 4);
		c25 = new Cell(2, 5);
		c26 = new Cell(2, 6);
		c27 = new Cell(2, 7);
		c28 = new Cell(2, 8);
		c29 = new Cell(2, 9);

		// And the sets which will go into each region
		r1cells = new HashSet<Cell>(Arrays.asList(new Cell[] { c11, c12, c13, c14, c15, c16, c17,
				c18, c19 }));
		r2cells = new HashSet<Cell>(Arrays.asList(new Cell[] { c21, c22, c23, c24, c25, c26, c27,
				c28, c29 }));

		r1 = new Region(allocation, competition, demandR1, potentialAgents, c11, c12, c13, c14,
				c15, c16, c17, c18, c19);
		r1.setID("Region01");

		r2 = new Region(allocation, competition, demandR2, potentialAgents, c21, c22, c23, c24,
				c25, c26, c27, c28, c29);
		r2.setID("Region02");

		regions = new HashSet<Region>(Arrays.asList(new Region[] { r1, r2 }));

		w = new RegionSet(r1, r2);
		try {
			w.initialise(modelData, runInfo, null);
			runInfo.getSchedule().setRegions(w);
			runInfo.getSchedule().initialise(modelData, runInfo, null);
		} catch (Exception e) {
			e.printStackTrace();
		}

		a1 = new DefaultAgent("A1", modelData);
		a2 = new DefaultAgent("A2", modelData);

		WorldSynchronisationModel worldSyncModel = new NullWorldSynchronisationModel();
		((WorldSyncSchedule) runInfo.getSchedule())
				.setWorldSyncModel(worldSyncModel);

	}

	/**
	 * Checks that an expected set is the same as the actual set Note: arguments are the opposite
	 * way round to normal, to allow user of varargs
	 * 
	 * @param msg
	 *        describe the origin and kind of compared data
	 * @param got
	 *        actual data to compare
	 * @param ex
	 *        expected data to compare with
	 */
	public static <T> void checkSet(String msg, Collection<T> got, T... ex)
	{
		HashSet<T> exp = new HashSet<T>(Arrays.asList(ex));
		assertEquals(msg + "\ngot: " + got + "\nexp:" + exp + "\n", exp, new HashSet<T>(got));
	}

	/**
	 * Checks that an expected set is the same as the actual set Note: arguments are the opposite
	 * way round to normal, to allow user of varargs
	 * 
	 * @param got
	 *        actual data to compare
	 * @param ex
	 *        expected data to compare with
	 */
	public static <T> void checkSet(Collection<T> got, T... ex) {
		checkSet("", got, ex);
	}

	/**
	 * Creates a new double map with the given index set and values. Not very useful, just syntatic
	 * sugar for commonly used tests
	 * 
	 * @param ind
	 *        index set
	 * @param vals
	 *        values
	 * @return double map with given index and values
	 */
	public static <T extends Indexed> DoubleMap<T> dm(IndexSet<T> ind, double... vals)
	{
		return new DoubleMap<T>(ind, vals);
	}

	/**
	 * Create a map using the {@link SimpleServices} and the given values
	 * 
	 * @param vals
	 * @return
	 */
	public static DoubleMap<Service> services(double... vals) {
		return dm(SimpleService.simpleServices, vals);
	}

	/**
	 * Creates a map using the {@link SimpleCapitals} and the given values.
	 * 
	 * @param vals
	 * @return
	 */
	public static DoubleMap<Capital> capitals(double... vals) {
		return dm(SimpleCapital.simpleCapitals, vals);
	}

	/**
	 * Compare maps of data
	 * 
	 * @param exp
	 *        expected data
	 * @param got
	 *        acutal data
	 */
	public static <T extends Indexed> void assertEqualMaps(UnmodifiableNumberMap<T> exp,
			UnmodifiableNumberMap<T> got)
	{
		assertEqualMaps("", exp, got);
	}

	/**
	 * Checks that two NumberMaps are the same
	 * 
	 * @param msg
	 * @param exp
	 * @param got
	 */
	public static <T extends Indexed> void assertEqualMaps(String msg,
			UnmodifiableNumberMap<T> exp, UnmodifiableNumberMap<T> got)
	{
		assertTrue(msg + "\ngot: " + got.prettyPrint() + ",\nexp: " + exp.prettyPrint(),
				exp.same(got));
	}

	/**
	 * Checks that the given cells are indeed unmanaged.
	 * 
	 * @param cells
	 */
	public static void assertUnmanaged(Cell... cells)
	{
		for (Cell c : cells) {
			assertEquals(c + " not unmanaged", Agent.NOT_MANAGED, c.getOwner());
		}
	}

	/**
	 * Asserts that the given cells are occupied by an agent with the given ID and given
	 * competitiveness.
	 * 
	 * @param id
	 *        agent ID to check
	 * @param competitiveness
	 *        competitiveness to check
	 * @param cells
	 *        cells to check
	 */
	public static void assertAgent(String id, double competitiveness, Cell... cells)
	{
		for (Cell c : cells)
		{
			Agent a = c.getOwner();
			assertEquals("ID of agent on " + c.toString(), id, a.getID());
			assertEquals("Competitiveness of agent on " + c.toString(), competitiveness,
					a.getCompetitiveness(), 0.0001);
		}

	}

	/**
	 * Checks whether the capital with the given name has the given index in the given capitals
	 * named index set.
	 * 
	 * @param caps
	 * @param name
	 * @param index
	 */
	public static <T extends Named & Indexed> void checkDataType(NamedIndexSet<T> caps,
			String name, int index)
	{
		assertNotNull(caps.forName(name));
		assertEquals("Checking index for " + name, index, caps.forName(name).getIndex());
	}

	/**
	 * Checks whether the <code>i</code>th cell is occupied by an agent with the <code>i</code>th
	 * ID.
	 * 
	 * @param cells
	 * @param owners
	 */
	public static void checkOwnership(Cell[] cells, String... owners)
	{
		for (int i = 0; i < cells.length; i++) {
			assertEquals("Ownership: ", owners[i], cells[i].getOwnerID());
		}
	}

	/**
	 * Checks whether the <code>i</code>th cell is occupied by an agent of the <code>i</code>th
	 * potential agent.
	 * 
	 * @param cells
	 * @param owners
	 */
	public static void checkOwnership(Cell[] cells, PotentialAgent... owners)
	{
		for (int i = 0; i < cells.length; i++) {
			assertEquals("Ownership: ", owners[i].getID(), cells[i].getOwnerID());
		}
	}

	/**
	 * Set the <code>i</code>th given capitals for tje <code>i</code>th cell.
	 * 
	 * @param cells
	 * @param capitals
	 */
	public static void setCapitals(Cell[] cells, double[]... capitals)
	{
		for (int i = 0; i < cells.length; i++) {
			cells[i].getModifiableBaseCapitals().put(capitals[i]);
		}
	}

	/**
	 * Log given objects in a line
	 * 
	 * @param vals
	 */
	public static void print(Object... vals)
	{
		logger.info(Joiner.on(" ").useForNull("<NULL>").join(vals));
	}

	/**
	 * Set up a basic world with the given cells.
	 * 
	 * @param cells
	 * @return
	 * @throws Exception
	 */
	public Region setupBasicWorld(Cell... cells) throws Exception
	{
		return setupBasicWorld(true, cells);
	}

	/**
	 * Return a {@link PotentialAgent} with the given ID and given giving in/put thresholds that
	 * produces only the given {@link Service} leveraging capitals according to given dependencies.
	 * 
	 * @param id
	 * @param givingUp
	 * @param givingIn
	 * @param amount
	 * @param service
	 * @param dependencies
	 * @return
	 */
	public static PotentialAgent getSingleProductionAgent(String id, double givingUp,
			double givingIn, double amount, Service service, Capital... dependencies)
	{
		SimpleProductionModel model = new SimpleProductionModel();
		for (Service s : SimpleService.simpleServices)
		{
			model.productionWeights.put(s, 0);
			for (Capital c : SimpleCapital.simpleCapitals) {
				model.capitalWeights.put(c, s, 0);
			}
		}
		model.productionWeights.put(service, amount);
		for (Capital d : dependencies) {
			model.capitalWeights.put(d, service, 1);
		}
		return new SimplePotentialAgent(id, modelData, model, givingUp, givingIn);
	}

	/**
	 * Returns a Multiset which provides the opportunity to receive counts agents of each
	 * {@link PotentialAgent} in the given region.
	 * 
	 * @param r
	 * @return
	 */
	public Multiset<PotentialAgent> countAgents(Region r)
	{
		Multiset<PotentialAgent> set = HashMultiset.create();
		for (Cell c : r.getCells()) {
			set.add(c.getOwner().getType());
		}
		return set;
	}

	/**
	 * Sets up a very basic world containing one region with the given cells. As models the
	 * {@link SimpleAllocationModel}, {@link SimpleCompetitivenessModel}, and
	 * {@link RegionalDemandModel} are applied. Schedule is initialised.
	 * 
	 * @param initialiseRegion
	 * @param cells
	 * @return
	 * @throws Exception
	 */
	public Region setupBasicWorld(boolean initialiseRegion, Cell... cells) throws Exception
	{
		Region r = new Region(cells);
		r.cellsCreated();
		r.setAllocationModel(new SimpleAllocationModel());
		r.setCompetitivenessModel(new SimpleCompetitivenessModel());
		r.setDemandModel(new RegionalDemandModel());
		if (initialiseRegion) {
			r.initialise(modelData, runInfo, null);
		}
		runInfo.getSchedule().setRegions(new RegionSet(r));
		runInfo.getSchedule().initialise(modelData, runInfo, r);

		return r;
	}

	/**
	 * 
	 * Creates a very basic world applying the given models.
	 * 
	 * @param allocation
	 * @param competition
	 * @param demand
	 * @param potentialAgents
	 * @param cells
	 * @return
	 * @throws Exception
	 */
	public Region setupWorld(AllocationModel allocation, CompetitivenessModel competition,
			DemandModel demand, Set<PotentialAgent> potentialAgents, Cell... cells)
			throws Exception
	{
		Region r = new Region(allocation, competition, demand, potentialAgents, cells);
		r.initialise(modelData, runInfo, null);
		runInfo.getSchedule().setRegions(new RegionSet(r));
		runInfo.getSchedule().initialise(modelData, runInfo, r);
		return r;
	}

	/**
	 * 
	 */
	protected void setupPseudoRandomEngine() {
		// set determined random number generator:
		r1.getRandom()
				.getURService()
				.registerGenerator(RandomPa.RANDOM_SEED_RUN.name(),
						new RandomEngine() {
							private static final long serialVersionUID = 5271301810955320643L;

							@Override
							public double nextDouble() {
								if (randomNumberCounter++ % 2 == 0) {
									return 0.25;
								} else {
									return 0.75;
								}
							}

							@Override
							public int nextInt() {
								return 0;
							}
						});
	}
}
