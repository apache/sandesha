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

import org.apache.axis2.addressing.EndpointReference;
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
public class CreateSequence implements IOMRMElement {
	private OMElement createSequenceElement;
	private AcksTo acksTo;
	private SequenceOffer sequenceOffer;
	
	OMNamespace createSeqNoNamespace =
		OMAbstractFactory.getSOAP11Factory().createOMNamespace(Constants.WSRM.NS_URI_RM, Constants.WSRM.NS_PREFIX_RM);
	public CreateSequence(){
		createSequenceElement = OMAbstractFactory.getSOAP11Factory().createOMElement(Constants.WSRM.CREATE_SEQUENCE,
				createSeqNoNamespace);
	}
	public OMElement getSOAPElement() throws OMException {
		createSequenceElement.addChild(sequenceOffer.getSOAPElement());
		createSequenceElement.addChild(acksTo.getSOAPElement());
		return createSequenceElement;
	}

	public Object fromSOAPEnvelope(SOAPEnvelope envelope) throws OMException {
		sequenceOffer = new SequenceOffer();
		acksTo = new AcksTo(new EndpointReference("",""));
		sequenceOffer.fromSOAPEnvelope(envelope);
		acksTo.fromSOAPEnvelope(envelope);		
		return this;
	}

	public OMElement toSOAPEnvelope(OMElement envelope) throws OMException {
		SOAPEnvelope soapEnvelope = (SOAPEnvelope)envelope;
		SOAPBody soapBody = soapEnvelope.getBody();
		soapBody.addChild(createSequenceElement);
		if(sequenceOffer != null){
			sequenceOffer.toSOAPEnvelope(createSequenceElement);
		}
		if (acksTo != null){
			acksTo.toSOAPEnvelope(createSequenceElement);
		}
		return envelope;
	}
	public void setAcksTo(AcksTo acksTo){
		this.acksTo = acksTo;
	}
	public void setSequenceOffer(SequenceOffer sequenceOffer){
		this.sequenceOffer = sequenceOffer;
	}
	public AcksTo getAcksTo(){
		return acksTo;
	}
	public SequenceOffer getSequenceOffer(){
		return sequenceOffer;
	}

}
