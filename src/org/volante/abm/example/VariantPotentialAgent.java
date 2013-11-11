package org.volante.abm.example;

import org.simpleframework.xml.Element;
import org.volante.abm.agent.*;
import org.volante.abm.data.*;
import org.volante.abm.models.ProductionModel;

import com.moseph.modelutils.distribution.Distribution;
import com.moseph.modelutils.fastdata.*;

public class VariantPotentialAgent extends SimplePotentialAgent
{
	@Element(required=false)
	Distribution givingUpDistribution = null;
	@Element(required=false)
	Distribution givingInDistribution = null;
	@Element(required=false)
	Distribution ageDistribution = null;
	
	//These only work with the SimpleProductionModel
	@Element(required=false)
	Distribution serviceLevelNoise = null;
	@Element(required=false)
	Distribution capitalImportanceNoise = null;
	
	/**
	 * Override the standard agent creation to make agents with individual variation
	 */
	public Agent createAgent( Region region, Cell... cells )
	{
		DefaultAgent da = new DefaultAgent( this, id, data, region, productionModel( production, region ), givingUp(), givingIn() );
		if( ageDistribution != null ) da.setAge( (int)ageDistribution.sample() );
		region.setOwnership( da, cells );
		return da; 
	}
	
	public double givingUp() { return givingUpDistribution == null ? givingUp : givingUpDistribution.sample(); }
	public double givingIn() { return givingInDistribution == null ? givingIn : givingInDistribution.sample(); }
	
	/**
	 * Returns a noisy version of the production model. Uses the serviceLevelNoise distribution to
	 * create variance in the optimal levels of service production, and capitalImportanceNoise to
	 * create variance in the importance of the captials to this production.
	 * 
	 * Only works on SimpleProduction models at the moment.
	 * @param production
	 * @param r
	 * @return
	 */
	public ProductionModel productionModel( final ProductionModel production, final Region r )
	{
		if( ! ( production instanceof SimpleProductionModel ) ) return production;
		return ((SimpleProductionModel) production).copyWithNoise( data, serviceLevelNoise, capitalImportanceNoise );
	}
}
