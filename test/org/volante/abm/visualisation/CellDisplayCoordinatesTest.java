package org.volante.abm.visualisation;

import static org.junit.Assert.*;

import java.awt.Color;

import org.junit.Test;
import org.volante.abm.data.Cell;
import org.volante.abm.example.*;
import org.volante.abm.models.DemandModel;

public class CellDisplayCoordinatesTest extends BasicTests
{

	private CellDisplay display;

	@Test
	public void testCellPixelConversions() throws Exception
	{
		setupRegion( -5, 10, -3, 11 ); //16 cells high, 15 wide
		display = new CellDisplay() { public int getColourForCell( Cell c ) { return 0; } };
		r1.initialise( modelData, runInfo, null );
		display.initialise( modelData, runInfo, r1 );
		display.update();
		checkCellAtLocation( 0, 0, -5, -3 );
		checkCellAtLocation( 15, 14, 10, 11 );
		checkCellToImage( -5, -3, 0, 14 );
		checkCellToImage( 10, 11, 15, 0 );
		
		checkSelection( 0, 0, -5, -3 );
		checkSelection( 15, 14, 10, 11 );
		moveSelection( 0, 0, 10, 11 );
		moveSelection( -1, 0, 9, 11 );
		moveSelection( 0, -1, 9, 10 );
		
	}
	
	public void setupRegion( int minX, int maxX, int minY, int maxY )
	{
		for( int x = minX; x <= maxX; x++ ) for( int y = minY; y <= maxY; y++ )
		{
			Cell c = new Cell( x, y );
			c.initialise( modelData, runInfo, r1 );
			r1.addCell( c );
		}
		
	}

	public void checkCellAtLocation( int x, int y, int cx, int cy )
	{
		Cell c = display.cells[x][y];
		assertEquals(cx,c.getX());
		assertEquals(cy,c.getY());
	}
	public void checkCellToImage( int cx, int cy, int ix, int iy )
	{
		assertEquals( ix, display.cXtoIX( cx ));
		assertEquals( iy, display.cYtoIY( cy ));
	}
	
	public void checkSelection( int selX, int selY, int cx, int cy )
	{
		display.setSelectedCell( selX, selY );
		assertEquals( display.cells[selX][selY], display.selected );
		assertEquals( cx, display.selectedX );
		assertEquals( cy, display.selectedY );
		Cell c = display.selected;
		assertEquals( cx, c.getX() );
		assertEquals( cy, c.getY() );
	}
	
	public void moveSelection( int dx, int dy, int cx, int cy )
	{
		display.moveSelection( dx, dy );
		Cell c = display.selected;
		assertEquals( cx, c.getX() );
		assertEquals( cy, c.getY() );
	}
}
