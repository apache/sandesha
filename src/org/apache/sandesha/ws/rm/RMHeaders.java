/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Axis" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
package org.apache.sandesha.ws.rm;

import org.apache.axis.AxisFault;
import org.apache.axis.message.SOAPBodyElement;
import org.apache.axis.message.SOAPEnvelope;
import org.apache.axis.message.SOAPHeaderElement;

import javax.xml.soap.SOAPException;
import java.util.Iterator;

/**
 * @author 
 * Amila Navarathna<br>
 * Jaliya Ekanayaka<br>
 * Sudar Nimalan<br>
 * (Apache Sandesha Project)
 *
 */
public class RMHeaders {
	private CreateSequence createSequence = null;
	private CreateSequenceResponse createSequenceResponse = null;
	private AckRequested ackRequest = null;
	private SequenceAcknowledgement sequenceAcknowledgement = null;
	private Sequence sequence = null;
	private TerminateSequence terminateSequence = null;

	public SOAPEnvelope toSoapEnvelop(SOAPEnvelope envelop) throws Exception {
		SOAPEnvelope env = envelop;

		if (createSequence != null) {
			createSequence.toSoapEnvelop(env);
		}
		if (createSequenceResponse != null) {
			createSequenceResponse.toSoapEnvelop(env);
		}
		if (ackRequest != null) {
			ackRequest.toSoapEnvelop(env);
		}
		if (sequenceAcknowledgement != null) {
			sequenceAcknowledgement.toSoapEnvelop(env);
		}
		if (sequence != null) {
			sequence.toSoapEnvelop(env);
		}
		if (terminateSequence != null) {
			terminateSequence.toSoapEnvelop(env);
		}

		return env;
	}
	public RMHeaders fromSOAPEnvelope(SOAPEnvelope env)
		throws AxisFault, SOAPException {
		if (env != null) {
			Iterator iterator = env.getHeaders().iterator();
			SOAPHeaderElement headerElement;
			while (iterator.hasNext()) {
				headerElement = (SOAPHeaderElement) iterator.next();
				//System.out.println(headerElement.getName());
				if (headerElement.getName().equals("Sequence")) {
					sequence = new Sequence();
					sequence.fromSOAPEnveploe(headerElement);
									
				}

				if (headerElement
					.getName()
					.equals("SequenceAcknowledgement")) {
					sequenceAcknowledgement = new SequenceAcknowledgement();
					sequenceAcknowledgement.fromSOAPEnveploe(headerElement);
				}
				if (headerElement.getName().equals("AckRequested")) {
					ackRequest = new AckRequested();
					ackRequest.fromSOAPEnveploe(headerElement);
				}
			}
			iterator = (Iterator) env.getBody().getChildElements();
			SOAPBodyElement bodyElement;
			while (iterator.hasNext()) {
				bodyElement = (SOAPBodyElement) iterator.next();
				if (bodyElement.getName().equals("CreateSequence")) {
					createSequence = new CreateSequence();
					createSequence.fromSOAPEnveploe(bodyElement);
				}

				if (bodyElement
					.getName()
					.equals("CreateSequenceResponse")) {
					createSequenceResponse = new CreateSequenceResponse();
					createSequenceResponse.fromSOAPEnveploe(bodyElement);
				}
				if (bodyElement.getName().equals("TerminateSequence")) {
					terminateSequence = new TerminateSequence();
					terminateSequence.fromSOAPEnveploe(bodyElement);
				}
			}
		}

		return this;

	}

	/**
	 * @return
	 */
	public CreateSequenceResponse getCreateSequenceResponse() {
		return createSequenceResponse;
	}

	/**
	 * @return
	 */
	public Sequence getSequence() {
		return sequence;
	}

	/**
	 * @return
	 */
	public SequenceAcknowledgement getSequenceAcknowledgement() {
		return sequenceAcknowledgement;
	}

	/**
	 * @return
	 */
	public TerminateSequence getTerminateSequence() {
		return terminateSequence;
	}

	/**
	 * @param sequence
	 */
	public void setCreateSequence(CreateSequence sequence) {
		createSequence = sequence;
	}

	/**
	 * @param response
	 */
	public void setCreateSequenceResponse(CreateSequenceResponse response) {
		createSequenceResponse = response;
	}

	/**
	 * @param sequence
	 */
	public void setSequence(Sequence sequence) {
		this.sequence = sequence;
	}

	/**
	 * @param acknowledgement
	 */
	public void setSequenceAcknowledgement(SequenceAcknowledgement acknowledgement) {
		sequenceAcknowledgement = acknowledgement;
	}

	/**
	 * @param sequence
	 */
	public void setTerminateSequence(TerminateSequence sequence) {
		terminateSequence = sequence;
	}

	/**
	 * @param requested
	 */
	public void setAckRequest(AckRequested requested) {
		ackRequest = requested;
	}

	/**
	 * @return
	 */
	public AckRequested getAckRequest() {
		return ackRequest;
	}

	/**
	 * @return
	 */
	public CreateSequence getCreateSequence() {
		return createSequence;
	}
	

}
