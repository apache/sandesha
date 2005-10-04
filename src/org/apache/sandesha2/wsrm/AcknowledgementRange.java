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
package org.apache.sandesha2.wsrm;

import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMAttribute;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMException;
import org.apache.axis2.om.OMFactory;
import org.apache.axis2.om.OMNamespace;
import org.apache.axis2.om.OMNode;
import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.sandesha2.Constants;
import org.apache.sandesha2.SOAPAbstractFactory;

/**
 * @author Saminda
 * @author chamikara
 * @author sanka
 */

public class AcknowledgementRange implements IOMRMElement {
	private OMElement acknowledgementRangeElement;

	private long upperValue;

	private long lowerValue;

	OMNamespace rmNamespace = SOAPAbstractFactory.getSOAPFactory(
			Constants.SOAPVersion.DEFAULT).createOMNamespace(
			Constants.WSRM.NS_URI_RM, Constants.WSRM.NS_PREFIX_RM);

	public AcknowledgementRange() {
		acknowledgementRangeElement = SOAPAbstractFactory.getSOAPFactory(
				Constants.SOAPVersion.DEFAULT).createOMElement(
				Constants.WSRM.ACK_RANGE, rmNamespace);
	}

	public OMElement getOMElement() throws OMException {
		return acknowledgementRangeElement;
	}

	public Object fromOMElement(OMElement ackRangePart) throws OMException {

		if (ackRangePart == null)
			throw new OMException("The passed element is null");

		OMAttribute lowerAttrib = ackRangePart.getAttribute(new QName(
				Constants.WSRM.LOWER));
		OMAttribute upperAttrib = ackRangePart.getAttribute(new QName(
				Constants.WSRM.UPPER));

		if (lowerAttrib == null || upperAttrib == null)
			throw new OMException(
					"Passed element does not contain upper or lower attributes");

		try {
			long lower = Long.parseLong(lowerAttrib.getValue());
			long upper = Long.parseLong(upperAttrib.getValue());
			upperValue = upper;
			lowerValue = lower;
		} catch (Exception ex) {
			throw new OMException(
					"The ack range does not have proper long values for Upper and Lower attributes");
		}

		acknowledgementRangeElement = SOAPAbstractFactory.getSOAPFactory(
				Constants.SOAPVersion.DEFAULT).createOMElement(
				Constants.WSRM.ACK_RANGE, rmNamespace);

		return this;
	}

	public OMElement toOMElement(OMElement sequenceAckElement)
			throws OMException {

		if (sequenceAckElement == null)
			throw new OMException(
					"Cant set Ack Range part since element is null");

		if (upperValue <= 0 || lowerValue <= 0 || lowerValue > upperValue)
			throw new OMException(
					"Cant set Ack Range part since Upper or Lower is not set to the correct value");

		OMAttribute lowerAttrib = SOAPAbstractFactory.getSOAPFactory(
				Constants.SOAPVersion.DEFAULT).createOMAttribute(
				Constants.WSRM.LOWER, null, Long.toString(lowerValue));
		OMAttribute upperAttrib = SOAPAbstractFactory.getSOAPFactory(
				Constants.SOAPVersion.DEFAULT).createOMAttribute(
				Constants.WSRM.UPPER, null, Long.toString(upperValue));

		acknowledgementRangeElement.addAttribute(lowerAttrib);
		acknowledgementRangeElement.addAttribute(upperAttrib);

		sequenceAckElement.addChild(acknowledgementRangeElement);

		acknowledgementRangeElement = SOAPAbstractFactory.getSOAPFactory(
				Constants.SOAPVersion.DEFAULT).createOMElement(
				Constants.WSRM.ACK_RANGE, rmNamespace);

		return sequenceAckElement;
	}

	public long getLowerValue() {
		return lowerValue;
	}

	public void setLowerValue(long lowerValue) {
		this.lowerValue = lowerValue;
	}

	public long getUpperValue() {
		return upperValue;
	}

	public void setUpperValue(long upperValue) {
		this.upperValue = upperValue;
	}
}