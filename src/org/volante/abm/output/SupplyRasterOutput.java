package org.volante.abm.output;

import org.simpleframework.xml.Attribute;
import org.volante.abm.data.*;

public class SupplyRasterOutput extends RasterOutputter
{
	@Attribute(name="service")
	String serviceName = "HUMAN";
	Service service;
	
	public SupplyRasterOutput() {}
	public SupplyRasterOutput( String serviceName ) { this.serviceName = serviceName; }
	public SupplyRasterOutput( Service service ) { this.service = service; }
	public double apply( Cell c )
	{
		return c.getSupply().getDouble( service );
	}

	public String getDefaultOutputName()
	{
		return "Service-"+service.getName();
	}

	@Override
	public void initialise() throws Exception
	{
		super.initialise();
		service = modelData.services.forName( serviceName );
	}

	public boolean isInt() { return false; }
}
