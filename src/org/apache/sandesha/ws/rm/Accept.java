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
import org.apache.axis.message.SOAPHeaderElement;
import org.apache.sandesha.Constants;

import javax.xml.soap.SOAPException;
import java.util.Iterator;

/**
 * class LastMessage
 *
 * @author Amila Navarathna
 * @author Jaliya Ekanayaka
 * @author Sudar Nimalan
 */
public class Accept extends MessageElement implements IRmElement {

    /**
     * Field lastMsgElement
     */
    private MessageElement acceptElement;

    private AcksTo acksTo;

    /**
     * Constructor LastMessage
     */
    public Accept() {
        acceptElement = new MessageElement();
        acceptElement.setName(Constants.WSRM.NS_PREFIX_RM + Constants.COLON + Constants.WSRM.ACCEPT);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.sandesha.ws.rm.IRmElement#getSoapElement()
     */

    /**
     * Method getSoapElement
     *
     * @return MessageElement
     */
    public MessageElement getSoapElement() throws SOAPException {
        acceptElement.addChildElement(acksTo.getSoapElement());
        return acceptElement;
    }

    public Accept fromSOAPEnvelope(MessageElement bodyElement) throws SOAPException {

        Iterator iterator = bodyElement.getChildElements();
        MessageElement childElement;

        while (iterator.hasNext()) {
            childElement = (MessageElement) iterator.next();

            if (childElement.getName().equals(Constants.WSRM.NS_PREFIX_RM + Constants.COLON + Constants.WSRM.ACKS_TO)) {
                acksTo = new AcksTo();
                acksTo.fromSOAPEnvelope(childElement);
            }

            if (childElement.getName().equals(Constants.WSRM.ACKS_TO)) {
                acksTo = new AcksTo();
                acksTo.fromSOAPEnvelope(childElement);
            }

        }

        return this;
    }

    /**
     * Method toSOAPEnvelope
     *
     * @param msgElement
     * @return MessageElement
     * @throws SOAPException
     */
    public MessageElement toSOAPEnvelope(MessageElement msgElement) throws SOAPException {
        MessageElement messageElement = new MessageElement("", Constants.WSRM.NS_PREFIX_RM, Constants.WSRM.NS_URI_RM);
        messageElement.setName(Constants.WSRM.ACCEPT);
        acksTo.toSOAPEnvelope(messageElement);
        msgElement.addChildElement(messageElement);
        return msgElement;
    }


    public void addChildElement(MessageElement element) {

    }

    /**
     * Method getLastMsgElement
     *
     * @return MessageElement
     */
    public MessageElement getAcceptElement() {
        return acceptElement;
    }

    /**
     * Method setLastMsgElement
     *
     * @param element
     */
    public void setAcceptElement(MessageElement element) {
        acceptElement = element;
    }

    public void setAcksTo(AcksTo acksTo) {
        this.acksTo = acksTo;
    }

    public AcksTo getAcksTo() {
        return this.acksTo;
    }


}