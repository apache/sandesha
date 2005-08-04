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

/**
 * @author Saminda
 *
 */
import java.util.Iterator;

import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMException;
import org.apache.axis2.om.OMNamespace;
import org.apache.axis2.om.OMNode;
import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.sandesha2.Constants;

public class Identifier implements Constants, IOMRMElement {
	private OMElement identifierElement;

	private String identifier;

	OMNamespace wsuNamespace = OMAbstractFactory.getSOAP11Factory().createOMNamespace(
			Constants.WSRM.NS_URI_RM, Constants.WSRM.NS_PREFIX_RM);

	public Identifier() {

		identifierElement = OMAbstractFactory.getSOAP11Factory().createOMElement(
				Constants.WSRM.IDENTIFIER, wsuNamespace);
	}

	public void setURI(String uri) throws OMException {
		identifierElement.addChild(OMAbstractFactory.getSOAP11Factory().createText(uri));
	}

	public void setIndentifer(String identifier) {
		this.identifier = identifier;
	}

	public String getIdentifier() {
		return identifier;
	}

	public OMElement getSOAPElement() throws OMException {
		identifierElement.addChild(OMAbstractFactory.getSOAP11Factory().createText(
				identifier));
		return identifierElement;
	}

	private boolean readElement(OMElement element) {
		Iterator iterator = element.getChildren();
		while (iterator.hasNext()) {
			OMNode omnode = (OMNode)iterator.next();
			if(omnode.getType() != OMNode.ELEMENT_NODE){
				continue ;
			}				
			OMElement childElement = (OMElement)omnode ;
			if (childElement.getLocalName().equals(Constants.WSRM.IDENTIFIER)) {
				identifier = childElement.getText();
				return true;
			}else {
			   readElement(childElement);	
			}
		}
		return false;
	}

	public Object fromSOAPEnvelope(SOAPEnvelope envelope) throws OMException {
		readElement(envelope);
		return this;
	}

	public OMElement toSOAPEnvelope(OMElement messageElement)
			throws OMException {
		//soapheaderblock will be given or anyother block reference to the requirment
		if (identifier == null || identifier == "") {
			throw new OMException("Set Identifier");
		}
		identifierElement.addChild(OMAbstractFactory.getSOAP11Factory().createText(
				identifier));
		messageElement.addChild(identifierElement);
		return messageElement;
	}

	public String toString() {
		return identifier;
	}

	public boolean equals(Object obj) {

		if (obj instanceof Identifier) {
			if (this.identifier == ((String) (((Identifier) obj)
					.getIdentifier()))) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	public int hashCode() {
		return identifier.hashCode();
	}
}