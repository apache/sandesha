package org.apache.sandesha2.wsrm;

import org.apache.sandesha2.SandeshaTestCase;
import org.apache.sandesha2.Sandesha2Constants;
import org.apache.ws.commons.soap.SOAPEnvelope;
import org.apache.ws.commons.soap.SOAPFactory;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.ws.commons.om.OMAbstractFactory;
import org.apache.ws.commons.om.OMElement;

import javax.xml.namespace.QName;

/**
 * Created by IntelliJ IDEA.
 * User: sanka
 * Date: Oct 7, 2005
 * Time: 2:43:50 AM
 * To change this template use File | Settings | File Templates.
 */
public class CreateSequenceResponseTest extends SandeshaTestCase {

	SOAPFactory factory = OMAbstractFactory.getSOAP11Factory();
	
    public CreateSequenceResponseTest() {
        super("CreateSequenceResponseTest");

    }

    public void testFromOMElement() {
        CreateSequenceResponse res = new CreateSequenceResponse(factory);
        SOAPEnvelope env = getSOAPEnvelope("", "CreateSequenceResponse.xml");
        res.fromOMElement(env.getBody());

        Identifier identifier = res.getIdentifier();
        assertEquals("uuid:88754b00-161a-11da-b6d6-8198de3c47c5", identifier.getIdentifier());

        Accept accept = res.getAccept();
        AcksTo  acksTo = accept.getAcksTo();
        Address address = acksTo.getAddress();
        assertEquals("http://localhost:8070/axis/services/TestService", address.getEpr().getAddress());

    }

    public void testToSOAPEnvelope() {
        CreateSequenceResponse res = new CreateSequenceResponse(factory);

        Identifier identifier = new Identifier(factory);
        identifier.setIndentifer("uuid:88754b00-161a-11da-b6d6-8198de3c47c5");
        res.setIdentifier(identifier);

        Accept accept = new Accept(factory);
        AcksTo acksTo = new AcksTo(factory);
        Address address = new Address(factory);
        address.setEpr(new EndpointReference("http://localhost:8070/axis/services/TestService"));
        acksTo.setAddress(address);
        accept.setAcksTo(acksTo);
        res.setAccept(accept);

        SOAPEnvelope env = getEmptySOAPEnvelope();
        res.toSOAPEnvelope(env);

        OMElement createSeqResponsePart = env.getBody().getFirstChildWithName(
                new QName(Sandesha2Constants.WSRM.NS_URI_RM, Sandesha2Constants.WSRM.CREATE_SEQUENCE_RESPONSE));
        OMElement identifierPart = createSeqResponsePart.getFirstChildWithName(
                new QName(Sandesha2Constants.WSRM.NS_URI_RM, Sandesha2Constants.WSRM.IDENTIFIER));
        assertEquals("uuid:88754b00-161a-11da-b6d6-8198de3c47c5", identifierPart.getText());

        OMElement acceptPart = createSeqResponsePart.getFirstChildWithName(
                new QName(Sandesha2Constants.WSRM.NS_URI_RM, Sandesha2Constants.WSRM.ACCEPT));
        OMElement acksToPart = acceptPart.getFirstChildWithName(
                new QName(Sandesha2Constants.WSRM.NS_URI_RM, Sandesha2Constants.WSRM.ACKS_TO));
        OMElement addressPart = acksToPart.getFirstChildWithName(new QName(
				Sandesha2Constants.WSA.NS_URI_ADDRESSING, Sandesha2Constants.WSA.ADDRESS));
        assertEquals("http://localhost:8070/axis/services/TestService", addressPart.getText());
    }
}
