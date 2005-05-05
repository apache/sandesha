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
package org.apache.sandesha.server;

import org.apache.axis.AxisFault;
import org.apache.axis.MessageContext;
import org.apache.axis.components.logger.LogFactory;
import org.apache.axis.message.addressing.AddressingHeaders;
import org.apache.commons.logging.Log;
import org.apache.sandesha.Constants;
import org.apache.sandesha.IStorageManager;
import org.apache.sandesha.RMMessageContext;
import org.apache.sandesha.client.ClientStorageManager;
import org.apache.sandesha.ws.rm.RMHeaders;
import org.apache.sandesha.ws.rm.Sequence;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;


public final class MessageValidator {
    private static IStorageManager storageMgr = null;
    private static final Log log = LogFactory.getLog(MessageValidator.class.getName());

    public static void validate(final RMMessageContext rmMsgContext, boolean client) throws AxisFault {

        if (client)
            storageMgr = new ClientStorageManager();
        else
            storageMgr = new ServerStorageManager();

        MessageContext msgContext = rmMsgContext.getMsgContext();
        try {
            AddressingHeaders addrHeaders=new AddressingHeaders(msgContext.getRequestMessage().getSOAPEnvelope());
            //ddressingHeaders addrHeaders = (AddressingHeaders) msgContext.getProperty(org.apache.axis.message.addressing.Constants.ENV_ADDRESSING_REQUEST_HEADERS);
            validateAddrHeaders(addrHeaders);
            rmMsgContext.setAddressingHeaders(addrHeaders);

            RMHeaders rmHeaders = new RMHeaders();
            rmHeaders.fromSOAPEnvelope(msgContext.getRequestMessage().getSOAPEnvelope());
            validateRMHeaders(rmHeaders);
            rmMsgContext.setRMHeaders(rmHeaders);

            validateForFaults(rmMsgContext);
        } catch (SOAPException e) {
            log.error(e);
            //TODO Do we need to throw a Sequence Fault at this level.
        } catch (Exception e) {
            log.error(e);
        }
    }


    private static void validateRMHeaders(RMHeaders rmHeaders) throws AxisFault {
        if (rmHeaders.getSequence() != null)
            return;
        if (rmHeaders.getAckRequest() != null)
            return;
        if (rmHeaders.getSequenceAcknowledgement() != null)
            return;
        if (rmHeaders.getTerminateSequence() != null)
            return;
        if (rmHeaders.getCreateSequence() != null)
            return;
        if (rmHeaders.getCreateSequenceResponse() != null)
            return;

        throw new AxisFault(new QName(Constants.FaultCodes.IN_CORRECT_MESSAGE), Constants.FaultMessages.NO_RM_HEADES, null, null);
    }

    private static void validateForFaults(RMMessageContext rmMsgCtx) throws AxisFault {
        RMHeaders rmHeaders = rmMsgCtx.getRMHeaders();
        Sequence sequence = rmHeaders.getSequence();

        if (sequence != null) {
            String seqId = sequence.getIdentifier().getIdentifier();
            if (!storageMgr.isRequestedSeqPresent(seqId)) {
                throw new AxisFault(new QName(Constants.FaultCodes.WSRM_FAULT_UNKNOWN_SEQUENCE), Constants.FaultMessages.UNKNOWN_SEQUENCE, null, null);
            }
            if (sequence.getMessageNumber() != null) {
                long msgNo = sequence.getMessageNumber().getMessageNumber();
                if (storageMgr.hasLastIncomingMsgReceived(sequence.getIdentifier().getIdentifier())) {
                    long lastMsg = storageMgr.getLastIncomingMsgNo(seqId);
                    if (msgNo > lastMsg)
                        throw new AxisFault(new QName(Constants.FaultCodes.WSRM_FAULR_LAST_MSG_NO_EXCEEDED), Constants.FaultMessages.LAST_MSG_NO_EXCEEDED, null, null);
                }
            }
        }
    }


    private static void validateAddrHeaders(AddressingHeaders addrHeaders) throws AxisFault {
        if (addrHeaders == null) {
            throw new AxisFault(new QName(Constants.FaultCodes.IN_CORRECT_MESSAGE), Constants.FaultMessages.NO_ADDRESSING_HEADERS, null, null);
        }
        //if (addrHeaders.getMessageID() == null)
        //    throw new AxisFault(new QName(Constants.FaultCodes.IN_CORRECT_MESSAGE), Constants.FaultMessages.NO_MESSAGE_ID, null, null);
    }
}
