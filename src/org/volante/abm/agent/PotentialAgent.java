package org.volante.abm.agent;

import org.volante.abm.data.*;
import org.volante.abm.schedule.RunInfo;
import org.volante.abm.serialization.Initialisable;

import com.moseph.modelutils.fastdata.*;
 /**
  * This is an interface for classes that create agents
  */
public interface PotentialAgent extends Initialisable
{
	public UnmodifiableNumberMap<Service> getPotentialSupply( Cell cell );
	public Agent createAgent( Region region, Cell... cells );
	public String getID();
	public int getSerialID();
	public double getGivingUp();
	public double getGivingIn();
	
	public static final int UNKNOWN_SERIAL = -1;
	
	public static final PotentialAgent NOT_MANAGED_TYPE = new PotentialAgent()
	{
		public void initialise( ModelData data, RunInfo info, Region extent ) throws Exception { }
		public UnmodifiableNumberMap<Service> getPotentialSupply( Cell cell )
		{
			return cell.getRegion().getModelData().serviceMap();
		}

		public Agent createAgent( Region region, Cell... cells ) { return Agent.NOT_MANAGED; }
		public String getID() { return Agent.NOT_MANAGED_ID; }
		public int getSerialID() { return -1; }
		public double getGivingUp() { return 0; }
		public double getGivingIn() { return 0; }
		
	};
	
}
