/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.sandesha2.wsrm;

import java.util.Iterator;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.OMFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.addressing.EndpointReferenceHelper;
import org.apache.sandesha2.Sandesha2Constants;
import org.apache.sandesha2.SandeshaException;
import org.apache.sandesha2.i18n.SandeshaMessageHelper;
import org.apache.sandesha2.i18n.SandeshaMessageKeys;

public class Endpoint implements IOMRMElement {

	private EndpointReference epr;
	
	private String rmNamespaceValue = null;
	
	private String addressingNamespaceValue = null;

	// Constructor used while parsing
	public Endpoint (String rmNamespaceValue) throws AxisFault {
		if (!isNamespaceSupported(rmNamespaceValue))
			throw new SandeshaException (SandeshaMessageHelper.getMessage(
					SandeshaMessageKeys.unknownSpec,
					rmNamespaceValue));
		
		this.rmNamespaceValue = rmNamespaceValue;
	}
	
	// Constructor used while writing
	public Endpoint (EndpointReference epr, String rmNamespaceValue, String addressingNamespaceValue) throws AxisFault {
		this (rmNamespaceValue);
		this.addressingNamespaceValue = addressingNamespaceValue;
		this.epr = epr;
	}

	public String getNamespaceValue(){
		return rmNamespaceValue;
	}

	public Object fromOMElement(OMElement endpointElement) throws OMException,AxisFault {

		epr = EndpointReferenceHelper.fromOM (endpointElement);
		if (epr==null) {
			String message = SandeshaMessageHelper.getMessage(
					SandeshaMessageKeys.invalidElementFoundWithinElement,
					"EPR",
					Sandesha2Constants.WSRM_COMMON.ENDPOINT);
			throw new SandeshaException (message);
		}
		
		// Sniff the addressing namespace from the Address child of the endpointElement
		Iterator children = endpointElement.getChildElements();
		while(children.hasNext() && addressingNamespaceValue == null) {
			OMElement child = (OMElement) children.next();
			if("Address".equals(child.getLocalName())) {
				addressingNamespaceValue = child.getNamespace().getNamespaceURI();
			}
		}
		
		return this;
	}

	public OMElement toOMElement(OMElement element) throws OMException,AxisFault {

		if (epr == null)
			throw new OMException(SandeshaMessageHelper.getMessage(
					SandeshaMessageKeys.cannotSetEndpoint,
					null));

        // Ensure that we use the plain OMFactory (and not SOAPFactory). This forces
        // EndpointReferenceHelper.toOM to create an OMElement instead of a SOAPHeaderBlock.
        OMFactory factory = element.getOMFactory().getMetaFactory().getOMFactory();
		
		QName endpoint = new QName (rmNamespaceValue,Sandesha2Constants.WSRM_COMMON.ENDPOINT, Sandesha2Constants.WSRM_COMMON.NS_PREFIX_RM);
	    OMElement endpointElement =	EndpointReferenceHelper.toOM (factory, epr, endpoint, addressingNamespaceValue);
		
		element.addChild(endpointElement);
		return element;
	}

	public EndpointReference getEPR() {
		return epr;
	}

	public boolean isNamespaceSupported (String namespaceName) {
		if (Sandesha2Constants.SPEC_2007_02.NS_URI.equals(namespaceName))
			return true;
		
		return false;
	}
}
