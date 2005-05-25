/*
 * Copyright  1999-2004 The Apache Software Foundation.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.apache.sandesha.ws.rm;

import org.apache.axis.message.MessageElement;
import org.apache.axis.message.SOAPEnvelope;
import org.apache.axis.message.SOAPHeaderElement;
import org.apache.sandesha.Constants;

import javax.xml.soap.Name;
import javax.xml.soap.SOAPException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * class SequenceAcknowledgement
 *
 * @author Amila Navarathna
 * @author Jaliya Ekanayaka
 * @author Sudar Nimalan
 */
public class SequenceAcknowledgement extends MessageElement implements IRmElement {

    /**
     * Field seqAck
     */
    private MessageElement seqAck; // this element

    /**
     * Field ackRanges
     */
    private List ackRanges;

    /**
     * Field nackList
     */
    private List nackList;

    /**
     * Field identifier
     */
    private Identifier identifier;

    /**
     * Constructor SequenceAcknowledgement
     */
    public SequenceAcknowledgement() {

        ackRanges = new LinkedList();
        nackList = new LinkedList();
        seqAck = new MessageElement();

        seqAck.setName(Constants.WSRM.NS_PREFIX_RM + Constants.COLON + Constants.WSRM.SEQUENCE_ACK);
    }

    /**
     * Method getSoapElement
     *
     * @return MessageElement
     * @throws SOAPException
     */
    public MessageElement getSoapElement() throws SOAPException {

        Iterator ite = ackRanges.iterator();

        while (ite.hasNext()) {
            MessageElement element = (MessageElement) ite.next();

            seqAck.addChildElement(element);
        }

        ite = nackList.iterator();

        while (ite.hasNext()) {
            MessageElement element = (MessageElement) ite.next();

            seqAck.addChildElement(element);
        }

        seqAck.addChildElement(identifier.getSoapElement());

        return seqAck;
    }

    /**
     * Method toSoapEnvelop
     *
     * @param envelope
     * @return SOAPEnvelope
     * @throws SOAPException
     */
    public SOAPEnvelope toSoapEnvelop(SOAPEnvelope envelope) throws SOAPException {

        SOAPEnvelope env = envelope;

        if (env.getHeader() == null) {
            env.addHeader();
        }

        Name name = env.createName("", Constants.WSRM.NS_PREFIX_RM, Constants.WSRM.NS_URI_RM);
        SOAPHeaderElement headerElement = (SOAPHeaderElement) env.getHeader().addHeaderElement(
                name);

        headerElement.setActor(null);
        headerElement.setName(Constants.WSRM.SEQUENCE_ACK);
        headerElement.setMustUnderstand(true);

        Iterator iterator = ackRanges.iterator();

        while (iterator.hasNext()) {

            AcknowledgementRange ackRange = (AcknowledgementRange) iterator.next();
            ackRange.toSOAPEnvelope(headerElement);
        }

        iterator = nackList.iterator();

        while (iterator.hasNext()) {
            Nack nack = (Nack) iterator.next();

            nack.toSOAPEnvelope(headerElement);
        }

        if (identifier != null) {
            identifier.toSOAPEnvelope(headerElement);
        }

        return env;
    }

    /**
     * Method fromSOAPEnveploe
     *
     * @param headerElement
     * @return SequenceAcknowledgement
     */
    public SequenceAcknowledgement fromSOAPEnveploe(SOAPHeaderElement headerElement) {

        Iterator iterator = headerElement.getChildElements();
        MessageElement childElement;

        while (iterator.hasNext()) {

            childElement = (MessageElement) iterator.next();

            if (childElement.getName().equals(
                    Constants.WSRM.NS_PREFIX_RM + Constants.COLON + Constants.WSRM.IDENTIFIER)) {
                identifier = new Identifier();

                identifier.fromSOAPEnvelope(childElement);
            }

            if (childElement.getName().equals(Constants.WSRM.IDENTIFIER)) {
                identifier = new Identifier();
                identifier.fromSOAPEnvelope(childElement);
            }

            if (childElement.getName().equals(
                    Constants.WSRM.NS_PREFIX_RM + Constants.COLON + Constants.WSRM.ACK_RANGE)) {
                AcknowledgementRange ackRange = new AcknowledgementRange();

                ackRange.fromSOAPEnvelope(childElement);
                ackRanges.add(ackRange);
            }

            if (childElement.getName().equals(Constants.WSRM.ACK_RANGE)) {
                AcknowledgementRange ackRange = new AcknowledgementRange();
                ackRange.fromSOAPEnvelope(childElement);
                ackRanges.add(ackRange);
            }

            if (childElement.getName().equals(
                    Constants.WSRM.NS_PREFIX_RM + Constants.COLON + Constants.WSRM.NACK)) {
                Nack nack = new Nack();
                nack.fromSOAPEnvelope(childElement);
            }

            if (childElement.getName().equals(Constants.WSRM.NACK)) {
                Nack nack = new Nack();
                nack.fromSOAPEnvelope(childElement);
            }
        }

        return this;
    }

    /**
     * Method addChildElement
     *
     * @param element
     * @throws SOAPException
     */
    public void addChildElement(MessageElement element) throws SOAPException {
        seqAck.addChildElement(element);
    }

    /**
     * Method getAckRanges
     *
     * @return List
     */
    public List getAckRanges() {
        return ackRanges;
    }

    /**
     * Method getIdentifier
     *
     * @return Identifier
     */
    public Identifier getIdentifier() {
        return identifier;
    }

    /**
     * Method getNackList
     *
     * @return List
     */
    public List getNackList() {
        return nackList;
    }

    /**
     * method addAckRanges
     *
     * @param ackRange
     * @return AcknowledgementRange
     */
    public AcknowledgementRange addAckRanges(AcknowledgementRange ackRange) {

        ackRanges.add(ackRange);

        return ackRange;
    }

    /**
     * Method addNackRanges
     *
     * @param nack
     * @return Nack
     */
    public Nack addNackRanges(Nack nack) {

        nackList.add(nack);

        return nack;
    }

    /**
     * Method setIdentifier
     *
     * @param identifier
     */
    public void setIdentifier(Identifier identifier) {
        this.identifier = identifier;
    }

    /**
     * Method setAckRanges
     *
     * @param list
     */
    public void setAckRanges(List list) {
        ackRanges = list;
    }
}