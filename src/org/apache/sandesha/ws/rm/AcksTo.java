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
import org.apache.axis.message.addressing.Address;
import org.apache.sandesha.Constants;

import javax.xml.soap.SOAPException;
import java.util.Iterator;

public class AcksTo implements IRmElement {
    private Address address;
    private MessageElement acksToElement;

    /**
     * Constructor Nack
     */
    public AcksTo() {
    }

    public AcksTo(Address address) {
        acksToElement = new MessageElement();
        acksToElement.setName(Constants.WSRM.NS_PREFIX_RM + Constants.COLON + Constants.WSRM.ACKS_TO);
        this.address = address;
    }

    /**
     * Method fromSOAPEnvelope
     *
     * @param element
     * @return Nack
     */
    public AcksTo fromSOAPEnvelope(MessageElement element) throws SOAPException {

        Iterator iterator = element.getChildElements();
        MessageElement childElement;
        try {
            while (iterator.hasNext()) {
                childElement = (MessageElement) iterator.next();
                if (childElement.getName().equals(org.apache.axis.message.addressing.Constants.NS_PREFIX_ADDRESSING +
                        Constants.COLON +
                        org.apache.axis.message.addressing.Constants.ADDRESS)) {
                    String uri = childElement.getFirstChild().getFirstChild().toString();
                    address = new Address(uri);
                }
                if (childElement.getName().equals(org.apache.axis.message.addressing.Constants.ADDRESS)) {
                    String uri = childElement.getFirstChild().getNodeValue();
                    address = new Address(uri);
                }
            }
        } catch (Exception e) {
            throw new SOAPException(e);
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
        MessageElement messageElement = new MessageElement("", Constants.WSRM.NS_PREFIX_RM,
                Constants.WSRM.NS_URI_RM);
        messageElement.setName(Constants.WSRM.ACKS_TO);
        address.append(messageElement);
        msgElement.addChildElement(messageElement);
        return msgElement;
    }

    public MessageElement getSoapElement() throws SOAPException {
        address.append(acksToElement);
        return acksToElement;
    }

    /**
     * Method addChildElement
     *
     * @param element
     */
    public void addChildElement(MessageElement element) {
        // TODO no child elements ?
    }

    /**
     * get the address
     *
     * @return
     */
    public Address getAddress() {
        return address;
    }

    /**
     * set the address
     *
     * @param address
     */
    public void setAddress(Address address) {
        this.address = address;
    }


}
