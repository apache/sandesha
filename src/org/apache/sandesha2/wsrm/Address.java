/*
 * Created on Sep 1, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.apache.sandesha2.wsrm;

import javax.xml.namespace.QName;

import org.apache.axis2.addressing.EndpointReference;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.sandesha2.Sandesha2Constants;
import org.apache.sandesha2.SandeshaException;

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
		rmNamespace = factory.createOMNamespace(Sandesha2Constants.WSA.NS_URI_ADDRESSING,
				Sandesha2Constants.WSA.NS_PREFIX_ADDRESSING);
		addressElement = factory.createOMElement(
				Sandesha2Constants.WSA.ADDRESS, rmNamespace);
	}
	
	public Address (EndpointReference epr,SOAPFactory factory) {
		this(factory);
		this.epr = epr;
	}

	public Object fromOMElement(OMElement element) throws OMException {

		OMElement addressPart = element.getFirstChildWithName(new QName(
				Sandesha2Constants.WSA.NS_URI_ADDRESSING, Sandesha2Constants.WSA.ADDRESS));
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
				Sandesha2Constants.WSA.ADDRESS, rmNamespace);
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
				Sandesha2Constants.WSA.ADDRESS, rmNamespace);

		return element;
	}

	public EndpointReference getEpr() {
		return epr;
	}

	public void setEpr(EndpointReference epr) {
		this.epr = epr;
	}
	
	public boolean isNamespaceSupported (String namespaceName) {
		if (Sandesha2Constants.SPEC_2005_02.NS_URI.equals(namespaceName))
			return true;
		
		if (Sandesha2Constants.SPEC_2005_10.NS_URI.equals(namespaceName))
			return true;
		
		return false;
	}
}