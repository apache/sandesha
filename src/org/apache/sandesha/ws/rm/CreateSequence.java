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
import org.apache.axis.message.SOAPBodyElement;
import org.apache.axis.message.SOAPEnvelope;
import org.apache.sandesha.Constants;

import javax.xml.soap.Name;
import javax.xml.soap.SOAPException;
import java.util.Iterator;

/**
 * class CreateSequence
 *
 * @author Amila Navarathna
 * @author Jaliya Ekanayaka
 * @author Sudar Nimalan
 */
public class CreateSequence implements IRmElement {

    /**
     * Field createSequence
     */
    private MessageElement createSequence;

    private SequenceOffer offer;

    private AcksTo acksTo;

    /**
     * Constructor CreateSequence
     */
    public CreateSequence() {
        createSequence = new MessageElement();
        createSequence.setName(
                Constants.WSRM.NS_PREFIX_RM + Constants.COLON + Constants.WSRM.CREATE_SEQUENCE);
    }


    /**
     * Method getSoapElement
     *
     * @return MessageElement
     */
    public MessageElement getSoapElement() throws SOAPException {
        createSequence.addChildElement(offer.getSoapElement());
        createSequence.addChildElement(acksTo.getSoapElement());
        return createSequence;
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
        if (env.getBody() == null) {
            env.addBody();
        }

        Name name = env.createName("", Constants.WSRM.NS_PREFIX_RM, Constants.WSRM.NS_URI_RM);
        SOAPBodyElement bodyElement = (SOAPBodyElement) env.getBody().addBodyElement(name);

        bodyElement.setName(Constants.WSRM.CREATE_SEQUENCE);

        if (offer != null)
            offer.toSOAPEnvelope(bodyElement);
        if (acksTo != null)
            acksTo.toSOAPEnvelope(bodyElement);

        return env;
    }

    /**
     * Method fromSOAPEnveploe
     *
     * @param bodyElement
     * @return CreateSequence
     */
    public CreateSequence fromSOAPEnveploe(SOAPBodyElement bodyElement) throws Exception {

        Iterator iterator = bodyElement.getChildElements();
        MessageElement childElement;
        while (iterator.hasNext()) {

            //TODO  add offer processing code here
            //TEST OFFER
            childElement = (MessageElement) iterator.next();
            if (childElement.getName().equals(
                    Constants.WSRM.NS_PREFIX_RM + Constants.COLON + Constants.WSRM.SEQUENCE_OFFER)) {
                offer = new SequenceOffer();
                offer.fromSOAPEnvelope(childElement);
            } else if (childElement.getName().equals(Constants.WSRM.SEQUENCE_OFFER)) {
                offer = new SequenceOffer();
                offer.fromSOAPEnvelope(childElement);
            } else if (childElement.getName().equals(
                    Constants.WSRM.NS_PREFIX_RM + Constants.COLON + Constants.WSRM.ACKS_TO)) {
                acksTo = new AcksTo();
                acksTo.fromSOAPEnvelope(childElement);
            } else if (childElement.getName().equals(Constants.WSRM.ACKS_TO)) {
                acksTo = new AcksTo();
                acksTo.fromSOAPEnvelope(childElement);
            }

        }
        return this;
    }

    /**
     * Method addChildElement
     *
     * @param element
     */
    public void addChildElement(MessageElement element) {
    }


    public SequenceOffer getOffer() {
        return offer;
    }


    public void setOffer(SequenceOffer offer) {
        this.offer = offer;
    }

    public AcksTo getAcksTo() {
        return acksTo;
    }

    public void setAcksTo(AcksTo acksTo) {
        this.acksTo = acksTo;
    }
}