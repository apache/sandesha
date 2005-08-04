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

import java.util.Iterator;

import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMException;
import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.axis2.soap.SOAPHeader;
import org.apache.axis2.soap.SOAPHeaderBlock;
import org.apache.sandesha2.Constants;

/**
 * @author Saminda
 *
 */
public class RMElements{
	private Sequence sequenceElement;
	private SequenceAcknowledgement sequenceAcknowledgementElement;
	
	private CreateSequence createSequenceElement;
	private CreateSequenceResponse createSequenceResponseElement;
	private TerminateSequence terminateSequenceElement;

	public OMElement getSOAPElement() throws OMException {
		//this mehod has no significant in the code
		return null;
	}
	
	public Object fromSOAPEnvelope(SOAPEnvelope envelope) throws OMException {
		SOAPHeader soapHeader = envelope.getHeader();
		Iterator iterator = soapHeader.examineAllHeaderBlocks();
		while (iterator.hasNext()){
			SOAPHeaderBlock childBlock = (SOAPHeaderBlock)iterator.next();
			if (childBlock.getLocalName().equals(Constants.WSRM.SEQUENCE)){
				sequenceElement = new Sequence();
				childBlock.setMustUnderstand(false);
				childBlock.setComplete(true);
				sequenceElement.fromSOAPEnvelope(envelope);
				
			}
			if (childBlock.getLocalName().equals(Constants.WSRM.SEQUENCE_ACK)){
				sequenceAcknowledgementElement = new SequenceAcknowledgement();
				childBlock.setMustUnderstand(false);
				childBlock.setComplete(true);
				sequenceAcknowledgementElement.fromSOAPEnvelope(envelope);
			}
		}
		createSequenceElement = new CreateSequence();
		createSequenceResponseElement = new CreateSequenceResponse();
		terminateSequenceElement = new TerminateSequence();
		
		createSequenceElement.fromSOAPEnvelope(envelope);
		createSequenceResponseElement.fromSOAPEnvelope(envelope);
		terminateSequenceElement.fromSOAPEnvelope(envelope);
		return this;
	}

	public SOAPEnvelope toSOAPEnvelope(SOAPEnvelope envelope) throws OMException {
		
		
		if(sequenceElement != null){
			sequenceElement.toSOAPEnvelope(envelope);
		}
		if ( sequenceAcknowledgementElement != null) {
			sequenceAcknowledgementElement.toSOAPEnvelope(envelope);
		}
		if ( createSequenceElement != null){
			createSequenceElement.toSOAPEnvelope(envelope);
		}
		if (createSequenceResponseElement != null){
			createSequenceResponseElement.toSOAPEnvelope(envelope);
		}
		if (terminateSequenceElement != null){
			terminateSequenceElement.toSOAPEnvelope(envelope);
		}
		return envelope;
	}
	public CreateSequence getCreateSequence(){
		return createSequenceElement;
	}
	public CreateSequenceResponse getCreateSequenceResponse(){
		return createSequenceResponseElement;
	}
	public Sequence getSequence(){
		return sequenceElement;
	}
	public SequenceAcknowledgement getSequenceAcknowledgement(){
		return sequenceAcknowledgementElement;
	}
	public TerminateSequence getTerminateSequence(){
		return terminateSequenceElement;
	}
	public void setCreateSequence(CreateSequence createSequence){
		createSequenceElement = createSequence;
	}
	public void setCreateSequenceResponse(CreateSequenceResponse createSequenceResponse){
		createSequenceResponseElement = createSequenceResponse;
	}
	public void setSequence(Sequence sequence){
		sequenceElement = sequence;
	}
	public void setSequenceAcknowledgement(SequenceAcknowledgement sequenceAcknowledgement){
		sequenceAcknowledgementElement = sequenceAcknowledgement;
	}
	public void setTerminateSequence(TerminateSequence terminateSequence){
		terminateSequenceElement = terminateSequence;
	}
	
	
}
