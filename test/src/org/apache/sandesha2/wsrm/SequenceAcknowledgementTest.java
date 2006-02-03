package org.apache.sandesha2.wsrm;

import org.apache.sandesha2.SandeshaTestCase;
import org.apache.sandesha2.Sandesha2Constants;
import org.apache.ws.commons.soap.SOAPEnvelope;
import org.apache.ws.commons.soap.SOAPFactory;
import org.apache.ws.commons.om.OMAbstractFactory;
import org.apache.ws.commons.om.OMElement;

import javax.xml.namespace.QName;
import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: sanka
 * Date: Oct 7, 2005
 * Time: 3:52:12 AM
 * To change this template use File | Settings | File Templates.
 */
public class SequenceAcknowledgementTest extends SandeshaTestCase {

	SOAPFactory factory = OMAbstractFactory.getSOAP11Factory();
	
    public SequenceAcknowledgementTest() {
        super("SequenceAcknowledgementTest");
    }

    public void testFromOMElement() {
        SequenceAcknowledgement sequenceAck = new SequenceAcknowledgement(factory);
        SOAPEnvelope env = getSOAPEnvelope("", "SequenceAcknowledgement.xml");
        sequenceAck.fromOMElement(env.getHeader());

        Identifier identifier = sequenceAck.getIdentifier();
        assertEquals("uuid:897ee740-1624-11da-a28e-b3b9c4e71445", identifier.getIdentifier());

        Iterator iterator = sequenceAck.getAcknowledgementRanges().iterator();
        while (iterator.hasNext()) {
            AcknowledgementRange ackRange = (AcknowledgementRange) iterator.next();
            if (ackRange.getLowerValue() == 1){
                assertEquals(2, ackRange.getUpperValue());

            } else if (ackRange.getLowerValue() == 4) {
                assertEquals(6, ackRange.getUpperValue());

            } else if (ackRange.getLowerValue() == 8) {
                assertEquals(10, ackRange.getUpperValue());
            }
        }

        iterator = sequenceAck.getNackList().iterator();
        while (iterator.hasNext()) {
            Nack nack = (Nack) iterator.next();
            if (nack.getNackNumber() == 3) {

            } else if (nack.getNackNumber() == 7) {

            } else {
                fail("invalid nack : " +  nack.getNackNumber());
            }
        }


    }

    public void testToOMElement() {
        SequenceAcknowledgement seqAck = new SequenceAcknowledgement(factory);
        Identifier identifier = new Identifier(factory);
        identifier.setIndentifer("uuid:897ee740-1624-11da-a28e-b3b9c4e71445");
        seqAck.setIdentifier(identifier);

        SOAPEnvelope env = getEmptySOAPEnvelope();
        seqAck.toSOAPEnvelope(env);

        OMElement sequenceAckPart = env.getHeader().getFirstChildWithName(
                new QName(Sandesha2Constants.WSRM.NS_URI_RM, Sandesha2Constants.WSRM.SEQUENCE_ACK));
        OMElement identifierPart = sequenceAckPart.getFirstChildWithName(
                new QName(Sandesha2Constants.WSRM.NS_URI_RM, Sandesha2Constants.WSRM.IDENTIFIER));
        assertEquals("uuid:897ee740-1624-11da-a28e-b3b9c4e71445", identifierPart.getText());




    }
}
