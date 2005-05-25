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
import org.apache.sandesha.Constants;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;

/**
 * class MessageNumber
 *
 * @author Amila Navarathna
 * @author Jaliya Ekanayaka
 * @author Sudar Nimalan
 */
public class MessageNumber implements IRmElement {

    /**
     * Field messageNumber
     */
    private long messageNumber;

    /**
     * Field messageNoElement
     */
    private MessageElement messageNoElement;

    /**
     * Constructor MessageNumber
     */
    public MessageNumber() {

        messageNoElement = new MessageElement();

        messageNoElement.setName(
                Constants.WSRM.NS_PREFIX_RM + Constants.COLON + Constants.WSRM.MSG_NUMBER);
    }

    /**
     * Method getMessageNumber
     *
     * @return long
     */
    public long getMessageNumber() {
        return messageNumber;
    }

    /**
     * Method getSoapElement
     *
     * @return MessageElement
     * @throws SOAPException
     */
    public MessageElement getSoapElement() throws SOAPException {

        // create the soap element for the message no
        messageNoElement.addTextNode((new Long(messageNumber)).toString());
        return messageNoElement;
    }

    /**
     * Method fromSOAPEnvelope
     *
     * @param element
     * @return MessageNumber
     */
    public MessageNumber fromSOAPEnvelope(MessageElement element) throws AxisFault {

        double tempMsgNo = (new Double(element.getValue())).doubleValue();
        if (tempMsgNo >= Constants.WSRM.MAX_MSG_NO)
            throw new AxisFault(new QName(Constants.FaultCodes.WSRM_FAULT_MSG_NO_ROLLOVER),
                    Constants.FaultMessages.MSG_NO_ROLLOVER, null, null);

        messageNumber = (new Long(element.getValue())).longValue();
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

        msgElement.addChildElement(Constants.WSRM.MSG_NUMBER, Constants.WSRM.NS_PREFIX_RM)
                .addTextNode((new Long(messageNumber)).toString());

        return msgElement;
    }

    /**
     * Method setMessageNumber Set the message no in the soap message element
     * create
     *
     * @param msgNo the message no
     */
    public void setMessageNumber(long msgNo) {
        messageNumber = msgNo;
    }


    /**
     * Method addChildElement
     *
     * @param element
     */
    public void addChildElement(MessageElement element) {
    }
}