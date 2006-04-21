package org.apache.sandesha2.wsrm;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.sandesha2.Sandesha2Constants;
import org.apache.sandesha2.SandeshaException;

import junit.framework.TestCase;

public class AckRequestedTest extends TestCase {

	SOAPFactory factory = OMAbstractFactory.getSOAP11Factory();
	String rmNamespaceValue = Sandesha2Constants.SPEC_2005_02.NS_URI;
	String addressingNamespaceValue = AddressingConstants.Final.WSA_NAMESPACE;
	
    public AckRequestedTest() {
//        super("CreateSequenceResponseTest");

    }

    public void testFromOMElement() throws SandeshaException {
    	
    }

    public void testToSOAPEnvelope()  throws SandeshaException {}
}
