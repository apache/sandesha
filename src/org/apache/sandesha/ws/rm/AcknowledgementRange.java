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

import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;

import org.apache.axis.message.MessageElement;
import org.apache.sandesha.Constants;

/**
 * class AcknowledgementRange
 * 
 * @author Amila Navarathna
 * @author Jaliya Ekanayaka
 * @author Sudar Nimalan
 */
public class AcknowledgementRange implements IRmElement {

    /**
     * Field ackRangeElement
     */
    private MessageElement ackRangeElement;

    /**
     * Field minValue
     */
    private long minValue;

    /**
     * Field maxValue
     */
    private long maxValue;

    /**
     * Constructor AcknowledgementRange
     */
    public AcknowledgementRange() {
        ackRangeElement = new MessageElement();
        ackRangeElement.setName("wsrm:AcknowledgementRange");
    }

    /**
     * Method setMaxValue
     * 
     * @param max
     */
    public void setMaxValue(long max) {
        maxValue = max;
    }

    /**
     * Method setMinValue
     * 
     * @param min
     */
    public void setMinValue(long min) {
        minValue = min;
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

        ackRangeElement.setAttribute("Upper", new Long(maxValue).toString());
        ackRangeElement.setAttribute("Lower", new Long(minValue).toString());

        return ackRangeElement;
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

        SOAPElement ackRange = msgElement.addChildElement(
                "AcknowledgementRange", Constants.NS_PREFIX_RM);

        ackRange.setAttribute("Upper", new Long(maxValue).toString());
        ackRange.setAttribute("Lower", new Long(minValue).toString());

        return msgElement;
    }

    /**
     * Method fromSOAPEnvelope
     * 
     * @param element
     * 
     * @return AcknowledgementRange
     */
    public AcknowledgementRange fromSOAPEnvelope(MessageElement element) {

        minValue = (new Long(element.getAttribute("Lower").trim())).longValue();
        maxValue = (new Long(element.getAttribute("Upper").trim())).longValue();

        return this;
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

        // TODO no child elements
    }

    /**
     * Method getMaxValue
     * 
     * @return long
     */
    public long getMaxValue() {
        return maxValue;
    }

    /**
     * Method getMinValue
     * 
     * @return long
     */
    public long getMinValue() {
        return minValue;
    }
}