package org.apache.sandesha.server;

import org.apache.axis.AxisFault;
import org.apache.axis.MessageContext;
import org.apache.axis.message.addressing.AddressingHeaders;
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


    public static void validate(final RMMessageContext rmMsgContext, boolean client) throws AxisFault {

        if (client)
            storageMgr = new ClientStorageManager();
        else
            storageMgr = new ServerStorageManager();

        MessageContext msgContext = rmMsgContext.getMsgContext();
        try {
            AddressingHeaders addrHeaders = (AddressingHeaders) msgContext.getProperty(org.apache.axis.message.addressing.Constants.ENV_ADDRESSING_REQUEST_HEADERS);
            validateAddrHeaders(addrHeaders);
            rmMsgContext.setAddressingHeaders(addrHeaders);

            RMHeaders rmHeaders = new RMHeaders();
            rmHeaders.fromSOAPEnvelope(msgContext.getRequestMessage().getSOAPEnvelope());
            validateRMHeaders(rmHeaders);
            rmMsgContext.setRMHeaders(rmHeaders);

            validateForFaults(rmMsgContext);
        } catch (SOAPException e) {
            e.printStackTrace();
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
            if (!storageMgr.isSequenceExist(sequence.getIdentifier().getIdentifier()))
                throw new AxisFault(new QName(Constants.FaultCodes.WSRM_FAULT_UNKNOWN_SEQUENCE), Constants.FaultMessages.UNKNOWN_SEQUENCE, null, null);
        
        }


    }

    private static void validateAddrHeaders(AddressingHeaders addrHeaders) throws AxisFault {
        if (addrHeaders == null) {
            throw new AxisFault(new QName(Constants.FaultCodes.IN_CORRECT_MESSAGE), Constants.FaultMessages.NO_ADDRESSING_HEADERS, null, null);
        }

        if (addrHeaders.getMessageID() == null)
            throw new AxisFault(new QName(Constants.FaultCodes.IN_CORRECT_MESSAGE), Constants.FaultMessages.NO_MESSAGE_ID, null, null);
    }

}