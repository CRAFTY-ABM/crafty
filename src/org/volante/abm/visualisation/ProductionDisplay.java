package org.volante.abm.visualisation;

import java.awt.event.ActionListener;
import java.util.*;

import javax.swing.JFrame;

import org.volante.abm.data.*;

public class ProductionDisplay extends DatatypeDisplay<Service> implements Display, ActionListener
{
	Service service = null;

	public double getVal( Cell c )
	{
		if( service == null ) return Double.NaN;
		return c.getSupply().getDouble( service );
	}

	public Collection<String> getNames()
	{
		Set<String> names = new HashSet<String>();
		for( Service s : data.services ) names.add( s.getName() );
		return names;
	}
	
	public void setupType( String type )
	{
		service = data.services.forName( type );
	}
	
	
	

	

}
