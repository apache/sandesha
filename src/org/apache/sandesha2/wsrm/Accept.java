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
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMException;
import org.apache.axis2.om.OMNamespace;
import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.sandesha2.Constants;
import org.apache.sandesha2.SOAPAbstractFactory;

/**
 * @author Saminda
 * @author chamikara
 * @author sanka
 */

public class Accept implements IOMRMElement {
	private OMElement acceptElement;
	private AcksTo acksTo;
	
	OMNamespace rmNamespace =
		SOAPAbstractFactory.getSOAPFactory(Constants.DEFAULT_SOAP_VERSION).createOMNamespace(Constants.WSRM.NS_URI_RM, Constants.WSRM.NS_PREFIX_RM);
	
	public Accept(){
		acceptElement = SOAPAbstractFactory.getSOAPFactory(Constants.DEFAULT_SOAP_VERSION).createOMElement(
				Constants.WSRM.ACCEPT,rmNamespace);
	}
	public OMElement getOMElement() throws OMException {
		return acceptElement;
	}
	
	public Object fromOMElement(OMElement element) throws OMException {
		
		OMElement acceptPart = element.getFirstChildWithName(
				new QName (Constants.WSRM.NS_URI_RM,Constants.WSRM.ACCEPT));
		if (acceptPart==null)
			throw new OMException ("Passed element does not contain an Accept part");
		
		acksTo = new AcksTo();
		acksTo.fromOMElement(acceptPart);
		
		acceptElement = SOAPAbstractFactory.getSOAPFactory(Constants.DEFAULT_SOAP_VERSION).createOMElement(
				Constants.WSRM.ACCEPT,rmNamespace);
		
		return this;
	}

	public OMElement toOMElement(OMElement element) throws OMException {

		if (acceptElement==null)
			throw new OMException ("Cant add Accept part since the internal element is null");
		
		if (acksTo==null)
			throw new OMException ("Cant add Accept part since AcksTo object is null");
		
		acksTo.toOMElement(acceptElement);
		element.addChild(acceptElement);
		
		acceptElement = SOAPAbstractFactory.getSOAPFactory(Constants.DEFAULT_SOAP_VERSION).createOMElement(
				Constants.WSRM.ACCEPT,rmNamespace);
		
		return element;
	}
	
	public void setAcksTo(AcksTo acksTo){
		this.acksTo = acksTo;
	}
	public AcksTo getAcksTo(){
		return acksTo;
	}
}
