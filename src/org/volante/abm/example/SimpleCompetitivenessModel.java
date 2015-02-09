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

import org.simpleframework.xml.Attribute;
import org.volante.abm.data.Cell;
import org.volante.abm.data.ModelData;
import org.volante.abm.data.Region;
import org.volante.abm.data.Service;
import org.volante.abm.models.CompetitivenessModel;
import org.volante.abm.models.DemandModel;
import org.volante.abm.schedule.RunInfo;
import org.volante.abm.visualisation.SimpleCompetitivenessDisplay;

import com.moseph.modelutils.fastdata.DoubleMap;
import com.moseph.modelutils.fastdata.UnmodifiableNumberMap;

/**
 * A simple model of competitiveness
 * @author dmrust
 *
 */
public class SimpleCompetitivenessModel implements CompetitivenessModel
{
	/**
	 * If set to true, then the current supply will be added back to the residual demand, so 
	 * competitiveness is calculated as if the cell is currently empty
	 */
	@Attribute(required=false)
	boolean removeCurrentLevel = false;
	
	/**
	 * If set to true, all negative demand (i.e. oversupply) is removed from the dot product
	 */
	@Attribute(required=false)
	boolean removeNegative = false;
	
	Region	region				= null;

	public void initialise(ModelData data, RunInfo info, Region extent) throws Exception {
		this.region = extent;
	}
	
	@Override
	public double getCompetitiveness(DemandModel demand, UnmodifiableNumberMap<Service> supply)
	{
		DoubleMap<Service> residual = demand.getResidualDemand().copy();
		residual.multiplyInto(1.0 / region.getNumCells(), residual);

		return addUpMarginalUtilities(residual, supply);
	}
	
	@Override
	public double getCompetitiveness( DemandModel demand, UnmodifiableNumberMap<Service> supply, Cell cell )
	{
		DoubleMap<Service> residual = demand.getResidualDemand( cell ).copy();
		if( removeCurrentLevel ) {
			cell.getSupply().addInto( residual );
		}
		return addUpMarginalUtilities( residual, supply );
	}
	
	@Override
	public double addUpMarginalUtilities( UnmodifiableNumberMap<Service> residual, UnmodifiableNumberMap<Service> supply )
	{
		if( ! removeNegative ) {
			return supply.dotProduct( residual );
		}
		DoubleMap<Service> res = (DoubleMap<Service>)residual;
		res.setMin( 0 );
		return supply.dotProduct( res );
	}


	public boolean isRemoveCurrentLevel() { return removeCurrentLevel; }
	public void setRemoveCurrentLevel( boolean removeCurrentLevel ) { this.removeCurrentLevel = removeCurrentLevel; }
	public boolean isRemoveNegative() { return removeNegative; }
	public void setRemoveNegative( boolean removeNegative ) { this.removeNegative = removeNegative; }

	@Override
	public CompetitivenessDisplay getDisplay() { return new SimpleCompetitivenessDisplay( this ); }
}
