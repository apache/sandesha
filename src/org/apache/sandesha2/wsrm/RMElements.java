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

import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMException;
import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.axis2.soap.SOAPHeader;
import org.apache.axis2.soap.SOAPHeaderBlock;
import org.apache.sandesha2.Constants;

/**
 * @author Saminda
 * @author chamikara
 * @author sanka
 */

public class RMElements {

	private Sequence sequence = null;

	private SequenceAcknowledgement sequenceAcknowledgement = null;

	private CreateSequence createSequence = null;

	private CreateSequenceResponse createSequenceResponse = null;

	private TerminateSequence terminateSequence = null;

	private AckRequested ackRequested = null;

	public void fromSOAPEnvelope(SOAPEnvelope envelope) {

		if (envelope == null)
			throw new OMException("The passed envelope is null");

		OMElement sequenceElement = envelope.getHeader().getFirstChildWithName(
				new QName(Constants.WSRM.NS_URI_RM, Constants.WSRM.SEQUENCE));
		if (sequenceElement != null) {
			sequence = new Sequence();
			sequence.fromOMElement(envelope.getHeader());
		}

		OMElement sequenceAckElement = envelope.getHeader()
				.getFirstChildWithName(
						new QName(Constants.WSRM.NS_URI_RM,
								Constants.WSRM.SEQUENCE_ACK));
		if (sequenceAckElement != null) {
			sequenceAcknowledgement = new SequenceAcknowledgement();
			sequenceAcknowledgement.fromOMElement(envelope.getHeader());
		}

		OMElement createSeqElement = envelope.getBody().getFirstChildWithName(
				new QName(Constants.WSRM.NS_URI_RM,
						Constants.WSRM.CREATE_SEQUENCE));
		if (createSeqElement != null) {
			createSequence = new CreateSequence();
			createSequence.fromOMElement(envelope.getBody());
		}

		OMElement createSeqResElement = envelope.getBody()
				.getFirstChildWithName(
						new QName(Constants.WSRM.NS_URI_RM,
								Constants.WSRM.CREATE_SEQUENCE_RESPONSE));
		if (createSeqResElement != null) {
			createSequenceResponse = new CreateSequenceResponse();
			createSequenceResponse.fromOMElement(envelope.getBody());
		}

		OMElement terminateSeqElement = envelope.getBody()
				.getFirstChildWithName(
						new QName(Constants.WSRM.NS_URI_RM,
								Constants.WSRM.TERMINATE_SEQUENCE));
		if (terminateSeqElement != null) {
			terminateSequence = new TerminateSequence();
			terminateSequence.fromOMElement(envelope.getBody());
		}

		OMElement ackRequestedElement = envelope.getHeader()
				.getFirstChildWithName(
						new QName(Constants.WSRM.NS_URI_RM,
								Constants.WSRM.ACK_REQUESTED));
		if (ackRequestedElement != null) {
			ackRequested = new AckRequested();
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
}