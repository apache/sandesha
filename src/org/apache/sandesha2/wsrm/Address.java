/*
 * Created on Sep 1, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.apache.sandesha2.wsrm;

import javax.xml.namespace.QName;

import org.apache.axis2.addressing.EndpointReference;
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

public class Address implements IOMRMElement {

	EndpointReference epr = null;

	OMElement addressElement;
	
	SOAPFactory factory;

	OMNamespace rmNamespace = null;

	public Address(SOAPFactory factory) {
		this.factory = factory;
		rmNamespace = factory.createOMNamespace(Constants.WSA.NS_URI_ADDRESSING,
				Constants.WSA.NS_PREFIX_ADDRESSING);
		addressElement = factory.createOMElement(
				Constants.WSA.ADDRESS, rmNamespace);
	}
	
	public Address (EndpointReference epr,SOAPFactory factory) {
		this(factory);
		this.epr = epr;
	}

	public Object fromOMElement(OMElement element) throws OMException {

		OMElement addressPart = element.getFirstChildWithName(new QName(
				Constants.WSA.NS_URI_ADDRESSING, Constants.WSA.ADDRESS));
		if (addressPart == null)
			throw new OMException(
					"Cant find an Address element in the given part");
		String addressText = addressPart.getText();
		if (addressText == null || addressText == "")
			throw new OMException(
					"Passed element does not have a valid address text");

		addressElement = addressPart;
		epr = new EndpointReference(addressText);
		addressElement = factory.createOMElement(
				Constants.WSA.ADDRESS, rmNamespace);
		return this;

	}

	public OMElement getOMElement() throws OMException {
		return addressElement;
	}

	public OMElement toOMElement(OMElement element) throws OMException {
		if (addressElement == null)
			throw new OMException(
					"Cant set Address. The address element is null");

		if (epr == null || epr.getAddress() == null || epr.getAddress() == "")
			throw new OMException(
					"cant set the address. The address value is not valid");

		addressElement.setText(epr.getAddress());
		element.addChild(addressElement);

		addressElement = factory.createOMElement(
				Constants.WSA.ADDRESS, rmNamespace);

		return element;
	}

	public EndpointReference getEpr() {
		return epr;
	}

	public void setEpr(EndpointReference epr) {
		this.epr = epr;
	}
}