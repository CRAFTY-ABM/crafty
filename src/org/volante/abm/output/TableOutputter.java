package org.volante.abm.output;

import java.io.IOException;
import java.util.*;

import org.volante.abm.data.*;
import org.volante.abm.schedule.*;

import com.csvreader.CsvWriter;

public abstract class TableOutputter<T> extends AbstractOutputter
{
	List<TableColumn<T>> columns = new ArrayList<TableColumn<T>>();
	CsvWriter writer;
	
	public void addColumn( TableColumn<T> col ) { columns.add(col); }
	
	public void doOutput( Regions r )
	{
		String filename = filePerTick() ? tickFilename( r ) : filename( r );
		try 
		{
			if( filePerTick() ) startFile( filename );
			else if( writer == null ) startFile( filename);
			writeData( getData( r ), r );
		} catch( Exception e )
		{
			log.error( "Couldn't write file " + filename + ": " + e.getMessage(), e );
		}
		if( filePerTick() ) endFile();
	}
	
	public abstract Iterable<T> getData( Regions r );
	
	public void startFile( String filename ) throws IOException
	{
		endFile();
		writer = new CsvWriter( filename );
		String[] headers = new String[columns.size()];
		for( int i = 0; i < columns.size(); i++ ) headers[i] = columns.get(i).getHeader();
		writer.writeRecord( headers );
	}
	public void writeData( Iterable<T> data, Regions r ) throws IOException
	{
		String[] output = new String[columns.size()];
		for( T d : data )
		{
			for( int i = 0; i < columns.size(); i++ ) output[i] = columns.get(i).getValue(d, modelData, runInfo, r);
			writer.writeRecord( output );
		}
	}
	
	public void endFile()
	{
		if( writer != null ) writer.close();
		writer = null;
	}
	
	public void close() { endFile(); }

	/**
	 * Gets the current tick. Generics is ignored
	 * @author dmrust
	 *
	 * @param <Capital>
	 */
	public static class TickColumn<T> implements TableColumn<T>
	{
		public String getHeader() { return "Tick"; }
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
		public String getHeader() { return "Region"; }
		public String getValue( T t, ModelData data, RunInfo info, Regions r ) { return r.getID(); }
	}
	
	public boolean filePerTick() { return true; }



}
