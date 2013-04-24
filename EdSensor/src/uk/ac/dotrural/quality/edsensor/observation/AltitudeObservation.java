package uk.ac.dotrural.quality.edsensor.observation;

import java.util.ArrayList;
import java.util.UUID;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

public class AltitudeObservation extends Observation {
	
	public String satellites;
	
	public AltitudeObservation(ObservationType property, String time, String value, String s, String event)
	{
		super(property, time, value, event);
		satellites = s;
	}
	
	public AltitudeObservation(ObservationType property, String time, String value, String s, String event, boolean derived, ArrayList<Observation> derivedFrom)
	{
		super(property, time, value, event, derived, derivedFrom);
		satellites = s;
	}
	
	public AltitudeObservation(String id, ObservationType property, String foi, String obsBy, String result, String obsVal, String value, String satellites, String rTime, String sTime, String event)
	{
		super(id, property, foi, obsBy, result, obsVal, value, rTime, sTime, event);
		this.satellites = satellites;
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
		sb.append("\t<" + observationValueUri + "> <" + NS + "satellites> \"" + satellites + "\" . \n");
		
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
		StringBuilder sb = new StringBuilder();
		
		sb.append(super.toString());
		sb.append("Satellites: " + satellites + '\n');
		
		return sb.toString();
	}
	
	
	public OntModel getRdfModel(String NS)
	{		
		OntModel sensorObservationModel = ModelFactory.createOntologyModel();
		Resource observationResource = sensorObservationModel.createResource(super.id);
		Statement observationTypeStmt = sensorObservationModel.createStatement(observationResource, sensorObservationModel.createProperty(RDF, "type"), sensorObservationModel.createResource(SSN + "Observation"));
		Statement observationResultStmt = sensorObservationModel.createStatement(observationResource, sensorObservationModel.createProperty(SSN, "observationResult"), sensorObservationModel.createResource(super.result));
		Statement resultTimeStmt = sensorObservationModel.createStatement(observationResource, sensorObservationModel.createProperty(SSN, "observationResultTime"), sensorObservationModel.createTypedLiteral(Long.parseLong(super.time)));
		Statement serverTimeStmt = sensorObservationModel.createStatement(observationResource, sensorObservationModel.createProperty(NS,"serverTimestamp"), sensorObservationModel.createTypedLiteral(Long.parseLong(super.sTime)));
		Statement observationResourceEventStmt = sensorObservationModel.createStatement(observationResource, sensorObservationModel.createProperty(NS, "Event"), sensorObservationModel.createTypedLiteral(event));
		
		Resource sensor = sensorObservationModel.createResource(super.obsBy);
		Statement sensorTypeStmt = sensorObservationModel.createStatement(sensor, sensorObservationModel.createProperty(RDF, "type"), sensorObservationModel.createResource(SSN + "SensingDevice"));
		Statement observedByStmt = sensorObservationModel.createStatement(observationResource, sensorObservationModel.createProperty(SSN, "observedBy"), sensor);
		
		Resource property = sensorObservationModel.createResource(NS + "Property/" + ObservationType.lookup(this.property));
		Statement propertyStmt = sensorObservationModel.createStatement(observationResource, sensorObservationModel.createProperty(SSN, "observedProperty"), property);
		Statement propertyTypeStmt = sensorObservationModel.createStatement(property, sensorObservationModel.createProperty(RDF, "type"), sensorObservationModel.createResource(SSN + "Property"));
		
		Resource featureOfInterest = sensorObservationModel.createResource(super.foi);
		Statement featureOfInterestStmt = sensorObservationModel.createStatement(observationResource, sensorObservationModel.createProperty(SSN,"featureOfInterest"), sensorObservationModel.createResource(super.foi));
		Statement featureOfInterestTypeStmt = sensorObservationModel.createStatement(featureOfInterest, sensorObservationModel.createProperty(RDF, "type"), sensorObservationModel.createResource(SSN + "FeatureOfInterest"));
		
		Resource sensorOutput = sensorObservationModel.createResource(super.result);
		Statement observationResultTypeStmt = sensorObservationModel.createStatement(sensorOutput, sensorObservationModel.createProperty(RDF, "type"), sensorObservationModel.createResource(SSN + "SensorOutput"));
		Statement observationValueStmt = sensorObservationModel.createStatement(sensorOutput, sensorObservationModel.createProperty(SSN, "hasValue"), sensorObservationModel.createResource(super.obsVal));
		
		Resource observationValue = sensorObservationModel.createResource(super.obsVal);
		Statement observationValueTypeStmt = sensorObservationModel.createStatement(observationValue, sensorObservationModel.createProperty(RDF, "type"), sensorObservationModel.createResource(SSN + "ObservationValue"));
		Statement observationValueValueStmt = sensorObservationModel.createStatement(observationValue, sensorObservationModel.createProperty(SSN, "hasValue"), sensorObservationModel.createTypedLiteral(super.value));
		Statement observationValueSatelliteStmt = sensorObservationModel.createStatement(observationValue, sensorObservationModel.createProperty(NS, "Satellites"), sensorObservationModel.createTypedLiteral(this.satellites));
		
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
		sensorObservationModel.add(observationValueSatelliteStmt);
		
		return sensorObservationModel;
	}

}
