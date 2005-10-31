package org.apache.sandesha2.wsrm;

import org.apache.sandesha2.SandeshaTestCase;
import org.apache.sandesha2.Constants;
import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.axis2.om.OMElement;

import javax.xml.namespace.QName;

/**
 * Created by IntelliJ IDEA.
 * User: sanka
 * Date: Oct 7, 2005
 * Time: 4:31:54 AM
 * To change this template use File | Settings | File Templates.
 */
public class SequenceTest extends SandeshaTestCase {

    public SequenceTest() {
        super("SequenceTest");

    }

    public void testFromOMElement() {
        SOAPEnvelope env = getSOAPEnvelope("", "Sequence.xml");
        Sequence sequence = new Sequence();
        sequence.fromOMElement(env.getHeader());

        Identifier identifier = sequence.getIdentifier();
        assertEquals("uuid:879da420-1624-11da-bed9-84d13db13902", identifier.getIdentifier());

        MessageNumber msgNo = sequence.getMessageNumber();
        assertEquals(1, msgNo.getMessageNumber());
    }

    public void testToSOAPEnvelope() {
        Sequence sequence = new Sequence();

        Identifier identifier = new Identifier();
        identifier.setIndentifer("uuid:879da420-1624-11da-bed9-84d13db13902");
        sequence.setIdentifier(identifier);

        MessageNumber msgNo = new MessageNumber();
        msgNo.setMessageNumber(1);
        sequence.setMessageNumber(msgNo);

        SOAPEnvelope envelope = getEmptySOAPEnvelope();
        sequence.toSOAPEnvelope(envelope);

        OMElement sequencePart = envelope.getHeader().getFirstChildWithName(
                new QName(Constants.WSRM.NS_URI_RM, Constants.WSRM.SEQUENCE));
        OMElement identifierPart = sequencePart.getFirstChildWithName(
                new QName(Constants.WSRM.NS_URI_RM, Constants.WSRM.IDENTIFIER));
        assertEquals("uuid:879da420-1624-11da-bed9-84d13db13902", identifierPart.getText());

        OMElement msgNumberPart = sequencePart.getFirstChildWithName(
				new QName (Constants.WSRM.NS_URI_RM,Constants.WSRM.MSG_NUMBER));
        assertEquals(1, Long.parseLong(msgNumberPart.getText()));
    }
}
