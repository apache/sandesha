package org.apache.sandesha.server;

import org.apache.axis.message.addressing.AddressingHeaders;
import org.apache.axis.MessageContext;
import org.apache.axis.AxisFault;
import org.apache.sandesha.ws.rm.RMHeaders;
import org.apache.sandesha.Constants;
import org.apache.sandesha.RMMessageContext;

import javax.xml.soap.SOAPException;
import javax.xml.namespace.QName;


public final class MessageValidator {


    public static void validate(final RMMessageContext rmMsgContext) throws AxisFault {

        MessageContext msgContext = rmMsgContext.getMsgContext();
        try {
            AddressingHeaders addrHeaders = (AddressingHeaders) msgContext.getProperty(org.apache.axis.message.addressing.Constants.ENV_ADDRESSING_REQUEST_HEADERS);
            validateAddrHeaders(addrHeaders);
            rmMsgContext.setAddressingHeaders(addrHeaders);

            RMHeaders rmHeaders = new RMHeaders();
            rmHeaders.fromSOAPEnvelope(msgContext.getRequestMessage().getSOAPEnvelope());
            validateRMHeaders(rmHeaders);
            rmMsgContext.setRMHeaders(rmHeaders);
        } catch (SOAPException e) {
            e.printStackTrace();
        }
    }

    private static void validateRMHeaders(RMHeaders rmHeaders) throws AxisFault {
        //Check for Fault scenarios
    }

    private static void validateAddrHeaders(AddressingHeaders addrHeaders) throws AxisFault {
        if (addrHeaders == null){
           throw new AxisFault(new QName(Constants.InvalidMessageErrors.IN_CORRECT_MESSAGE),Constants.AddressingHeadersValidationErrors.NO_ADDRESSING_HEADERS, null,null);
        }

        if (addrHeaders.getMessageID() == null)
            throw new AxisFault(Constants.AddressingHeadersValidationErrors.NO_MESSAGE_ID);
       }

 }
