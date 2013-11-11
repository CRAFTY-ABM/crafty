package org.volante.abm.update;

import org.simpleframework.xml.Attribute;
import org.volante.abm.data.*;
import org.volante.abm.schedule.RunInfo;
import org.volante.abm.update.AgentTypeUpdater.CapitalUpdateFunction;

/**
 * Expresses change of value as a proportion of difference with top value (for +ve numbers)
 * or bottom value (for -ve numbers)
 * @author dmrust
 *
 */
public class ProportionalChangeFunction implements CapitalUpdateFunction
{
	Capital capital;
	@Attribute(required=false)
	double top = 1;
	@Attribute(required=false)
	double bottom = 0;
	@Attribute()
	double change = 0;
	@Attribute
	String capitalName;
	
	public ProportionalChangeFunction() {};
	public ProportionalChangeFunction( Capital c, double change )
	{
		this.capital = c;
		this.change = change;
	}
	
	public ProportionalChangeFunction( Capital c, double change, double top, double bottom )
	{
		this( c, change );
		this.top = top;
		this.bottom = bottom;
	}
	
	/**
	 * Applies the function to the cell. If you want to do something more complex (i.e. involving cell values)
	 * override this.
	 */
	public void apply( Cell c )
	{
		c.getModifiableBaseCapitals().put( capital, c.getBaseCapitals().getDouble( capital ) );
	}
	
	/**
	 * The actual update function. If you want to change the calculation, override this.
	 * @param value
	 * @return
	 */
	public double function( double value )
	{
		//e.g. top = 0.8, value = 0.4, change = 0.5 -> 0.4 + (0.8-0.4)*0.5 -> 0.6
		if (change > 0 ) return value + (top-value) * change;
		// e.g. bottom = 0.2, value = 0.6, change = -0.5 -> 0.6 - (0.2-0.6) * (-0.5) -> 0.6 - (-0.4*-0.5) -> 0.4
		if( change < 0 ) return value - (bottom-value) * change;
		return value;
	}

	public void initialise( ModelData data, RunInfo info, Region extent ) throws Exception
	{
		if( capital == null ) capital = data.capitals.forName( capitalName );
	}
}