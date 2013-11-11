package org.volante.abm.schedule;

public class ScheduleThread implements Runnable
{
	Schedule schedule;
	
	public ScheduleThread( Schedule sched )
	{
		this.schedule = sched;
	}

	public void start()
	{
		Thread t = new Thread( this );
		t.start();
	}
	public void run()
	{
		//while( schedule.getEndTick() < 0 || schedule.getEndTick() >= schedule.getCurrentTick() )
		while( true )
		{
			if( shouldRun() )
				schedule.tick();
			else try { Thread.sleep( 1000 ); } 
				catch (InterruptedException e) { } //Don't care if we're interrupted
		}
	}
	
	public boolean shouldRun()
	{
		if( schedule.getCurrentTick() <= schedule.getTargetTick() )
		{
			return true;
		}
		return false;
	}

}
