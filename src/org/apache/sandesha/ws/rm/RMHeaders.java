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

package org.apache.sandesha.ws.rm;

import org.apache.axis.AxisFault;
import org.apache.axis.message.SOAPBodyElement;
import org.apache.axis.message.SOAPEnvelope;
import org.apache.axis.message.SOAPHeaderElement;

import javax.xml.soap.SOAPException;
import java.util.Iterator;

/**
 * class RMHeaders
 *
 * @author Amila Navarathna
 * @author Jaliya Ekanayaka
 * @author Sudar Nimalan
 */
public class RMHeaders {

    /**
     * Field createSequence
     */
    private CreateSequence createSequence = null;

    /**
     * Field createSequenceResponse
     */
    private CreateSequenceResponse createSequenceResponse = null;

    /**
     * Field ackRequest
     */
    private AckRequested ackRequest = null;

    /**
     * Field sequenceAcknowledgement
     */
    private SequenceAcknowledgement sequenceAcknowledgement = null;

    /**
     * Field sequence
     */
    private Sequence sequence = null;

    /**
     * Field terminateSequence
     */
    private TerminateSequence terminateSequence = null;

    /**
     * Method toSoapEnvelop
     *
     * @param envelope
     * @return SOAPEnvelope
     * @throws Exception
     */
    public SOAPEnvelope toSoapEnvelop(SOAPEnvelope envelope) throws Exception {

        SOAPEnvelope env = envelope;

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

    /**
     * Method fromSOAPEnvelope
     *
     * @param env
     * @return RMHeaders
     * @throws AxisFault
     */
    public RMHeaders fromSOAPEnvelope(SOAPEnvelope env) throws SOAPException, AxisFault {

        if (env != null) {
            Iterator iterator = env.getHeaders().iterator();
            SOAPHeaderElement headerElement;

            while (iterator.hasNext()) {
                headerElement = (SOAPHeaderElement) iterator.next();

                if (headerElement.getName().equals("Sequence")) {
                    sequence = new Sequence();
                    headerElement.setMustUnderstand(false);
                    sequence.fromSOAPEnveploe(headerElement);
                }

                if (headerElement.getName().equals("SequenceAcknowledgement")) {
                    sequenceAcknowledgement = new SequenceAcknowledgement();
                    headerElement.setMustUnderstand(false);
                    sequenceAcknowledgement.fromSOAPEnveploe(headerElement);
                }

                if (headerElement.getName().equals("AckRequested")) {
                    ackRequest = new AckRequested();
                    headerElement.setMustUnderstand(false);
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

                if (bodyElement.getName().equals("CreateSequenceResponse")) {
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
     * Method getCreateSequenceResponse
     *
     * @return CreateSequenceResponse
     */
    public CreateSequenceResponse getCreateSequenceResponse() {
        return createSequenceResponse;
    }

    /**
     * Method getSequence
     *
     * @return Sequence
     */
    public Sequence getSequence() {
        return sequence;
    }

    /**
     * Method getSequenceAcknowledgement
     *
     * @return SequenceAcknowledgement
     */
    public SequenceAcknowledgement getSequenceAcknowledgement() {
        return sequenceAcknowledgement;
    }

    /**
     * Method getTerminateSequence
     *
     * @return TerminateSequence
     */
    public TerminateSequence getTerminateSequence() {
        return terminateSequence;
    }

    /**
     * Method setCreateSequence
     *
     * @param sequence
     */
    public void setCreateSequence(CreateSequence sequence) {
        createSequence = sequence;
    }

    /**
     * Method setCreateSequenceResponse
     *
     * @param response
     */
    public void setCreateSequenceResponse(CreateSequenceResponse response) {
        createSequenceResponse = response;
    }

    /**
     * Method setSequence
     *
     * @param sequence
     */
    public void setSequence(Sequence sequence) {
        this.sequence = sequence;
    }

    /**
     * Method setSequenceAcknowledgement
     *
     * @param acknowledgement
     */
    public void setSequenceAcknowledgement(SequenceAcknowledgement acknowledgement) {
        sequenceAcknowledgement = acknowledgement;
    }

    /**
     * Method setSequenceAcknowledgement
     *
     * @param sequence
     */
    public void c(TerminateSequence sequence) {
        terminateSequence = sequence;
    }

    /**
     * Method setAckRequest
     *
     * @param requested
     */
    public void setAckRequest(AckRequested requested) {
        ackRequest = requested;
    }

    /**
     * Method getAckRequest
     *
     * @return AckRequested
     */
    public AckRequested getAckRequest() {
        return ackRequest;
    }

    /**
     * Method getCreateSequence
     *
     * @return CreateSequence
     */
    public CreateSequence getCreateSequence() {
        return createSequence;
    }
}