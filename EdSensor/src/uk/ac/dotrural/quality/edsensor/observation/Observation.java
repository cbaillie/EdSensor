package uk.ac.dotrural.quality.edsensor.observation;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.UUID;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

public class Observation implements Comparable<Observation> {
	
	public String id;
	public String old_id;
	public ObservationType property;
	public String time;
	public String sTime;
	public String value = null;
	public String event;
	
	public String foi;
	public String obsBy;
	public String result;
	public String obsVal;
	
	public boolean derivedObservation = false;
	public ArrayList<Observation> derivedFrom;
	
	public final String SSN = "http://purl.oclc.org/NET/ssnx/ssn#";
	public final String RDF = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	
	public Observation(ObservationType property, String time, String v, String event)
	{
		id = "" + UUID.randomUUID();
		derivedFrom = new ArrayList<Observation>();
		
		this.property = property;
		this.time = parseTime(time);
		
		Double valDouble = Double.parseDouble(v);
		if(valDouble.isNaN())
			System.out.println("Observation constructor NaN: " + v);
		
		value = v;
		this.event = event;
	}
	
	public Observation(ObservationType property, String time, String v, String event, boolean derivedObservation, ArrayList<Observation> derivedFrom)
	{
		id = "" + UUID.randomUUID();
		this.derivedFrom = new ArrayList<Observation>();
		
		this.property = property;
		this.time = time;
		
		Double valDouble = Double.parseDouble(v);
		if(valDouble.isNaN())
			System.out.println("Observation constructor (derived) NaN: " + v);

		value = v;
		
		this.event = event;
		
		this.derivedObservation = true;
		this.derivedFrom = derivedFrom;
	}
	
	public Observation(String id, ObservationType property, String time, String v, boolean derivedObservation, ArrayList<Observation> derivedFrom)
	{
		this.id = "" + id;
		this.derivedFrom = new ArrayList<Observation>();
		
		this.property = property;
		this.time = time;
		
		Double valDouble = Double.parseDouble(v);
		if(valDouble.isNaN())
			System.out.println("Observation constructor (derived) NaN: " + v);

		value = v;
		
		this.derivedObservation = true;
		this.derivedFrom = derivedFrom;
	}
	
	public Observation(String id, ObservationType property, String foi, String obsBy, String result, String obsVal, String value, String rTime, String sTime, String event)
	{
		this.id = id;
		this.property = property;
		this.foi = foi;
		this.obsBy = obsBy;
		this.result = result;
		this.obsVal = obsVal;
		this.value = value;
		time = rTime;
		this.sTime = sTime;
		this.event = event;
	}
	
	public Long getTime()
	{
		return Long.parseLong(time);
	}
	
	private String parseTime(String timeStr)
	{
		try
		{
			String[] arr = timeStr.split("\\s");		
			String date = arr[0];
			String time = arr[1];
				
			//Sort date
			String[] dates = date.split("/");
			String day = dates[1];
			String month = dates[0];
			String year = dates[2];
			
			String[] times = time.split(":");
			String hour = times[0];
			String minutes = times[1];
			String secs = times[2];
			
			Calendar cal = GregorianCalendar.getInstance();
			cal.clear();
			cal.set(Integer.parseInt(year), (Integer.parseInt(month)-1), Integer.parseInt(day), Integer.parseInt(hour), Integer.parseInt(minutes), Integer.parseInt(secs));
			
			return "" + cal.getTimeInMillis();
		}
		catch(Exception ex)
		{
			return "0";
		}
	}
	
	public void describe()
	{
		System.out.println("ID: " + id);
		System.out.println("MEASURES: " + property);
		System.out.println("TIME: " + time);
		if(value != null)
			System.out.println("VALUE: " + value);
		
		if(derivedObservation && (property == ObservationType.TEMPERATURE || property == ObservationType.HUMIDITY))
		{
			for(int i=0;i<derivedFrom.size();i++)
			{
				System.out.println("");
				System.out.println("Derived from: ");
				Observation obs = (Observation)derivedFrom.get(i);
				obs.describe();
			}
		}
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
		sb.append("\t<" + observationUri + "> <http://dtp-126.sncs.abdn.ac.uk/quality/CarSensor/Event> \"" + event + "\" . \n\n");
		
		//SensorOutput
		sb.append("\t<" + sensorOutputUri + "> a <http://purl.oclc.org/NET/ssnx/ssn#SensorOutput> . \n");
		sb.append("\t<" + sensorOutputUri + "> a <http://www.w3.org/ns/prov-o/Entity> . \n");
		sb.append("\t<" + sensorOutputUri + "> <http://purl.oclc.org/NET/ssnx/ssn#hasValue> <" + observationValueUri + "> . \n\n");
		
		//ObservationValue
		sb.append("\t<" + observationValueUri + "> a <http://purl.oclc.org/NET/ssnx/ssn#ObservationValue> . \n");
		sb.append("\t<" + observationValueUri + "> a <http://www.w3.org/ns/prov-o/Entity> . \n");
		sb.append("\t<" + observationValueUri + "> <http://purl.oclc.org/NET/ssnx/ssn#hasValue> " + value + " . \n");
		
		if(derivedObservation)
		{
			sb = followDerivationPath(NS, this, sb);
		}
		
		sb.append("}");
		
		return sb.toString();
	}
	
	public StringBuilder followDerivationPath(String NS, Observation obs, StringBuilder sb)
	{
		for(int i=0;i<obs.derivedFrom.size();i++)
		{
			Observation derivedFromObs = (Observation)obs.derivedFrom.get(i);
			sb.append("\t\t<" + NS + "Observation/" + obs.id+ "> <http://www.w3.org/ns/prov-o/wasDerivedFrom> <" + NS + "Observation/" + derivedFromObs.id + "> . \n");
		}
		return sb;
	}

	@Override
	public int compareTo(Observation obs) {
		return (int)(getTime() - obs.getTime());
	}
	
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("ID: " + id + '\n');
		sb.append("Type: " + property + '\n');
		sb.append("Time: " + time + '\n');
		
		if(value != null)
			sb.append("Value: " + value + '\n');
		
		return sb.toString();
	}
	
	public OntModel getRdfModel(String NS)
	{		
		OntModel sensorObservationModel = ModelFactory.createOntologyModel();
		Resource observationResource = sensorObservationModel.createResource(id);
		Statement observationTypeStmt = sensorObservationModel.createStatement(observationResource, sensorObservationModel.createProperty(RDF, "type"), sensorObservationModel.createResource(SSN + "Observation"));
		Statement observationResultStmt = sensorObservationModel.createStatement(observationResource, sensorObservationModel.createProperty(SSN, "observationResult"), sensorObservationModel.createResource(this.result));
		Statement resultTimeStmt = sensorObservationModel.createStatement(observationResource, sensorObservationModel.createProperty(SSN, "observationResultTime"), sensorObservationModel.createTypedLiteral(Long.parseLong(this.time)));
		Statement serverTimeStmt = sensorObservationModel.createStatement(observationResource, sensorObservationModel.createProperty(NS,"serverTimestamp"), sensorObservationModel.createTypedLiteral(Long.parseLong(sTime)));
		Statement observationResourceEventStmt = sensorObservationModel.createStatement(observationResource, sensorObservationModel.createProperty(NS, "Event"), sensorObservationModel.createTypedLiteral(event));
		
		Resource sensor = sensorObservationModel.createResource(obsBy);
		Statement sensorTypeStmt = sensorObservationModel.createStatement(sensor, sensorObservationModel.createProperty(RDF, "type"), sensorObservationModel.createResource(SSN + "SensingDevice"));
		Statement observedByStmt = sensorObservationModel.createStatement(observationResource, sensorObservationModel.createProperty(SSN, "observedBy"), sensor);
		
		Resource property = sensorObservationModel.createResource(NS + "Property/" + ObservationType.lookup(this.property));
		Statement propertyStmt = sensorObservationModel.createStatement(observationResource, sensorObservationModel.createProperty(SSN, "observedProperty"), property);
		Statement propertyTypeStmt = sensorObservationModel.createStatement(property, sensorObservationModel.createProperty(RDF, "type"), sensorObservationModel.createResource(SSN + "Property"));
		
		Resource featureOfInterest = sensorObservationModel.createResource(foi);
		Statement featureOfInterestStmt = sensorObservationModel.createStatement(observationResource, sensorObservationModel.createProperty(SSN,"featureOfInterest"), sensorObservationModel.createResource(this.foi));
		Statement featureOfInterestTypeStmt = sensorObservationModel.createStatement(featureOfInterest, sensorObservationModel.createProperty(RDF, "type"), sensorObservationModel.createResource(SSN + "FeatureOfInterest"));
		
		Resource sensorOutput = sensorObservationModel.createResource(result);
		Statement observationResultTypeStmt = sensorObservationModel.createStatement(sensorOutput, sensorObservationModel.createProperty(RDF, "type"), sensorObservationModel.createResource(SSN + "SensorOutput"));
		Statement observationValueStmt = sensorObservationModel.createStatement(sensorOutput, sensorObservationModel.createProperty(SSN, "hasValue"), sensorObservationModel.createResource(obsVal));
		
		Resource observationValue = sensorObservationModel.createResource(obsVal);
		Statement observationValueTypeStmt = sensorObservationModel.createStatement(observationValue, sensorObservationModel.createProperty(RDF, "type"), sensorObservationModel.createResource(SSN + "ObservationValue"));
		Statement observationValueValueStmt = sensorObservationModel.createStatement(observationValue, sensorObservationModel.createProperty(SSN, "hasValue"), sensorObservationModel.createTypedLiteral(value));
		
		sensorObservationModel.add(observationTypeStmt);
		sensorObservationModel.add(observationResultStmt);
		sensorObservationModel.add(resultTimeStmt);
		sensorObservationModel.add(serverTimeStmt);
		sensorObservationModel.add(featureOfInterestStmt);
		sensorObservationModel.add(featureOfInterestTypeStmt);
		sensorObservationModel.add(sensorTypeStmt);
		sensorObservationModel.add(observedByStmt);
		sensorObservationModel.add(propertyStmt);
		sensorObservationModel.add(propertyTypeStmt);
		sensorObservationModel.add(observationResourceEventStmt);
		sensorObservationModel.add(observationResultTypeStmt);
		sensorObservationModel.add(observationValueStmt);
		sensorObservationModel.add(observationValueTypeStmt);
		sensorObservationModel.add(observationValueValueStmt);
		
		return sensorObservationModel;
	}

}
