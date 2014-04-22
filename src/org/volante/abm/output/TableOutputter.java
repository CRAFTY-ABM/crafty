/**
 * This file is part of
 * 
 * CRAFTY - Competition for Resources between Agent Functional TYpes
 *
 * Copyright (C) 2014 School of GeoScience, University of Edinburgh, Edinburgh, UK
 * 
 * CRAFTY is free software: You can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software 
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *  
 * CRAFTY is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * School of Geoscience, University of Edinburgh, Edinburgh, UK
 * 
 */
package org.volante.abm.output;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.volante.abm.data.ModelData;
import org.volante.abm.data.Regions;
import org.volante.abm.schedule.RunInfo;

import com.csvreader.CsvWriter;

public abstract class TableOutputter<T> extends AbstractOutputter
{
	List<TableColumn<T>> columns = new ArrayList<TableColumn<T>>();
	CsvWriter writer;
	

	public void addColumn( TableColumn<T> col ) { columns.add(col); }
	
	@Override
	public void doOutput( Regions r )
	{
		String filename = filePerTick() ? tickFilename( r ) : filename( r );
		try 
		{
			if( filePerTick() ) {
				startFile( filename );
			} else if( writer == null ) {
				startFile( filename);
			}
			writeData( getData( r ), r );
		} catch( Exception e )
		{
			log.error( "Couldn't write file " + filename + ": " + e.getMessage(), e );
		}
		if( filePerTick() ) {
			endFile();
		}
	}
	
	public abstract Iterable<T> getData( Regions r );
	
	public void startFile( String filename ) throws IOException
	{
		endFile();
		writer = new CsvWriter( filename );
		String[] headers = new String[columns.size()];
		for( int i = 0; i < columns.size(); i++ ) {
			headers[i] = columns.get(i).getHeader();
		}
		writer.writeRecord( headers );
	}
	public void writeData( Iterable<T> data, Regions r ) throws IOException
	{
		String[] output = new String[columns.size()];
		for( T d : data )
		{
			for( int i = 0; i < columns.size(); i++ ) {
				output[i] = columns.get(i).getValue(d, modelData, runInfo, r);
			}
			writer.writeRecord( output );
		}
	}
	
	public void endFile()
	{
		if( writer != null ) {
			writer.close();
		}
		writer = null;
	}
	
	@Override
	public void close() { endFile(); }

	/**
	 * Gets the current tick. Generics is ignored
	 * @author dmrust
	 *
	 * @param <Capital>
	 */
	public static class TickColumn<T> implements TableColumn<T>
	{
		@Override
		public String getHeader() { return "Tick"; }
		@Override
		public String getValue( T t, ModelData data, RunInfo info, Regions r ) { return info.getSchedule().getCurrentTick() + ""; }
	}
	
	/**
	 * Gets the current region name. Generics is ignored
	 * @author dmrust
	 *
	 * @param <Capital>
	 */
	public static class RegionColumn<T> implements TableColumn<T>
	{
		@Override
		public String getHeader() {
			return "RegionSet";
		}
		@Override
		public String getValue( T t, ModelData data, RunInfo info, Regions r ) { return r.getID(); }
	}
	
	public boolean filePerTick() { return true; }



}
