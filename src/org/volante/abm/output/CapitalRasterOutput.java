package org.volante.abm.output;

import org.simpleframework.xml.Attribute;
import org.volante.abm.data.*;

public class CapitalRasterOutput extends RasterOutputter
{
	@Attribute(name="capital")
	String capitalName = "HUMAN";
	Capital capital;
	
	public double apply( Cell c )
	{
		return c.getEffectiveCapitals().getDouble( capital );
	}

	public String getDefaultOutputName()
	{
		return "Capital-"+capital.getName();
	}

	public void initialise() throws Exception
	{
		super.initialise();
		capital = modelData.capitals.forName( capitalName );
	}

	public boolean isInt() { return false; }
}
