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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.OMFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.sandesha2.Sandesha2Constants;
import org.apache.sandesha2.SandeshaException;
import org.apache.sandesha2.i18n.SandeshaMessageHelper;
import org.apache.sandesha2.i18n.SandeshaMessageKeys;
import org.apache.sandesha2.util.EndpointReferenceSerializer;

public class Endpoint implements IOMRMElement {

	private EndpointReference epr;
	
	private String rmNamespaceValue = null;
	
	private String addressingNamespaceValue = null;

	public Endpoint (String rmNamespaceValue,String addressingNamespaceValue) throws AxisFault {
		if (!isNamespaceSupported(rmNamespaceValue))
			throw new SandeshaException (SandeshaMessageHelper.getMessage(
					SandeshaMessageKeys.unknownSpec,
					rmNamespaceValue));
		
		this.rmNamespaceValue = rmNamespaceValue;
		this.addressingNamespaceValue = addressingNamespaceValue;
	}
	
	public Endpoint (EndpointReference epr, String rmNamespaceValue, String addressingNamespaceValue) throws AxisFault {
		this (rmNamespaceValue,addressingNamespaceValue);
		this.epr = epr;
	}

	public String getNamespaceValue(){
		return rmNamespaceValue;
	}

	public Object fromOMElement(OMElement endpointElement) throws OMException,AxisFault {

		epr = EndpointReferenceSerializer.fromOM (endpointElement);
		if (epr==null) {
			String message = SandeshaMessageHelper.getMessage(
					SandeshaMessageKeys.invalidElementFoundWithinElement,
					"EPR",
					Sandesha2Constants.WSRM_COMMON.ENDPOINT);
			throw new SandeshaException (message);
		}
		
		return this;
	}

	public OMElement toOMElement(OMElement element) throws OMException,AxisFault {

		if (epr == null)
			throw new OMException(SandeshaMessageHelper.getMessage(
					SandeshaMessageKeys.cannotSetEndpoint,
					null));

		OMFactory factory = element.getOMFactory();
		
		QName endpoint = new QName (rmNamespaceValue,Sandesha2Constants.WSRM_COMMON.ENDPOINT, Sandesha2Constants.WSRM_COMMON.NS_PREFIX_RM);
	    OMElement endpointElement =	EndpointReferenceSerializer.toOM (factory,epr, endpoint,addressingNamespaceValue);
		
		element.addChild(endpointElement);
		return element;
	}

	public EndpointReference getEPR() {
		return epr;
	}

	public void setEPR(EndpointReference epr) {
		this.epr = epr;
	}
	
	public boolean isNamespaceSupported (String namespaceName) {
		if (Sandesha2Constants.SPEC_2005_02.NS_URI.equals(namespaceName))
			return true;
		
		if (Sandesha2Constants.SPEC_2006_08.NS_URI.equals(namespaceName))
			return true;
		
		return false;
	}
}
