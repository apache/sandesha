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
     * @param envelop 
     * @return 
     * @throws Exception 
     */
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

    /**
     * Method fromSOAPEnvelope
     * 
     * @param env 
     * @return 
     * @throws AxisFault     
     * @throws SOAPException 
     */
    public RMHeaders fromSOAPEnvelope(SOAPEnvelope env)
            throws AxisFault, SOAPException {

        if (env != null) {
            Iterator iterator = env.getHeaders().iterator();
            SOAPHeaderElement headerElement;

            while (iterator.hasNext()) {
                headerElement = (SOAPHeaderElement) iterator.next();

                // System.out.println(headerElement.getName());
                if (headerElement.getName().equals("Sequence")) {
                    sequence = new Sequence();

                    sequence.fromSOAPEnveploe(headerElement);
                }

                if (headerElement.getName().equals("SequenceAcknowledgement")) {
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
