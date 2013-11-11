package org.volante.abm.visualisation;

import java.awt.event.ActionEvent;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import org.volante.abm.schedule.*;

public class ScheduleControls extends JPanel implements ScheduleStatusListener
{
	JButton step;
	JButton stepTillEnd;
	JTextField runUntil;
	JButton stop;
	AbstractAction stepAction;
	AbstractAction stepTillEndAction;
	AbstractAction stopNextTick;
	Schedule schedule;
	
	public ScheduleControls()
	{
		stepAction = getAction( "Step", new Runnable() { public void run() { schedule.incrementTick(); }} );
		stepTillEndAction = getAction( "Step Until", new Runnable() { public void run() { schedule.setTargetTick( Integer.parseInt( runUntil.getText() ) ); }} );
		setLayout( new BoxLayout( this, BoxLayout.X_AXIS ));
		stopNextTick = getAction( "Stop", new Runnable() { public void run() { schedule.setTargetTick( schedule.getCurrentTick() ); }} );
		
		step = new JButton( stepAction ) ;
		stepTillEnd = new JButton( stepTillEndAction );
		stop = new JButton(stopNextTick);
		runUntil = new JTextField( "...", 5 );
		
		
		add( step );
		add( stepTillEnd );
		add( runUntil );
		add( stop );
	}
	
	public ScheduleControls( Schedule s )
	{
		this();
		setSchedule( s );
		//runUntil.setText( schedule.getEndTick()+"" );
	}
	
	public void setSchedule( Schedule s )
	{
		this.schedule = s;
		schedule.addStatusListener( this );
	}
	
	public void scheduleStatus( ScheduleStatusEvent e )
	{
		
	}

	public AbstractAction getAction(String name, final Runnable run)
	{
		return new AbstractAction(name)
		{
			public void actionPerformed( ActionEvent arg0 ) { SwingUtilities.invokeLater( run ); }
		};
		
	}
	
}
