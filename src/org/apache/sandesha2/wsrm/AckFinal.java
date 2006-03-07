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

import org.apache.sandesha2.Sandesha2Constants;
import org.apache.sandesha2.SandeshaException;
import org.apache.ws.commons.om.OMElement;
import org.apache.ws.commons.om.OMException;
import org.apache.ws.commons.om.OMNamespace;
import org.apache.ws.commons.soap.SOAPFactory;

/**
 * @author Chamikara Jayalath <chamikaramj@gmail.com>
 * 
 * This represent the wsrm:final element that may be present withing a sequence acknowledgement.
 */
public class AckFinal {

	private OMElement finalElement = null;
	OMNamespace rmNamespace = null;
	SOAPFactory factory;
	String namespaceValue = null;
	
	public AckFinal(SOAPFactory factory,String namespaceValue) throws SandeshaException {
		if (!isNamespaceSupported(namespaceValue))
			throw new SandeshaException ("Unsupported namespace");
		
		this.factory = factory;
		this.namespaceValue = namespaceValue;
		rmNamespace = factory.createOMNamespace(
				namespaceValue, Sandesha2Constants.WSRM_COMMON.NS_PREFIX_RM);
		finalElement = factory.createOMElement(
				Sandesha2Constants.WSRM_COMMON.FINAL, rmNamespace);
	}

	public OMElement getOMElement() throws OMException {
		return finalElement;
	}

	public Object fromOMElement(OMElement element) throws OMException {
		OMElement nonePart = element.getFirstChildWithName(new QName(
				namespaceValue, Sandesha2Constants.WSRM_COMMON.FINAL));
		if (nonePart == null)
			throw new OMException(
					"The passed element does not contain a 'Final' part");

		finalElement = factory.createOMElement(
				Sandesha2Constants.WSRM_COMMON.FINAL, rmNamespace);

		return this;
	}

	public OMElement toOMElement(OMElement sequenceAckElement) throws OMException {
		//soapheaderblock element will be given
		if (finalElement == null)
			throw new OMException("Cant set 'Final' element. It is null");

		sequenceAckElement.addChild(finalElement);

		finalElement = factory.createOMElement(
				Sandesha2Constants.WSRM_COMMON.FINAL, rmNamespace);

		return sequenceAckElement;
	}

	public void setLastMessageElement(OMElement noneElement) {
		this.finalElement = noneElement;
	}

	public OMElement getLastMessageElement() {
		return finalElement;
	}

	public boolean isPresent() {
		return (finalElement != null) ? true : false;
	}
	
	//this element is only supported in 2005_05_10 spec.
	public boolean isNamespaceSupported (String namespaceName) {
		if (Sandesha2Constants.SPEC_2005_02.NS_URI.equals(namespaceName))
			return false;
		
		if (Sandesha2Constants.SPEC_2005_10.NS_URI.equals(namespaceName))
			return true;
		
		return false;
	}
}
