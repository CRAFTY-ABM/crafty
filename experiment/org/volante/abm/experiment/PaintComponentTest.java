package org.volante.abm.experiment;

import java.awt.*;
import java.awt.image.BufferedImage;

import javax.swing.*;

import sun.swing.SwingUtilities2;

public class PaintComponentTest
{
	public static void main( String[] args )
	{
		JFrame frame = new JFrame();
		
		JLabel label = new JLabel("Hello");
		label.setBackground( Color.orange );
		JPanel panel = new JPanel();
		panel.add( label );
		panel.setSize( 100, 100 );
		panel.setPreferredSize( new Dimension( 100, 100 ) );
		BufferedImage image = new BufferedImage( 300, 300, BufferedImage.TYPE_INT_RGB );
		Graphics2D g = image.createGraphics();
		g.setColor( Color.yellow );
		g.fillRect( 0, 0, 300, 300 );
		panel.setBackground( Color.green );
		//panel.paintComponents( g );
		CellRendererPane cp = new CellRendererPane();
		cp.setBackground( Color.blue );
		cp.add( panel );
		layoutComponent( panel );
		cp.paintComponent( g, panel, cp, 0, 0, 300, 300 );
		//SwingUtilities.paintComponent( g, panel, new Container(), 0, 0, 300, 300 );
		
		
		frame.setSize( 350, 350 );
		frame.add(new JLabel( new ImageIcon(image) ));
		//frame.add(panel);
		//frame.add(cp);
		frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
		frame.setVisible( true );
	}
	
	public static void layoutComponent(Component component) {
	    synchronized (component.getTreeLock()) {
	        component.doLayout();

	        if (component instanceof Container) {
	            for (Component child : ((Container) component).getComponents()) {
	                layoutComponent(child);
	            }
	        }
	    }
	}

}
