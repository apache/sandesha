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

import org.apache.axis.AxisFault;
import org.apache.axis.message.MessageElement;
import org.apache.axis.message.SOAPEnvelope;
import org.apache.axis.message.SOAPHeaderElement;
import org.apache.sandesha.Constants;

import javax.xml.soap.Name;
import javax.xml.soap.SOAPException;
import java.util.Iterator;

/**
 * class Sequence
 *
 * @author Amila Navarathna
 * @author Jaliya Ekanayaka
 * @author Sudar Nimalan
 */
public class Sequence extends MessageElement implements IRmElement {

    /**
     * Field seqElement
     */
    private MessageElement seqElement;

    /**
     * Field identifier
     */
    private Identifier identifier;

    /**
     * Field messageNumber
     */
    private MessageNumber messageNumber;

    /**
     * Field lastMessage
     */
    private LastMessage lastMessage;

    /**
     * Constructor Sequence
     */
    public Sequence() {
        seqElement = new MessageElement();
        seqElement.setName(Constants.WSRM.NS_PREFIX_RM + Constants.COLON + Constants.WSRM.SEQUENCE);
    }

    /**
     * Method getSoapElement
     *
     * @return MessageElement
     * @throws SOAPException
     */
    public MessageElement getSoapElement() throws SOAPException {

        seqElement.addChildElement(identifier.getSoapElement());
        seqElement.addChildElement(messageNumber.getSoapElement());
        seqElement.addChildElement(lastMessage.getSoapElement());

        return seqElement;
    }

    /**
     * Method toSoapEnvelop
     *
     * @param envelope
     * @return SOAPEnvelope
     * @throws Exception
     */
    public SOAPEnvelope toSoapEnvelop(SOAPEnvelope envelope) throws Exception {

        SOAPEnvelope env = envelope;

        if (env.getHeader() == null) {
            env.addHeader();
        }
        removeHeaders(envelope);

        Name name = env.createName("", Constants.WSRM.NS_PREFIX_RM, Constants.WSRM.NS_URI_RM);
        SOAPHeaderElement headerElement = (SOAPHeaderElement) env.getHeader().addHeaderElement(name);

        headerElement.setActor(null);
        headerElement.setName(Constants.WSRM.SEQUENCE);
        headerElement.setMustUnderstand(true);


        if (identifier != null) {
            identifier.toSOAPEnvelope(headerElement);
        }

        if (messageNumber != null) {
            messageNumber.toSOAPEnvelope(headerElement);
        }

        if (lastMessage != null) {
            lastMessage.toSOAPEnvelope(headerElement);
        }

        return env;
    }

    /**
     * Method fromSOAPEnveploe
     *
     * @param headerElement
     * @return Sequence
     */
    public Sequence fromSOAPEnveploe(SOAPHeaderElement headerElement) throws AxisFault {

        Iterator iterator = headerElement.getChildElements();
        MessageElement childElement;

        while (iterator.hasNext()) {
            childElement = (MessageElement) iterator.next();

            if (childElement.getName().equals(Constants.WSRM.NS_PREFIX_RM + Constants.COLON + Constants.WSRM.IDENTIFIER)) {
                identifier = new Identifier();
                identifier.fromSOAPEnvelope(childElement);
            }

            if (childElement.getName().equals(Constants.WSRM.IDENTIFIER)) {
                identifier = new Identifier();
                identifier.fromSOAPEnvelope(childElement);
            }

            if (childElement.getName().equals(Constants.WSRM.NS_PREFIX_RM + Constants.COLON + Constants.WSRM.MSG_NUMBER)) {
                messageNumber = new MessageNumber();
                messageNumber.fromSOAPEnvelope(childElement);
            }

            if (childElement.getName().equals(Constants.WSRM.MSG_NUMBER)) {
                messageNumber = new MessageNumber();
                messageNumber.fromSOAPEnvelope(childElement);
            }

            if (childElement.getName().equals(Constants.WSRM.NS_PREFIX_RM + Constants.COLON + Constants.WSRM.LAST_MSG)) {
                lastMessage = new LastMessage();
                lastMessage.fromSOAPEnvelope(childElement);
            }

            if (childElement.getName().equals(Constants.WSRM.LAST_MSG)) {
                lastMessage = new LastMessage();
                lastMessage.fromSOAPEnvelope(childElement);
            }
        }

        return this;
    }

    public void removeHeaders(SOAPEnvelope soapEnvelope) throws Exception {

        Iterator iterator = soapEnvelope.getHeader().getChildElements();
        MessageElement childElement;

        while (iterator.hasNext()) {
            childElement = (MessageElement) iterator.next();

            if (Constants.WSRM.SEQUENCE.equals(childElement.getName()) &&
                    (Constants.WSRM.NS_URI_RM.equals(childElement.getNamespaceURI()))) {
                childElement.detachNode();
                break;
            }
        }
    }

    /**
     * Method addChildElement
     *
     * @param element
     * @throws SOAPException
     */
    public void addChildElement(MessageElement element) throws SOAPException {
        seqElement.addChildElement(element);
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
     * Method getLastMessage
     *
     * @return LastMessage
     */
    public LastMessage getLastMessage() {
        return lastMessage;
    }

    /**
     * Method getMessageNumber
     *
     * @return MessageNumber
     */
    public MessageNumber getMessageNumber() {
        return messageNumber;
    }

    /**
     * Method setIdentifier
     *
     * @param identifer
     */
    public void setIdentifier(Identifier identifer) {
        identifier = identifer;
    }

    /**
     * Method setLastMessage
     *
     * @param message
     */
    public void setLastMessage(LastMessage message) {
        lastMessage = message;
    }

    /**
     * Method setMessageNumber
     *
     * @param number
     */
    public void setMessageNumber(MessageNumber number) {
        messageNumber = number;
    }
}