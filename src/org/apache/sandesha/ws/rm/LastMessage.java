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

import javax.xml.soap.SOAPException;

import org.apache.axis.message.MessageElement;
import org.apache.sandesha.Constants;

/**
 * class LastMessage
 * 
 * @author Amila Navarathna
 * @author Jaliya Ekanayaka
 * @author Sudar Nimalan
 */
public class LastMessage extends MessageElement implements IRmElement {

    /**
     * Field lastMsgElement
     */
    private MessageElement lastMsgElement;

    /**
     * Constructor LastMessage
     */
    public LastMessage() {
        lastMsgElement = new MessageElement();

        lastMsgElement.setName("wsrm:LastMessage");
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
    public MessageElement getSoapElement() {
        return lastMsgElement;
    }

    /**
     * Method fromSOAPEnvelope
     * 
     * @param element
     * 
     * @return LastMessage
     */
    public LastMessage fromSOAPEnvelope(MessageElement element) {
        return this;
    }

    /**
     * Method toSOAPEnvelope
     * 
     * @param msgElement
     * 
     * @return MessageElement
     * 
     * @throws SOAPException
     */
    public MessageElement toSOAPEnvelope(MessageElement msgElement)
            throws SOAPException {
        msgElement.addChildElement("LastMessage", Constants.NS_PREFIX_RM);
        return msgElement;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.sandesha.ws.rm.IRmElement#addChildElement(org.apache.axis.message.MessageElement)
     */

    /**
     * Method addChildElement
     * 
     * @param element
     */
    public void addChildElement(MessageElement element) {

        // no child elements in LastMessage element
    }

    /**
     * Method getLastMsgElement
     * 
     * @return MessageElement
     */
    public MessageElement getLastMsgElement() {
        return lastMsgElement;
    }

    /**
     * Method setLastMsgElement
     * 
     * @param element
     */
    public void setLastMsgElement(MessageElement element) {
        lastMsgElement = element;
    }
}