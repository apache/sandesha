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
 * @author Chamikara Jayalath <chamikaramj@gmail.com>
 * @author Sanka Samaranayaka <ssanka@gmail.com>
 * @author Saminda Abeyruwan  <saminda@opensource.lk>
 */

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.sandesha2.Sandesha2Constants;
import org.apache.sandesha2.SandeshaException;

public class Identifier implements Sandesha2Constants, IOMRMElement {

	private String identifier = null;
	
	private OMFactory defaultFactory;
	
	private String namespaceValue = null;
	
	public Identifier(OMFactory defaultFactory, String namespaceValue) throws SandeshaException {
		if (!isNamespaceSupported(namespaceValue))
			throw new SandeshaException ("Unsupported namespace");
		
		this.defaultFactory = defaultFactory;
		this.namespaceValue = namespaceValue;
	}

	public void setIndentifer(String identifier) {
		this.identifier = identifier;
	}

	public String getIdentifier() {
		return identifier;
	}

	public String getNamespaceValue() throws OMException {
		return namespaceValue;
	}

	public Object fromOMElement(OMElement element) throws OMException {
		
		OMFactory factory = element.getOMFactory();
		if (factory==null)
			factory = defaultFactory;
		
		OMElement identifierPart = element.getFirstChildWithName(new QName(
				namespaceValue, Sandesha2Constants.WSRM_COMMON.IDENTIFIER));
		if (identifierPart == null)
			throw new OMException("The parsed element does not contain an identifier part");
		
		String identifierText = identifierPart.getText();
		if (identifierText == null || identifierText == "")
			throw new OMException("The identifier value is not valid");

		identifier = identifierText;
		return this;
	}

	public OMElement toOMElement(OMElement element) throws OMException {

		if (identifier == null || identifier == "") {
			throw new OMException("identifier is not set .. ");
		}
		
		OMFactory factory = element.getOMFactory();
		if (factory==null)
			factory = defaultFactory;
			
		OMNamespace wsrmNamespace = factory.createOMNamespace(namespaceValue,Sandesha2Constants.WSRM_COMMON.NS_PREFIX_RM);
		OMElement identifierElement = factory.createOMElement(Sandesha2Constants.WSRM_COMMON.IDENTIFIER, wsrmNamespace);

		identifierElement.setText(identifier);
		element.addChild(identifierElement);

		return element;
	}

	public String toString() {
		return identifier;
	}

	public int hashCode() {
		return identifier.hashCode();
	}
	
	public boolean isNamespaceSupported (String namespaceName) {
		if (Sandesha2Constants.SPEC_2005_02.NS_URI.equals(namespaceName))
			return true;
		
		if (Sandesha2Constants.SPEC_2005_10.NS_URI.equals(namespaceName))
			return true;
		
		return false;
	}
}