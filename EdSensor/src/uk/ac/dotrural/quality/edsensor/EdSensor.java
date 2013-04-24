package uk.ac.dotrural.quality.edsensor;

import java.util.ArrayList;

import uk.ac.dotrural.quality.edsensor.observation.*;
import uk.ac.dotrural.quality.edsensor.parser.LineParser;
import uk.ac.dotrural.quality.edsensor.reader.LogReader;
import uk.ac.dotrural.quality.edsensor.sparql.Updater;

public class EdSensor {
	
	private final String file = "CoastalWalk";
	private final String filename = file + ".csv";
	private final String filepath = "resource/" + filename;
	
	private final String NS = "http://dtp-126.sncs.abdn.ac.uk/quality/SensorBox/";
	
	private final String storename = "http://dtp-126.sncs.abdn.ac.uk:8080/openrdf-sesame/repositories/" + file;
	private final String endpoint = storename.concat("/statements");
	
	private int querySent = 0;
	
	ArrayList<Observation> observations;
	
	public static void main(String[] args) {
		new EdSensor();
	}
	
	public EdSensor()
	{
		run();
	}
	
	private void run()
	{
		System.out.println("Starting");
		System.out.println("===================");
		System.out.println("== Parsing File ===");

		observations = parseLogFile();
		
		System.out.println("=== File parsed ===");
		
		System.out.println("=== Storing Obs ===");
		storeObservations(observations);
		
		System.out.println("===================");
		System.out.println("Finished");
	}

	private ArrayList<Observation> parseLogFile()
	{
		LogReader reader = new LogReader();
		ArrayList<Observation> observations = new ArrayList<Observation>();
		ArrayList<String> lines = reader.readLog(filepath);
		System.out.println("== Log File Read ==");
		lines.remove(0);
		
		for(int i=0;i<lines.size();i++)
		{
			LineParser lp = new LineParser();
			ArrayList<Observation> obs = lp.parse((String)lines.get(i));
			for(int j=0;j<obs.size();j++)
			{
				observations.add(obs.get(j));
			}
		}
		return observations;
	}
	
	private void storeObservations(ArrayList<Observation> observations)
	{
		String query = "";
		try
		{
			for(int i=0;i<observations.size();i++)
			{
				query = "";
				Observation obs = (Observation)observations.get(i);
				switch(obs.property)
				{
				case ACCELERATION:
					AccelerometerObservation acc = (AccelerometerObservation)obs;
					query = acc.getModel(NS);				
					break;
				case GPS:
					GPSObservation gps = (GPSObservation)obs;
					query = gps.getModel(NS);
					break;
				case ALTITUDE:
					AltitudeObservation alt = (AltitudeObservation)obs;
					query = alt.getModel(NS);
					break;
				default:
					query = obs.getModel(NS);
					break;
				}
				Updater update = new Updater();
				update.sendUpdate(endpoint, this, query);
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}

	public void status()
	{
		querySent++;
		System.out.println(querySent + " of " + observations.size() + " sent.");
	}
	
}
