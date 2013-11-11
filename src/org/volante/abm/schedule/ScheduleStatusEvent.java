package org.volante.abm.schedule;

public class ScheduleStatusEvent
{
	boolean running = false;
	int tick;
	ScheduleStage stage;
	

	public ScheduleStatusEvent( int tick, ScheduleStage stage, boolean running )
	{
		super();
		this.tick = tick;
		this.stage = stage;
		this.running = running;
	}


	public static enum ScheduleStage
	{
		PRE_TICK,
		MAIN_LOOP,
		POST_TICK,
		PAUSED;
	}


	public boolean isRunning() { return running; } 
	public int getTick() { return tick; } 
	public ScheduleStage getStage() { return stage; }
}
