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

/**
 * class Nack
 * 
 * @author Amila Navarathna
 * @author Jaliya Ekanayaka
 * @author Sudar Nimalan
 */
public class Nack implements IRmElement {

    /**
     * Field notAckNum
     */
    private long notAckNum;

    /**
     * Field nackElement
     */
    private MessageElement nackElement;

    /**
     * Constructor Nack
     */
    public Nack() {

        nackElement = new MessageElement();

        nackElement.setName("wsrm:Nack");
    }

    /*
     * (non-Javadoc)
     * @see org.apache.sandesha.ws.rm.IRmElement#getSoapElement()
     */

    /**
     * Method getSoapElement
     * 
     * @return 
     * @throws SOAPException 
     */
    public MessageElement getSoapElement() throws SOAPException {

        nackElement.addTextNode(new Long(notAckNum).toString());

        return nackElement;
    }

    /**
     * Method fromSOAPEnvelope
     * 
     * @param element 
     * @return 
     */
    public Nack fromSOAPEnvelope(MessageElement element) {

        notAckNum = (new Long(element.getFirstChild().toString())).longValue();

        return this;
    }

    /**
     * Method toSOAPEnvelope
     * 
     * @param msgElement 
     * @return 
     * @throws SOAPException 
     */
    public MessageElement toSOAPEnvelope(MessageElement msgElement)
            throws SOAPException {

        msgElement.addChildElement("Nack", Constants.NS_PREFIX_RM).addTextNode((new Long(notAckNum)).toString());

        return msgElement;
    }

    /*
     * (non-Javadoc)
     * @see org.apache.sandesha.ws.rm.IRmElement#addChildElement(org.apache.axis.message.MessageElement)
     */

    /**
     * Method addChildElement
     * 
     * @param element 
     */
    public void addChildElement(MessageElement element) {

        // TODO no child elements ?
    }

    /**
     * @param notAckNo 
     */
    public void setNotAckNum(long notAckNo) {
        notAckNum = notAckNo;
    }
}
