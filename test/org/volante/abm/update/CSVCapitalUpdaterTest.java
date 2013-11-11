package org.volante.abm.update;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;
import org.volante.abm.data.*;
import org.volante.abm.example.*;
import org.volante.abm.serialization.ABMPersister;
import org.volante.abm.update.CSVCapitalUpdater;

import com.csvreader.CsvReader;

public class CSVCapitalUpdaterTest extends BasicTests
{

	@Test
	public void testGeneralOperation() throws Exception
	{
		
		CSVCapitalUpdater updater = new CSVCapitalUpdater();
		updater.yearlyFilenames.put(2000,"csv/Region1-2000.csv");
		updater.yearlyFilenames.put(2001,"csv/Region1-2001.csv");
		updater = persister.roundTripSerialise( updater );
		Region r = setupWorldWithUpdater( 2000, updater, c11, c12, c21, c22 );
		
		runInfo.getSchedule().tick(); //Does 2000 tick
		checkRegionCells( r, "csv/Region1-2000.csv" );
		runInfo.getSchedule().tick();
		checkRegionCells( r, "csv/Region1-2001.csv" );
	}
	
	@Test
	public void testYearlyFilename() throws Exception
	{
		CSVCapitalUpdater updater = new CSVCapitalUpdater();
		updater.filename = "csv/Region1-%y.csv";
		updater.yearInFilename = true;
		Region r = setupWorldWithUpdater( 2000, updater, c11, c12, c21, c22 );
		
		runInfo.getSchedule().tick(); //Does 2000 tick
		checkRegionCells( r, "csv/Region1-2000.csv" );
		runInfo.getSchedule().tick();
		checkRegionCells( r, "csv/Region1-2001.csv" );
	}
	
	public Region setupWorldWithUpdater( int year, AbstractUpdater updater, Cell...cells ) throws Exception
	{
		Region r = setupBasicWorld( cells );
		updater.initialise( modelData, runInfo, r );
		runInfo.getSchedule().register( updater );
		runInfo.getSchedule().setStartTick( year );
		return r;
	}

	
	public void checkRegionCells( Region r, String csvFile ) throws IOException
	{
		CsvReader target = runInfo.getPersister().getCSVReader( csvFile );
		while( target.readRecord() )
		{
			Cell cell = r.getCell( Integer.parseInt(target.get("x")), Integer.parseInt(target.get("y")) );
			for( Capital c : modelData.capitals )
			{
				if( target.get( c.getName() ) != null )
				{
					double exp = Double.parseDouble( target.get(c.getName() ));
					double got = cell.getEffectiveCapitals().getDouble( c );
					assertEquals( "Capital " + c.getName(), exp, got, 0.00001 );
					System.out.println("Got: " + got + ", Exp: " + exp + " for " + c.getName() + " on " + cell );
				}
			}
		}
	}

}
