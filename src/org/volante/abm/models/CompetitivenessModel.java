package org.volante.abm.models;

import org.volante.abm.data.*;
import org.volante.abm.serialization.Initialisable;
import org.volante.abm.visualisation.*;

import com.moseph.modelutils.fastdata.*;

public interface CompetitivenessModel extends Initialisable, Displayable
{
	public double getCompetitveness( DemandModel demand, UnmodifiableNumberMap<Service> supply, Cell cell );
	public double getCompetitveness( DemandModel demand, UnmodifiableNumberMap<Service> supply );
	public double addUpMarginalUtilities( UnmodifiableNumberMap<Service> demand, UnmodifiableNumberMap<Service> supply );
	
	public CompetitivenessDisplay getDisplay();
	
	public interface CompetitivenessDisplay extends Display {}
}
