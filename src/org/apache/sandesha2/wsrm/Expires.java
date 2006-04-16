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
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.sandesha2.Sandesha2Constants;
import org.apache.sandesha2.SandeshaException;

/**
 * @author Chamikara Jayalath <chamikaramj@gmail.com>
 * @author Sanka Samaranayaka <ssanka@gmail.com>
 * @author Saminda Abeyruwan  <saminda@opensource.lk>
 */

public class Expires implements IOMRMElement {

	private OMFactory defaultFactory;
	
	private String duration = null;
	
	private String namespaceValue = null;

	public Expires(OMFactory factory,String namespaceValue) throws SandeshaException {
		if (!isNamespaceSupported(namespaceValue))
			throw new SandeshaException ("Unsupported namespace");
		
		this.defaultFactory = factory;
		this.namespaceValue = namespaceValue;
	}

	public Object fromOMElement(OMElement element) throws OMException {
		OMElement expiresPart = element.getFirstChildWithName(new QName(
				namespaceValue, Sandesha2Constants.WSRM_COMMON.EXPIRES));
		if (expiresPart == null)
			throw new OMException("Passed elemenet does not have a Expires part");
		String expiresText = expiresPart.getText();
		if (expiresText == null || expiresText == "")
			throw new OMException("The duration value is not valid");

		duration = expiresText;
		return element;
	}

	public String getNamespaceValue() throws OMException {
		// TODO Auto-generated method stub
		return namespaceValue;
	}

	public OMElement toOMElement(OMElement element) throws OMException {

		if (duration == null || duration == "")
			throw new OMException("Cant set Expires. The duration value is not set");

		OMFactory factory = element.getOMFactory();
		if (factory==null)
			factory = defaultFactory;
		
		OMNamespace rmNamespace = factory.createOMNamespace(namespaceValue,Sandesha2Constants.WSRM_COMMON.NS_PREFIX_RM);
		OMElement expiresElement = factory.createOMElement(
				Sandesha2Constants.WSRM_COMMON.EXPIRES, rmNamespace);
		
		expiresElement.setText(duration);
		element.addChild(expiresElement);

		return element;
	}

	public String getDuration() {
		return duration;
	}

	public void setDuration(String duration) {
		this.duration = duration;
	}
	
	public boolean isNamespaceSupported (String namespaceName) {
		if (Sandesha2Constants.SPEC_2005_02.NS_URI.equals(namespaceName))
			return true;
		
		if (Sandesha2Constants.SPEC_2005_10.NS_URI.equals(namespaceName))
			return true;
		
		return false;
	}
}