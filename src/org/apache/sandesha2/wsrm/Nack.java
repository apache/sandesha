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

public class Nack implements IOMRMElement {
	
	private long nackNumber;
	
	private SOAPFactory defaultFactory;
	
	private String namespaceValue = null;
		
	public Nack(SOAPFactory factory,String namespaceValue) throws SandeshaException {
		if (!isNamespaceSupported(namespaceValue))
			throw new SandeshaException ("Unsupported namespace");
		
		this.defaultFactory = factory;
		this.namespaceValue = namespaceValue;
	}
	
	public String getNamespaceValue() {
		return namespaceValue;
	}
	

	public Object fromOMElement(OMElement nackElement) throws OMException{
		
		if (nackElement==null)
			throw new OMException ("Passed seq ack element does not contain a nack part");
		
		try {
			nackNumber = Long.parseLong(nackElement.getText());
		}catch (Exception ex ) {
			throw new OMException ("Nack element does not contain a valid long value");
		}
		
		return this;
	} 
	
	public OMElement toOMElement(OMElement sequenceAckElement) throws OMException {
		if (sequenceAckElement==null)
			throw new OMException ("Cant set the nack part since the seq ack element is null");
		
		if (nackNumber<=0)
			throw new OMException ("Cant set the nack part since the nack number does not have a valid value");
		
		OMFactory factory = sequenceAckElement.getOMFactory();
		if (factory==null)
			factory = defaultFactory;
		
		OMNamespace rmNamespace = factory.createOMNamespace(namespaceValue,Sandesha2Constants.WSRM_COMMON.NS_PREFIX_RM);
		OMElement nackElement = factory.createOMElement(Sandesha2Constants.WSRM_COMMON.NACK,rmNamespace);
		nackElement.setText(Long.toString(nackNumber));
		sequenceAckElement.addChild(nackElement);
		
		return sequenceAckElement;
	}

	public long getNackNumber() {
		return nackNumber;
	}

	public void setNackNumber(long nackNumber) {
		this.nackNumber = nackNumber;
	}

	public boolean isNamespaceSupported (String namespaceName) {
		if (Sandesha2Constants.SPEC_2005_02.NS_URI.equals(namespaceName))
			return true;
		
		if (Sandesha2Constants.SPEC_2005_10.NS_URI.equals(namespaceName))
			return true;
		
		return false;
	}
}
