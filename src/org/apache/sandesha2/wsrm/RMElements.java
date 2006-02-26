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

import java.util.ArrayList;

import javax.xml.namespace.QName;

import org.apache.sandesha2.Sandesha2Constants;
import org.apache.sandesha2.util.SOAPAbstractFactory;
import org.apache.ws.commons.om.OMElement;
import org.apache.ws.commons.om.OMException;
import org.apache.ws.commons.soap.SOAP11Constants;
import org.apache.ws.commons.soap.SOAPEnvelope;
import org.apache.ws.commons.soap.SOAPFactory;
import org.apache.ws.commons.soap.SOAPHeader;

/**
 * @author Chamikara Jayalath <chamikaramj@gmail.com>
 * @author Sanka Samaranayaka <ssanka@gmail.com>
 * @author Saminda Abeyruwan  <saminda@opensource.lk>
 */

public class RMElements {

	private Sequence sequence = null;
	private SequenceAcknowledgement sequenceAcknowledgement = null;
	private CreateSequence createSequence = null;
	private CreateSequenceResponse createSequenceResponse = null;
	private TerminateSequence terminateSequence = null;
	private AckRequested ackRequested = null;
	private SOAPFactory factory = null;
	String rmNamespaceValue = null;
	
	public void fromSOAPEnvelope(SOAPEnvelope envelope, String action) {

		if (envelope == null)
			throw new OMException("The passed envelope is null");

		SOAPFactory factory;

		//Ya I know. Could hv done it directly :D (just to make it consistent)
		if (envelope.getNamespace().getName().equals(SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI))
			factory = SOAPAbstractFactory.getSOAPFactory(Sandesha2Constants.SOAPVersion.v1_1);
		else
			factory = SOAPAbstractFactory.getSOAPFactory(Sandesha2Constants.SOAPVersion.v1_2);
			
		
		//finding out the rm version.
		rmNamespaceValue = getRMNamespaceValue (envelope,action);
		if (rmNamespaceValue==null)
			return;
		
		OMElement sequenceElement = envelope.getHeader().getFirstChildWithName(
				new QName(rmNamespaceValue, Sandesha2Constants.WSRM_COMMON.SEQUENCE));
		if (sequenceElement != null) {
			sequence = new Sequence(factory,rmNamespaceValue);
			sequence.fromOMElement(envelope.getHeader());
		}

		OMElement sequenceAckElement = envelope.getHeader()
				.getFirstChildWithName(
						new QName(rmNamespaceValue,
								Sandesha2Constants.WSRM_COMMON.SEQUENCE_ACK));
		if (sequenceAckElement != null) {
			sequenceAcknowledgement = new SequenceAcknowledgement(factory,rmNamespaceValue);
			sequenceAcknowledgement.fromOMElement(envelope.getHeader());
		}

		OMElement createSeqElement = envelope.getBody().getFirstChildWithName(
				new QName(rmNamespaceValue,
						Sandesha2Constants.WSRM_COMMON.CREATE_SEQUENCE));
		
		if (createSeqElement != null) {
			createSequence = new CreateSequence(factory,rmNamespaceValue);
			createSequence.fromOMElement(envelope.getBody());
		}

		OMElement createSeqResElement = envelope.getBody()
				.getFirstChildWithName(
						new QName(rmNamespaceValue,
								Sandesha2Constants.WSRM_COMMON.CREATE_SEQUENCE_RESPONSE));
		if (createSeqResElement != null) {
			createSequenceResponse = new CreateSequenceResponse(factory,rmNamespaceValue);
			createSequenceResponse.fromOMElement(envelope.getBody());
		}

		OMElement terminateSeqElement = envelope.getBody()
				.getFirstChildWithName(
						new QName(rmNamespaceValue,
								Sandesha2Constants.WSRM_COMMON.TERMINATE_SEQUENCE));
		if (terminateSeqElement != null) {
			terminateSequence = new TerminateSequence(factory,rmNamespaceValue);
			terminateSequence.fromOMElement(envelope.getBody());
		}

		OMElement ackRequestedElement = envelope.getHeader()
				.getFirstChildWithName(
						new QName(rmNamespaceValue,
								Sandesha2Constants.WSRM_COMMON.ACK_REQUESTED));
		if (ackRequestedElement != null) {
			ackRequested = new AckRequested(factory,rmNamespaceValue);
			ackRequested.fromOMElement(envelope.getHeader());
		}
	}

	public SOAPEnvelope toSOAPEnvelope(SOAPEnvelope envelope) {
		if (sequence != null) {
			sequence.toOMElement(envelope.getHeader());
		}
		if (sequenceAcknowledgement != null) {
			sequenceAcknowledgement.toOMElement(envelope.getHeader());
		}
		if (createSequence != null) {
			createSequence.toOMElement(envelope.getBody());
		}
		if (createSequenceResponse != null) {
			createSequenceResponse.toOMElement(envelope.getBody());
		}
		if (terminateSequence != null) {
			terminateSequence.toOMElement(envelope.getBody());
		}
		if (ackRequested != null) {
			ackRequested.toOMElement(envelope.getBody());
		}
		return envelope;
	}

	public CreateSequence getCreateSequence() {
		return createSequence;
	}

	public CreateSequenceResponse getCreateSequenceResponse() {
		return createSequenceResponse;
	}

	public Sequence getSequence() {
		return sequence;
	}

	public SequenceAcknowledgement getSequenceAcknowledgement() {
		return sequenceAcknowledgement;
	}

	public TerminateSequence getTerminateSequence() {
		return terminateSequence;
	}

	public void setCreateSequence(CreateSequence createSequence) {
		this.createSequence = createSequence;
	}

	public void setCreateSequenceResponse(
			CreateSequenceResponse createSequenceResponse) {
		this.createSequenceResponse = createSequenceResponse;
	}

	public void setSequence(Sequence sequence) {
		this.sequence = sequence;
	}

	public void setSequenceAcknowledgement(
			SequenceAcknowledgement sequenceAcknowledgement) {
		this.sequenceAcknowledgement = sequenceAcknowledgement;
	}

	public void setTerminateSequence(TerminateSequence terminateSequence) {
		this.terminateSequence = terminateSequence;
	}

	public AckRequested getAckRequested() {
		return ackRequested;
	}

	public void setAckRequested(AckRequested ackRequested) {
		this.ackRequested = ackRequested;
	}
	
	private String getRMNamespaceValue (SOAPEnvelope envelope, String action) {
		SOAPHeader header = envelope.getHeader();
		if (header!=null) {
			ArrayList headers = header.getHeaderBlocksWithNSURI(Sandesha2Constants.SPEC_2005_02.NS_URI);
			if (headers!=null && headers.size()>0)
				return Sandesha2Constants.SPEC_2005_02.NS_URI;
			
			headers = header.getHeaderBlocksWithNSURI(Sandesha2Constants.SPEC_2005_10.NS_URI);
			if (headers!=null && headers.size()>0)
				return Sandesha2Constants.SPEC_2005_10.NS_URI;
		}
		
		//rm control messages with parts in the body will be identified by the wsa:action.
		if (action==null)
			return null;
		
		if (action.equals(Sandesha2Constants.SPEC_2005_02.Actions.ACTION_CREATE_SEQUENCE))
			return Sandesha2Constants.SPEC_2005_02.NS_URI;
		if (action.equals(Sandesha2Constants.SPEC_2005_02.Actions.ACTION_CREATE_SEQUENCE_RESPONSE))
			return Sandesha2Constants.SPEC_2005_02.NS_URI;
		if (action.equals(Sandesha2Constants.SPEC_2005_02.Actions.ACTION_SEQUENCE_ACKNOWLEDGEMENT))
			return Sandesha2Constants.SPEC_2005_02.NS_URI;
		if (action.equals(Sandesha2Constants.SPEC_2005_02.Actions.ACTION_TERMINATE_SEQUENCE))
			return Sandesha2Constants.SPEC_2005_02.NS_URI;
		
		if (action.equals(Sandesha2Constants.SPEC_2005_10.Actions.ACTION_CREATE_SEQUENCE))
			return Sandesha2Constants.SPEC_2005_10.NS_URI;
		if (action.equals(Sandesha2Constants.SPEC_2005_10.Actions.ACTION_CREATE_SEQUENCE_RESPONSE))
			return Sandesha2Constants.SPEC_2005_10.NS_URI;
		if (action.equals(Sandesha2Constants.SPEC_2005_10.Actions.ACTION_SEQUENCE_ACKNOWLEDGEMENT))
			return Sandesha2Constants.SPEC_2005_10.NS_URI;
		if (action.equals(Sandesha2Constants.SPEC_2005_10.Actions.ACTION_TERMINATE_SEQUENCE))
			return Sandesha2Constants.SPEC_2005_10.NS_URI;
		
		return null;   //a version could not be found
	}
}