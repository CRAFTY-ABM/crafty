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
package org.volante.abm.agent;


import org.volante.abm.data.Cell;
import org.volante.abm.data.ModelData;
import org.volante.abm.data.Region;
import org.volante.abm.data.Service;
import org.volante.abm.models.ProductionModel;
import org.volante.abm.schedule.RunInfo;
import org.volante.abm.serialization.Initialisable;

import com.moseph.modelutils.fastdata.UnmodifiableNumberMap;


/**
 * This is an interface for classes that create agents
 */
public interface PotentialAgent extends Initialisable {
	public UnmodifiableNumberMap<Service> getPotentialSupply(Cell cell);

	public Agent createAgent(Region region, Cell... cells);

	public String getID();

	public int getSerialID();

	public double getGivingUp();

	public double getGivingIn();

	public ProductionModel getProduction();

	public static final int				UNKNOWN_SERIAL		= -1;

	public static final PotentialAgent	NOT_MANAGED_TYPE	= new PotentialAgent()
															{
																@Override
																public void initialise(
																		ModelData data,
																		RunInfo info, Region extent)
																		throws Exception {
																}

																@Override
																public UnmodifiableNumberMap<Service> getPotentialSupply(
																		Cell cell)
															{
																return cell.getRegion()
																		.getModelData()
																		.serviceMap();
															}

																@Override
																public Agent createAgent(
																		Region region,
																		Cell... cells) {
																	return Agent.NOT_MANAGED;
																}

																@Override
																public String getID() {
																	return Agent.NOT_MANAGED_ID;
																}

																@Override
																public int getSerialID() {
																	return -1;
																}

																@Override
																public double getGivingUp() {
																	return 0;
																}

																@Override
																public double getGivingIn() {
																	return 0;
																}

																@Override
																public ProductionModel getProduction() {
																	return null;
																}

															};

}
