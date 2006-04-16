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
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.sandesha2.Sandesha2Constants;
import org.apache.sandesha2.SandeshaException;

/**
 * Adds the SequenceFault header block.
 * 
 * @author Chamikara Jayalath <chamikaramj@gmail.com>
 * @author Sanka Samaranayaka <ssanka@gmail.com>
 * @author Saminda Abeyruwan  <saminda@opensource.lk>
 */

public class SequenceFault implements IOMRMElement {
	
	private FaultCode faultCode;
	
	private SOAPFactory defaultFactory;
	
	private String namespaceValue = null;

	public SequenceFault(SOAPFactory factory,String namespaceValue) throws SandeshaException {
		if (!isNamespaceSupported(namespaceValue))
			throw new SandeshaException ("Unsupported namespace");
		
		this.defaultFactory = factory;
		this.namespaceValue = namespaceValue;
	}

	public String getNamespaceValue() {
		return namespaceValue;
	}

	public Object fromOMElement(OMElement body) throws OMException,SandeshaException {

		if (body == null || !(body instanceof SOAPBody))
			throw new OMException(
					"Cant get Sequence Fault part from a non-header element");

		OMElement sequenceFaultPart = body.getFirstChildWithName(new QName(
				namespaceValue, Sandesha2Constants.WSRM_COMMON.SEQUENCE_FAULT));

		if (sequenceFaultPart == null)
			throw new OMException("The passed element does not contain a Sequence Fault element");

		OMElement faultCodePart = sequenceFaultPart
				.getFirstChildWithName(new QName(namespaceValue,Sandesha2Constants.WSRM_COMMON.FAULT_CODE));

		if (faultCodePart != null) {
			faultCode = new FaultCode(defaultFactory,namespaceValue);
			faultCode.fromOMElement(sequenceFaultPart);
		}

		return this;
	}

	public OMElement toOMElement(OMElement body) throws OMException {

		if (body == null || !(body instanceof SOAPBody))
			throw new OMException("Cant get Sequence Fault part from a non-header element");

		OMFactory factory = body.getOMFactory();
		if (factory==null)
			factory = defaultFactory;
		
		OMNamespace rmNamespace = factory.createOMNamespace(namespaceValue,Sandesha2Constants.WSRM_COMMON.NS_PREFIX_RM);
		OMElement sequenceFaultElement =factory.createOMElement(
				Sandesha2Constants.WSRM_COMMON.SEQUENCE_FAULT, rmNamespace);
		if (faultCode != null)
			faultCode.toOMElement(sequenceFaultElement);

		body.addChild(sequenceFaultElement);

		return body;
	}

	public void setFaultCode(FaultCode faultCode) {
		this.faultCode = faultCode;
	}

	public FaultCode getFaultCode() {
		return faultCode;
	}
	
	public boolean isNamespaceSupported (String namespaceName) {
		if (Sandesha2Constants.SPEC_2005_02.NS_URI.equals(namespaceName))
			return true;
		
		if (Sandesha2Constants.SPEC_2005_10.NS_URI.equals(namespaceName))
			return true;
		
		return false;
	}

}
