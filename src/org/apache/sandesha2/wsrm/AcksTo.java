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

import javax.xml.namespace.QName;

import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMException;
import org.apache.axis2.om.OMNamespace;
import org.apache.axis2.soap.SOAPFactory;
import org.apache.sandesha2.Constants;

/**
 * @author Chamikara Jayalath <chamikaramj@gmail.com>
 * @author Sanka Samaranayaka <ssanka@gmail.com>
 * @author Saminda Abeyruwan  <saminda@opensource.lk>
 */

public class AcksTo implements IOMRMElement {

	private Address address;

	private OMElement acksToElement;

	//private OMElement addressElement;

	private SOAPFactory factory;
	
	OMNamespace rmNamespace = null;

	public AcksTo(SOAPFactory factory) {
		this.factory = factory;
		rmNamespace = factory.createOMNamespace(
				Constants.WSRM.NS_URI_RM, Constants.WSRM.NS_PREFIX_RM);
		acksToElement = factory.createOMElement(
				Constants.WSRM.ACKS_TO, rmNamespace);
	}
	
	public AcksTo (Address address,SOAPFactory factory) {
		this (factory);
		this.address = address;
	}

	public OMElement getOMElement() throws OMException {
		return acksToElement;
	}

	public Object fromOMElement(OMElement element) throws OMException {
		OMElement acksToPart = element.getFirstChildWithName(new QName(
				Constants.WSRM.NS_URI_RM, Constants.WSRM.ACKS_TO));

		if (acksToPart == null)
			throw new OMException(
					"Passed element does not contain an acksTo part");

		address = new Address(factory);
		address.fromOMElement(acksToPart);

		acksToElement = factory.createOMElement(
				Constants.WSRM.ACKS_TO, rmNamespace);

		return this;
	}

	public OMElement toOMElement(OMElement element) throws OMException {

		if (acksToElement == null)
			throw new OMException("Cant set AcksTo. AcksTo element is null");
		if (address == null)
			throw new OMException("Cant set AcksTo. Address is null");

		OMElement acksToPart = element.getFirstChildWithName(new QName(
				Constants.WSA.NS_URI_ADDRESSING, Constants.WSRM.ACKS_TO));

		address.toOMElement(acksToElement);
		element.addChild(acksToElement);

		acksToElement =factory.createOMElement(
				Constants.WSRM.ACKS_TO, rmNamespace);

		return element;
	}

	public Address getAddress() {
		return address;
	}

	public void setAddress(Address address) {
		this.address = address;
	}
}