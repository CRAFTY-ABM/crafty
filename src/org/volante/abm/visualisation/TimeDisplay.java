package org.volante.abm.visualisation;


import java.awt.Dimension;

import javax.swing.*;

import org.volante.abm.schedule.*;

import static java.awt.Color.*;

public class TimeDisplay extends JPanel implements ScheduleStatusListener
{
	JLabel tick = new JLabel("0");
	JLabel status = new JLabel("Not started");
	JPanel running = new JPanel();
	int height = 20;
	private Schedule schedule;
	
	
	public TimeDisplay()
	{
		running.setBackground( ORANGE );
		setLayout( new BoxLayout( this, BoxLayout.X_AXIS ));
		tick.setPreferredSize( new Dimension( 100, height ) );
		status.setPreferredSize( new Dimension( 100, height ) );
		running.setPreferredSize( new Dimension( height, height ) );
		add(new JLabel("Year:"));
		add( tick );
		add(new JLabel("Status:"));
		add( status );
		add(new JLabel("Running:"));
		add( running );
	}
	public TimeDisplay( Schedule s )
	{
		this();
		setSchedule( s );
	}
	
	
	public void setSchedule( Schedule s )
	{
		this.schedule = s;
		tick.setText( s.getCurrentTick() +"" );
		s.addStatusListener( this );
	}

	public void scheduleStatus( ScheduleStatusEvent e )
	{
		tick.setText( e.getTick() + "" );
		status.setText( e.getStage().name() );
		running.setBackground( e.isRunning() ? green : red );
	}

}
