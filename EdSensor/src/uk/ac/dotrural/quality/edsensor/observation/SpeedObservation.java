package uk.ac.dotrural.quality.edsensor.observation;

import java.util.ArrayList;
import java.util.UUID;

public class SpeedObservation extends Observation {
	
	public SpeedObservation(ObservationType property, String time, String value, String event)
	{
		super(property, time, value, event);
	}
	
	public SpeedObservation(ObservationType property, String time, String value, String event, boolean derived, ArrayList<Observation> derivedFrom)
	{
		super(property, time, value, event, derived, derivedFrom);
	}
	
	public String getModel(String NS)
	{
		StringBuilder sb = new StringBuilder();
		
		sb.append("INSERT DATA {\n");
		
		String observationUri = NS + "Observation/" + id;
		String sensorOutputUri = NS + "SensorOutput/" + UUID.randomUUID();
		String observationValueUri = NS + "ObservationValue/" + UUID.randomUUID();
		
		//Observation
		sb.append("\t<" + observationUri + "> a <http://purl.oclc.org/NET/ssnx/ssn#Observation> . \n");
		sb.append("\t<" + observationUri + "> a <http://www.w3.org/ns/prov-o/Entity> . \n");
		sb.append("\t<" + observationUri + "> <http://purl.oclc.org/NET/ssnx/ssn#featureOfInterest> <" + NS + "Features/EdJourney1> . \n");
		sb.append("\t<" + observationUri + "> <http://purl.oclc.org/NET/ssnx/ssn#observationResult> <" + sensorOutputUri + "> . \n");
		sb.append("\t<" + observationUri + "> <http://purl.oclc.org/NET/ssnx/ssn#observedBy> <" + NS + "Sensor/" + ObservationType.lookup(property) + "> . \n");
		sb.append("\t<" + observationUri + "> <http://purl.oclc.org/NET/ssnx/ssn#observedProperty> <" + NS + "Property/" + ObservationType.lookup(property) + "> . \n");
		sb.append("\t<" + observationUri + "> <http://purl.oclc.org/NET/ssnx/ssn#observationResultTime> " + time + " . \n");
		sb.append("\t<" + observationUri + "> <http://purl.oclc.org/NET/ssnx/ssn#observationServerTime> " + System.currentTimeMillis() + " . \n\n");
		sb.append("\t<" + observationUri + "> <" + NS + "Event> \"" + super.event + "\" . \n\n");
		
		//SensorOutput
		sb.append("\t<" + sensorOutputUri + "> a <http://purl.oclc.org/NET/ssnx/ssn#SensorOutput> . \n");
		sb.append("\t<" + sensorOutputUri + "> a <http://www.w3.org/ns/prov-o/Entity> . \n");
		sb.append("\t<" + sensorOutputUri + "> <http://purl.oclc.org/NET/ssnx/ssn#hasValue> <" + observationValueUri + "> . \n\n");
		
		//ObservationValue
		sb.append("\t<" + observationValueUri + "> a <http://purl.oclc.org/NET/ssnx/ssn#ObservationValue> . \n");
		sb.append("\t<" + observationValueUri + "> a <http://www.w3.org/ns/prov-o/Entity> . \n");
		sb.append("\t<" + observationValueUri + "> <http://purl.oclc.org/NET/ssnx/ssn#hasValue> " + super.value + " . \n");
		
		if(derivedObservation)
		{
			sb = followDerivationPath(NS, sb);
		}
		
		sb.append("}");
		
		return sb.toString();
	}
	
	public StringBuilder followDerivationPath(String NS, StringBuilder sb)
	{
		for(int i=0;i<derivedFrom.size();i++)
		{
			GPSObservation derivedFromObs = (GPSObservation)super.derivedFrom.get(i);
			sb.append("\t\t<" + NS + "Observation/" + id+ "> <http://www.w3.org/ns/prov-o/wasDerivedFrom> <" + NS + "Observation/" + derivedFromObs.id + "> . \n");
		}
		return sb;
	}
	
	
	public Long getTime()
	{
		return Long.parseLong(time);
	}
	
	public String toString()
	{	
		return super.toString();
	}

}
