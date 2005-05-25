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
import org.apache.sandesha.Constants;

import javax.xml.soap.SOAPException;

public class FaultCode extends MessageElement implements IRmElement {

    /**
     * Field lastMsgElement
     */
    private MessageElement faultCode;

    /**
     * Constructor LastMessage
     */
    public FaultCode() {
        faultCode = new MessageElement();
        faultCode.setName(Constants.WSRM.NS_PREFIX_RM + Constants.COLON + Constants.WSRM.FAULT_CODE);
    }

    /**
     * Method getSoapElement
     *
     * @return MessageElement
     */
    public MessageElement getSoapElement() {
        return faultCode;
    }

    /**
     * Method fromSOAPEnvelope
     *
     * @param element
     * @return LastMessage
     */
    public FaultCode fromSOAPEnvelope(MessageElement element) {
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
        msgElement.addChildElement(Constants.WSRM.FAULT_CODE, Constants.WSRM.NS_PREFIX_RM);
        return msgElement;
    }

    /**
     * Method addChildElement
     *
     * @param element
     */
    public void addChildElement(MessageElement element) {

        // no child elements in LastMessage element
    }


    public MessageElement getFaultCode() {
        return faultCode;
    }

    public void setFaultCode(MessageElement faultCode) {
        this.faultCode = faultCode;
    }
}
