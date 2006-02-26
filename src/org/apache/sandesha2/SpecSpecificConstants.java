package org.apache.sandesha2;

public class SpecSpecificConstants {

	private static String unknowsSpecErrorMessage = "Unknown specification version";
	
	public static String getRMNamespaceValue (String specVersion) throws SandeshaException {
		if (Sandesha2Constants.SPEC_VERSIONS.WSRM.equals(specVersion)) 
			return Sandesha2Constants.SPEC_2005_02.NS_URI;
		else if (Sandesha2Constants.SPEC_VERSIONS.WSRX.equals(specVersion)) 
			return Sandesha2Constants.SPEC_2005_10.NS_URI;
		else 
			throw new SandeshaException (unknowsSpecErrorMessage);
	}
	
	public static String getCreateSequenceAction (String specVersion) throws SandeshaException {
		if (Sandesha2Constants.SPEC_VERSIONS.WSRM.equals(specVersion)) 
			return Sandesha2Constants.SPEC_2005_02.Actions.ACTION_CREATE_SEQUENCE;
		else if (Sandesha2Constants.SPEC_VERSIONS.WSRX.equals(specVersion)) 
			return Sandesha2Constants.SPEC_2005_10.Actions.ACTION_CREATE_SEQUENCE;
		else 
			throw new SandeshaException (unknowsSpecErrorMessage);
	}
	
	public static String getCreateSequenceResponseAction (String specVersion) throws SandeshaException {
		if (Sandesha2Constants.SPEC_VERSIONS.WSRM.equals(specVersion)) 
			return Sandesha2Constants.SPEC_2005_02.Actions.ACTION_CREATE_SEQUENCE_RESPONSE;
		else if (Sandesha2Constants.SPEC_VERSIONS.WSRX.equals(specVersion)) 
			return Sandesha2Constants.SPEC_2005_10.Actions.ACTION_CREATE_SEQUENCE_RESPONSE;
		else 
			throw new SandeshaException (unknowsSpecErrorMessage);
	}
	
	public static String getTerminateSequenceAction (String specVersion) throws SandeshaException {
		if (Sandesha2Constants.SPEC_VERSIONS.WSRM.equals(specVersion)) 
			return Sandesha2Constants.SPEC_2005_02.Actions.ACTION_TERMINATE_SEQUENCE;
		else if (Sandesha2Constants.SPEC_VERSIONS.WSRX.equals(specVersion)) 
			return Sandesha2Constants.SPEC_2005_10.Actions.ACTION_TERMINATE_SEQUENCE;
		else 
			throw new SandeshaException (unknowsSpecErrorMessage);
	}
	
	public static String getSequenceAcknowledgementAction (String specVersion) throws SandeshaException {
		if (Sandesha2Constants.SPEC_VERSIONS.WSRM.equals(specVersion)) 
			return Sandesha2Constants.SPEC_2005_02.Actions.ACTION_SEQUENCE_ACKNOWLEDGEMENT;
		else if (Sandesha2Constants.SPEC_VERSIONS.WSRX.equals(specVersion)) 
			return Sandesha2Constants.SPEC_2005_10.Actions.ACTION_SEQUENCE_ACKNOWLEDGEMENT;
		else 
			throw new SandeshaException (unknowsSpecErrorMessage);
	}
	
	public static String getCreateSequenceSOAPAction (String specVersion) throws SandeshaException {
		if (Sandesha2Constants.SPEC_VERSIONS.WSRM.equals(specVersion)) 
			return Sandesha2Constants.SPEC_2005_02.Actions.SOAP_ACTION_CREATE_SEQUENCE;
		else if (Sandesha2Constants.SPEC_VERSIONS.WSRX.equals(specVersion)) 
			return Sandesha2Constants.SPEC_2005_10.Actions.SOAP_ACTION_CREATE_SEQUENCE;
		else 
			throw new SandeshaException (unknowsSpecErrorMessage);
	}
	
	public static String getCreateSequenceResponseSOAPAction (String specVersion) throws SandeshaException {
		if (Sandesha2Constants.SPEC_VERSIONS.WSRM.equals(specVersion)) 
			return Sandesha2Constants.SPEC_2005_02.Actions.SOAP_ACTION_CREATE_SEQUENCE_RESPONSE;
		else if (Sandesha2Constants.SPEC_VERSIONS.WSRX.equals(specVersion)) 
			return Sandesha2Constants.SPEC_2005_10.Actions.SOAP_ACTION_CREATE_SEQUENCE_RESPONSE;
		else 
			throw new SandeshaException (unknowsSpecErrorMessage);
	}
	
	public static String getTerminateSequenceSOAPAction (String specVersion) throws SandeshaException {
		if (Sandesha2Constants.SPEC_VERSIONS.WSRM.equals(specVersion)) 
			return Sandesha2Constants.SPEC_2005_02.Actions.SOAP_ACTION_TERMINATE_SEQUENCE;
		else if (Sandesha2Constants.SPEC_VERSIONS.WSRX.equals(specVersion)) 
			return Sandesha2Constants.SPEC_2005_10.Actions.SOAP_ACTION_TERMINATE_SEQUENCE;
		else 
			throw new SandeshaException (unknowsSpecErrorMessage);
	}
	
	public static String getSequenceAcknowledgementSOAPAction (String specVersion) throws SandeshaException {
		if (Sandesha2Constants.SPEC_VERSIONS.WSRM.equals(specVersion)) 
			return Sandesha2Constants.SPEC_2005_02.Actions.SOAP_ACTION_SEQUENCE_ACKNOWLEDGEMENT;
		else if (Sandesha2Constants.SPEC_VERSIONS.WSRX.equals(specVersion)) 
			return Sandesha2Constants.SPEC_2005_10.Actions.SOAP_ACTION_SEQUENCE_ACKNOWLEDGEMENT;
		else 
			throw new SandeshaException (unknowsSpecErrorMessage);
	}
	
}
