package org.apache.sandesha2;

public class SpecSpecificConstants {

	private static String unknownSpecErrorMessage = "Unknown specification version";
	
	public static String getSpecVersionString (String namespaceValue) throws SandeshaException {
		if (Sandesha2Constants.SPEC_2005_02.NS_URI.equals(namespaceValue))
			return Sandesha2Constants.SPEC_VERSIONS.WSRM;
		else if (Sandesha2Constants.SPEC_2005_10.NS_URI.equals(namespaceValue))
			return Sandesha2Constants.SPEC_VERSIONS.WSRX;
		else
			throw new SandeshaException ("Unknows rm namespace value");
	}
	
	public static String getRMNamespaceValue (String specVersion) throws SandeshaException {
		if (Sandesha2Constants.SPEC_VERSIONS.WSRM.equals(specVersion)) 
			return Sandesha2Constants.SPEC_2005_02.NS_URI;
		else if (Sandesha2Constants.SPEC_VERSIONS.WSRX.equals(specVersion)) 
			return Sandesha2Constants.SPEC_2005_10.NS_URI;
		else 
			throw new SandeshaException (unknownSpecErrorMessage);
	}
	
	public static String getCreateSequenceAction (String specVersion) throws SandeshaException {
		if (Sandesha2Constants.SPEC_VERSIONS.WSRM.equals(specVersion)) 
			return Sandesha2Constants.SPEC_2005_02.Actions.ACTION_CREATE_SEQUENCE;
		else if (Sandesha2Constants.SPEC_VERSIONS.WSRX.equals(specVersion)) 
			return Sandesha2Constants.SPEC_2005_10.Actions.ACTION_CREATE_SEQUENCE;
		else 
			throw new SandeshaException (unknownSpecErrorMessage);
	}
	
	public static String getCreateSequenceResponseAction (String specVersion) throws SandeshaException {
		if (Sandesha2Constants.SPEC_VERSIONS.WSRM.equals(specVersion)) 
			return Sandesha2Constants.SPEC_2005_02.Actions.ACTION_CREATE_SEQUENCE_RESPONSE;
		else if (Sandesha2Constants.SPEC_VERSIONS.WSRX.equals(specVersion)) 
			return Sandesha2Constants.SPEC_2005_10.Actions.ACTION_CREATE_SEQUENCE_RESPONSE;
		else 
			throw new SandeshaException (unknownSpecErrorMessage);
	}
	
	public static String getTerminateSequenceAction (String specVersion) throws SandeshaException {
		if (Sandesha2Constants.SPEC_VERSIONS.WSRM.equals(specVersion)) 
			return Sandesha2Constants.SPEC_2005_02.Actions.ACTION_TERMINATE_SEQUENCE;
		else if (Sandesha2Constants.SPEC_VERSIONS.WSRX.equals(specVersion)) 
			return Sandesha2Constants.SPEC_2005_10.Actions.ACTION_TERMINATE_SEQUENCE;
		else 
			throw new SandeshaException (unknownSpecErrorMessage);
	}
	
	public static String getCloseSequenceAction (String specVersion) throws SandeshaException {
		if (Sandesha2Constants.SPEC_VERSIONS.WSRM.equals(specVersion)) 
			throw new SandeshaException ("This rm spec version does not define a sequenceClose action");
		else if (Sandesha2Constants.SPEC_VERSIONS.WSRX.equals(specVersion)) 
			return Sandesha2Constants.SPEC_2005_10.Actions.ACTION_CLOSE_SEQUENCE;
		else 
			throw new SandeshaException (unknownSpecErrorMessage);
	}
	
	public static String getAckRequestAction (String specVersion) throws SandeshaException {
		if (Sandesha2Constants.SPEC_VERSIONS.WSRM.equals(specVersion)) 
			throw new SandeshaException ("this spec version does not define a ackRequest action");
		else if (Sandesha2Constants.SPEC_VERSIONS.WSRX.equals(specVersion)) 
			return Sandesha2Constants.SPEC_2005_10.Actions.ACTION_ACK_REQUEST;
		else 
			throw new SandeshaException (unknownSpecErrorMessage);
	}
	
	public static String getSequenceAcknowledgementAction (String specVersion) throws SandeshaException {
		if (Sandesha2Constants.SPEC_VERSIONS.WSRM.equals(specVersion)) 
			return Sandesha2Constants.SPEC_2005_02.Actions.ACTION_SEQUENCE_ACKNOWLEDGEMENT;
		else if (Sandesha2Constants.SPEC_VERSIONS.WSRX.equals(specVersion)) 
			return Sandesha2Constants.SPEC_2005_10.Actions.ACTION_SEQUENCE_ACKNOWLEDGEMENT;
		else 
			throw new SandeshaException (unknownSpecErrorMessage);
	}
	
	public static String getCreateSequenceSOAPAction (String specVersion) throws SandeshaException {
		if (Sandesha2Constants.SPEC_VERSIONS.WSRM.equals(specVersion)) 
			return Sandesha2Constants.SPEC_2005_02.Actions.SOAP_ACTION_CREATE_SEQUENCE;
		else if (Sandesha2Constants.SPEC_VERSIONS.WSRX.equals(specVersion)) 
			return Sandesha2Constants.SPEC_2005_10.Actions.SOAP_ACTION_CREATE_SEQUENCE;
		else 
			throw new SandeshaException (unknownSpecErrorMessage);
	}
	
	public static String getCreateSequenceResponseSOAPAction (String specVersion) throws SandeshaException {
		if (Sandesha2Constants.SPEC_VERSIONS.WSRM.equals(specVersion)) 
			return Sandesha2Constants.SPEC_2005_02.Actions.SOAP_ACTION_CREATE_SEQUENCE_RESPONSE;
		else if (Sandesha2Constants.SPEC_VERSIONS.WSRX.equals(specVersion)) 
			return Sandesha2Constants.SPEC_2005_10.Actions.SOAP_ACTION_CREATE_SEQUENCE_RESPONSE;
		else 
			throw new SandeshaException (unknownSpecErrorMessage);
	}
	
	public static String getTerminateSequenceSOAPAction (String specVersion) throws SandeshaException {
		if (Sandesha2Constants.SPEC_VERSIONS.WSRM.equals(specVersion)) 
			return Sandesha2Constants.SPEC_2005_02.Actions.SOAP_ACTION_TERMINATE_SEQUENCE;
		else if (Sandesha2Constants.SPEC_VERSIONS.WSRX.equals(specVersion)) 
			return Sandesha2Constants.SPEC_2005_10.Actions.SOAP_ACTION_TERMINATE_SEQUENCE;
		else 
			throw new SandeshaException (unknownSpecErrorMessage);
	}
	
	public static String getAckRequestSOAPAction (String specVersion) throws SandeshaException {
		if (Sandesha2Constants.SPEC_VERSIONS.WSRM.equals(specVersion)) 
			throw new SandeshaException ("this spec version does not define a ackRequest SOAP action");
		else if (Sandesha2Constants.SPEC_VERSIONS.WSRX.equals(specVersion)) 
			return Sandesha2Constants.SPEC_2005_10.Actions.SOAP_ACTION_ACK_REQUEST;
		else 
			throw new SandeshaException (unknownSpecErrorMessage);
	}
	
	public static String getSequenceAcknowledgementSOAPAction (String specVersion) throws SandeshaException {
		if (Sandesha2Constants.SPEC_VERSIONS.WSRM.equals(specVersion)) 
			return Sandesha2Constants.SPEC_2005_02.Actions.SOAP_ACTION_SEQUENCE_ACKNOWLEDGEMENT;
		else if (Sandesha2Constants.SPEC_VERSIONS.WSRX.equals(specVersion)) 
			return Sandesha2Constants.SPEC_2005_10.Actions.SOAP_ACTION_SEQUENCE_ACKNOWLEDGEMENT;
		else 
			throw new SandeshaException (unknownSpecErrorMessage);
	}
	
	public static boolean isTerminateSequenceResponseRequired (String specVersion)  throws SandeshaException {
		if (Sandesha2Constants.SPEC_VERSIONS.WSRM.equals(specVersion)) 
			return false;
		else if (Sandesha2Constants.SPEC_VERSIONS.WSRX.equals(specVersion)) 
			return true;
		else 
			throw new SandeshaException (unknownSpecErrorMessage);
	}
	
	public static boolean isLastMessageIndicatorRequired (String specVersion)  throws SandeshaException {
		if (Sandesha2Constants.SPEC_VERSIONS.WSRM.equals(specVersion)) 
			return true;
		else if (Sandesha2Constants.SPEC_VERSIONS.WSRX.equals(specVersion)) 
			return false;
		else 
			throw new SandeshaException (unknownSpecErrorMessage);
	}
	
	public static boolean isAckFinalAllowed (String specVersion)  throws SandeshaException {
		if (Sandesha2Constants.SPEC_VERSIONS.WSRM.equals(specVersion)) 
			return false;
		else if (Sandesha2Constants.SPEC_VERSIONS.WSRX.equals(specVersion)) 
			return true;
		else 
			throw new SandeshaException (unknownSpecErrorMessage);
	}
	
	public static boolean isAckNoneAllowed (String specVersion)  throws SandeshaException {
		if (Sandesha2Constants.SPEC_VERSIONS.WSRM.equals(specVersion)) 
			return false;
		else if (Sandesha2Constants.SPEC_VERSIONS.WSRX.equals(specVersion)) 
			return true;
		else 
			throw new SandeshaException (unknownSpecErrorMessage);
	}
	
	public static boolean isSequenceClosingAllowed (String specVersion)  throws SandeshaException {
		if (Sandesha2Constants.SPEC_VERSIONS.WSRM.equals(specVersion)) 
			return false;
		else if (Sandesha2Constants.SPEC_VERSIONS.WSRX.equals(specVersion)) 
			return true;
		else 
			throw new SandeshaException (unknownSpecErrorMessage);
	}
	
	public static String getDefaultSpecVersion () {
		return Sandesha2Constants.SPEC_VERSIONS.WSRM;
	}
}
