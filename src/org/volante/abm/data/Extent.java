package org.volante.abm.data;
import static java.lang.Math.*;

public class Extent
{
	int minX = Integer.MAX_VALUE;
	int maxX = Integer.MIN_VALUE;
	int minY = Integer.MAX_VALUE;
	int maxY = Integer.MIN_VALUE;
	int width = 0;
	int height = 0;
	
	public int getMinX() { return minX; }
	public int getMaxX() { return maxX; }
	public int getMinY() { return minY; }
	public int getMaxY() { return maxY; }
	
	public void update( int x, int y )
	{
		minX = min(minX,x);
		maxX = max(maxX,x);
		minY = min(minY,y);
		maxY = max(maxY,y);
		updateHeightWidth();
	}
	
	public void update( Cell c )
	{
		update( c.x, c.y );
	}

	public void update( Extent e )
	{
		minX = min(minX,e.minX);
		maxX = max(maxX,e.maxX);
		minY = min(minY,e.minY);
		maxY = max(maxY,e.maxY);
		updateHeightWidth();
	}
	
	public void updateHeightWidth()
	{
		width = abs( maxX - minX )+1;
		height = abs( maxY - minY )+1;
	}
	
	public String toString()
	{
		return "Extent (" + minX + "," + minY + "),(" + maxX + "," + maxY + ")";
	}
	public int getWidth()
	{
		return width;
	}
	public int getHeight()
	{
		return height;
	}
	public void setMinY( int minY )
	{
		this.minY = minY;
	}
	
	//Get the zero-indexed address of the cell for putting into arrays
	public int xInd( int x ) { return x-minX; }
	public int yInd( int y ) { return y-minY; }
	
	//Get the cell coordinates corresponding to the given address
	public int indToX( int ind ) { return ind + minX; }
	public int indToY( int ind ) { return ind + minY; }
}
