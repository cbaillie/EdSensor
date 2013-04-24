package uk.ac.dotrural.quality.edsensor.parser;

import java.util.ArrayList;

import uk.ac.dotrural.quality.edsensor.observation.*;

public class LineParser {
	
	private String lastTime = "04/11/2013 05:02:03";
	
	public ArrayList<Observation> parse(String line)
	{
		ArrayList<Observation> observations = new ArrayList<Observation>();
		String[] obs = line.split(",");
		
		try
		{
			String date = obs[18];
			String time = obs[0];
			//String timeM = obs[2];
			String avgX = (obs[5].equals("") ? "0" : obs[3]);
			String avgY = (obs[8].equals("") ? "0" : obs[6]);
			String avgZ = (obs[11].equals("") ? "0" : obs[9]);
			String hum = (obs[12].equals("") ? "0" : obs[10]);
			String temp = (obs[13].equals("") ? "0" : obs[11]);
			String lat = (obs[14].equals("") ? "0" : obs[12]);
			String lon = (obs[15].equals("") ? "0" : obs[13]);
			String alt = (obs[16].equals("") ? "0" : obs[14]);
			String speed = (obs[17].equals("") ? "0" : obs[15]);
			String sat = (obs[18].equals("") ? "0" : obs[16]);
			String prec = (obs[19].equals("") ? "0" : obs[17]);
			//String event = (obs[20].equals("") ? "0" : obs[20]);
			String event = "Coastal walk";
			
			if(time.contains("#VALUE") || time.length() == 0)
			{
				tick();
				time = lastTime;
			} 
			else
			{
				time = date.concat(" " + time);
				lastTime = time;
			}
			
			observations.add(new AccelerometerObservation(ObservationType.ACCELERATION, "X", time, avgX, event));
			observations.add(new AccelerometerObservation(ObservationType.ACCELERATION, "Y", time, avgY, event));
			observations.add(new AccelerometerObservation(ObservationType.ACCELERATION, "Z", time, avgZ, event));
			observations.add(new Observation(ObservationType.HUMIDITY, time, hum, event));
			observations.add(new Observation(ObservationType.TEMPERATURE, time, temp, event));
			observations.add(new GPSObservation(ObservationType.GPS, time, lat, lon, sat, prec, event));
			observations.add(new AltitudeObservation(ObservationType.ALTITUDE, time, alt, sat, event));
			observations.add(new SpeedObservation(ObservationType.SPEED, time, speed, event));
		}
		catch(ArrayIndexOutOfBoundsException aex)
		{

		}
		catch(Exception ex)
		{
			//System.out.println("LineParser Exception: " + ex.toString());
			ex.printStackTrace();
		}
		return observations;
	}
	
	public void tick()
	{
		String[] dateArr = lastTime.split("\\s+");
		String time = dateArr[1];
		
		Integer hours, minutes, secs;
		
		String[] timeArr = time.split(":");
		hours = Integer.parseInt(timeArr[0]);
		minutes = Integer.parseInt(timeArr[1]);
		secs = Integer.parseInt(timeArr[2]);
		
		if(secs == 59)
		{
			if(minutes == 59)
			{
				if(hours == 23)
					hours = 0;
				else
					hours++;
				minutes = 0;
			}
			else
				minutes++;
			secs = 0;
		}
		else
			secs++;

		StringBuilder sb = new StringBuilder();
		
		if(hours < 10)
			sb.append("0" + hours);
		else
			sb.append(hours);
		
		sb.append(":");
		
		if(minutes < 10)
			sb.append("0" + minutes);
		else
			sb.append(minutes);
		
		sb.append(":");
		
		if(secs < 10)
			sb.append("0" + secs);
		else
			sb.append(secs);
			
		lastTime = dateArr[0] + ' ' + sb.toString();
	}

}
