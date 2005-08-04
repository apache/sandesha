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

import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMException;
import org.apache.axis2.om.OMNamespace;
import org.apache.axis2.soap.SOAPBody;
import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.sandesha2.Constants;

/**
 * @author Saminda
 *
 */
public class TerminateSequence implements IOMRMElement {
	private OMElement terminateSequenceElement;
	private Identifier identifier;
	
	OMNamespace terminateSequenceNameSpace = 
		OMAbstractFactory.getSOAP11Factory().createOMNamespace(
				Constants.WSRM.NS_URI_RM, Constants.WSRM.NS_PREFIX_RM);
	public TerminateSequence(){
		terminateSequenceElement = OMAbstractFactory.getSOAP11Factory().createOMElement(
				Constants.WSRM.TERMINATE_SEQUENCE,terminateSequenceNameSpace);
	}
	public OMElement getSOAPElement() throws OMException {
		terminateSequenceElement.addChild(identifier.getSOAPElement());
		return terminateSequenceElement;
	}
	public void addChild(OMElement element){
		terminateSequenceElement.addChild(element);
	}
	public Object fromSOAPEnvelope(SOAPEnvelope envelope) throws OMException {
		identifier = new Identifier();
		identifier.fromSOAPEnvelope(envelope);
		return this;
	}
	public OMElement toSOAPEnvelope(OMElement envelope) throws OMException {
		SOAPEnvelope soapEnvelope = (SOAPEnvelope)envelope;
		SOAPBody soapBody = soapEnvelope.getBody();
		if (identifier != null){
			identifier.toSOAPEnvelope(terminateSequenceElement);
		}
		soapBody.addChild(terminateSequenceElement);
		return envelope;
	}
	public Identifier getIdentifier(){
		return identifier;
	}
	public void setIdentifier(Identifier identifier){
		this.identifier = identifier;
	}

}
