package org.apache.sandesha2.wsrm;

import org.apache.sandesha2.SandeshaTestCase;
import org.apache.sandesha2.Sandesha2Constants;
import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.axis2.soap.SOAPFactory;
import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMElement;

import javax.xml.namespace.QName;

/**
 * Created by IntelliJ IDEA.
 * User: sanka
 * Date: Oct 7, 2005
 * Time: 3:36:59 AM
 * To change this template use File | Settings | File Templates.
 */
public class TerminateSequenceTest extends SandeshaTestCase {

	SOAPFactory factory = OMAbstractFactory.getSOAP11Factory();
	
    public TerminateSequenceTest() {
        super("TerminateSequenceTest");
    }

    public void testFromOMElement() {
        TerminateSequence terminateSequence =  new TerminateSequence(factory);
        SOAPEnvelope env = getSOAPEnvelope("", "TerminateSequence.xml");
        terminateSequence.fromOMElement(env.getBody());

        Identifier identifier = terminateSequence.getIdentifier();
        assertEquals("uuid:59b0c910-1625-11da-bdfc-b09ed76a1f06", identifier.getIdentifier());
    }

    public void testToSOAPEnvelope() {
        TerminateSequence terminateSequence = new TerminateSequence(factory);
        Identifier identifier = new Identifier(factory);
        identifier.setIndentifer("uuid:59b0c910-1625-11da-bdfc-b09ed76a1f06");
        terminateSequence.setIdentifier(identifier);

        SOAPEnvelope env = getEmptySOAPEnvelope();
        terminateSequence.toSOAPEnvelope(env);

        OMElement terminateSeqPart = env.getBody().getFirstChildWithName(
                new QName(Sandesha2Constants.WSRM.NS_URI_RM, Sandesha2Constants.WSRM.TERMINATE_SEQUENCE));
        OMElement identifierPart = terminateSeqPart.getFirstChildWithName(
                new QName(Sandesha2Constants.WSRM.NS_URI_RM, Sandesha2Constants.WSRM.IDENTIFIER));
        assertEquals("uuid:59b0c910-1625-11da-bdfc-b09ed76a1f06", identifierPart.getText());
    }
}
