package org.volante.abm.visualisation;

import java.awt.event.*;
import java.util.Collection;

import javax.swing.*;

import org.simpleframework.xml.Attribute;
import org.volante.abm.data.*;
import org.volante.abm.example.SimpleCapital;
import org.volante.abm.schedule.RunInfo;

import com.moseph.modelutils.fastdata.*;

public abstract class DatatypeDisplay<T> extends MaxMinCellDisplay implements Display, ActionListener
{
	@Attribute(name="initial",required=false)
	String initialType = null;
	private JComboBox controls;
	

	public void initialise( ModelData data, RunInfo info, Regions region ) throws Exception
	{
		super.initialise( data, info, region );
		setupControls();
		setTypeName( initialType );
	}
	
	public void setTypeName( String typeName )
	{
		if( typeName == null ) return;
		setupType( typeName );
		update();
		repaint();
	}
	
	public abstract void setupType( String type );
	public abstract Collection<String> getNames();
	
	public void setupControls()
	{
		controls = new JComboBox();
		for( String s : getNames() ) controls.addItem( s );
		if( initialType != null ) controls.setSelectedItem( initialType );
		controls.addActionListener( this );
	}
	
	public JComponent getControls() { return controls; }


	public void actionPerformed( ActionEvent e )
	{
		setTypeName( controls.getSelectedItem() + "" );
	}
}
