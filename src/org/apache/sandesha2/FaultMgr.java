package org.apache.sandesha2;

import org.apache.sandesha2.wsrm.AckRequested;
import org.apache.sandesha2.wsrm.CreateSequence;
import org.apache.sandesha2.wsrm.CreateSequenceResponse;
import org.apache.sandesha2.wsrm.Sequence;
import org.apache.sandesha2.wsrm.Identifier;
import org.apache.sandesha2.wsrm.SequenceAcknowledgement;
import org.apache.sandesha2.wsrm.SequenceFault;
import org.apache.sandesha2.wsrm.FaultCode;
import org.apache.sandesha2.wsrm.TerminateSequence;
import org.apache.sandesha2.storage.AbstractBeanMgrFactory;
import org.apache.sandesha2.storage.beanmanagers.NextMsgBeanMgr;
import org.apache.sandesha2.storage.beanmanagers.RetransmitterBeanMgr;
import org.apache.sandesha2.storage.beanmanagers.SequencePropertyBeanMgr;
import org.apache.sandesha2.storage.beanmanagers.StorageMapBeanMgr;
import org.apache.sandesha2.storage.beans.RetransmitterBean;
import org.apache.sandesha2.storage.beans.SequencePropertyBean;
import org.apache.sandesha2.storage.beans.StorageMapBean;
import org.apache.sandesha2.util.SandeshaUtil;
import org.apache.axis2.soap.*;
import org.apache.axis2.soap.impl.llom.SOAPConstants;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.om.OMAttribute;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.AxisFault;

import javax.xml.namespace.QName;
import java.math.BigInteger;
import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: sanka
 * Date: Oct 9, 2005
 * Time: 11:10:07 PM
 * To change this template use File | Settings | File Templates.
 */
public class FaultMgr {

    private static final int SEQUENCE_FAULT = 10;
    private static final String WSA_ACTION_FAULT = "http://schemas.xmlsoap.org/ws/2004/08/addressing/fault";

    private static final int SEQUENCE_TYPE = 22;
    private static final int NON_SEQUENCE_TYPe = 23;
    
    public static final int UNKNOWN_SEQUENCE_TYPE = 1;
    public static final int MESSAGE_NUMBER_ROLLOVER_TYPE = 2;
    public static final int INVALID_ACKNOWLEDGEMENT_TYPE = 3;
    
    
    public static final String UNKNOW_SEQUENCE_CODE = "Sender";
    public static final String UNKNOW_SEQUENCE_SUBCODE 
            = "wsrm:UnknowSequence";
    public static final String UNKNOW_SEQUENCE_RESON 
            = "The vaule of wsrm:Identifier is not a known Sequence Identifer";
    
   public static final String  MESSAGE_NUMBER_ROLLOVER_CODE = "Sender";
   public static final String MESSAGE_NUMBER_ROLLOVER_SUBCODE 
           = "wsrm:MessageNumberRollover";
   public static final String MESSAGE_NUMBER_ROLLOVER_REASON 
           = "The maximum value of wsrm:MessageNumber has been exceeded.";
   
   public static final String INVALID_ACKNOWLEDGEMENT_CODE = "Sender";
   public static final String INVALID_ACKNOWLEDGEMENT_SUBCODE 
           = "wsrm:InvalidAcknowledgement";
   public static final String INVALID_ACKNOWLEDGEMENT_REASON 
           = "The SequenceAcknowledgement violates the cumlative acknowledgement";
   
   protected class FaultData {
        int type;
        String code;
        String subcode;
        String reason;  
        MessageContext msgCtx;
    }
    
    public FaultMgr() {
    }
    
    public RMMsgContext check(MessageContext msgCtx) throws SandeshaException {
        int msgType = getMessageType(msgCtx);
        
        switch (msgType) {
        
            case Constants.MessageTypes.APPLICATION:
            
                /* Sequence*/
                return checkSequence(msgCtx);
            
            case Constants.MessageTypes.ACK:
                
                /* SequenceAcknowledgement */
                return checkSequenceAcknowledgement(msgCtx);
            
            
                
                
                
        }
        return null;
    }
    
    public RMMsgContext checkSequence(MessageContext msgCtx) 
            throws SandeshaException {
        
        NextMsgBeanMgr mgr =
            AbstractBeanMgrFactory.getInstance(msgCtx).getNextMsgBeanMgr();
        SOAPEnvelope envelope = msgCtx.getEnvelope();
        
        OMElement element = envelope.getHeader().getFirstChildWithName(
                new QName(Constants.WSRM.NS_URI_RM, Constants.WSRM.SEQUENCE));
        OMElement identifier = element.getFirstChildWithName(
                new QName(Constants.WSRM.NS_URI_RM, Constants.WSRM.IDENTIFIER));

        String identifierString = identifier.getText().trim();
        
        if (mgr.retrieve(identifierString) == null) {
        
            // should throw an UnknownSequence fault
            return getFault(FaultMgr.UNKNOWN_SEQUENCE_TYPE, msgCtx);
        }

        OMElement msgNumber = element.getFirstChildWithName(
            new QName (Constants.WSRM.NS_URI_RM,Constants.WSRM.MSG_NUMBER));

        BigInteger bigInteger = new BigInteger(msgNumber.getText().trim());
        
        // throws new MessageNumberRollover fault
        if (bigInteger.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) == 1) {
            return getFault(FaultMgr.MESSAGE_NUMBER_ROLLOVER_TYPE,
                    msgCtx);
        }
        
        // Phew !! no faults ..
        return null;
    }
    
    public RMMsgContext checkSequenceAcknowledgement(MessageContext msgCtx) 
            throws SandeshaException {
        
        SOAPEnvelope envelope = msgCtx.getEnvelope();
        
        OMElement element = envelope.getFirstChildWithName(
                new QName(Constants.WSRM.NS_URI_RM, Constants.WSRM.SEQUENCE_ACK));
          
        // this is a SequenceAcknowledgement message
        OMElement identifierPart = element.getFirstChildWithName(
                new QName(Constants.WSRM.NS_URI_RM, Constants.WSRM.IDENTIFIER));
        NextMsgBeanMgr mgr =
            AbstractBeanMgrFactory.getInstance(msgCtx).getNextMsgBeanMgr();
        if (mgr.retrieve(identifierPart.getText()) == null) {
            //throw UnknownSequenceFault
            return getFault(FaultMgr.UNKNOWN_SEQUENCE_TYPE, msgCtx);
        }
        
        String identifierString = identifierPart.getText().trim();
        StorageMapBeanMgr smgr = 
                AbstractBeanMgrFactory.getInstance(msgCtx).getStorageMapBeanMgr();
        SequencePropertyBeanMgr propertyBeanMgr = 
                AbstractBeanMgrFactory.getInstance(msgCtx).getSequencePropretyBeanMgr();
        
        SequencePropertyBean propertyBean = 
            propertyBeanMgr.retrieve(identifierString, 
                    Constants.SequenceProperties.TEMP_SEQUENCE_ID);
        
        //TODO 
        String acksString = ""; //propertyBean.getAcksString();
        
        String[] msgNumberStrs = acksString.split(",");

        //TODO move this to a util class
        long[] msgNumbers = new long[msgNumberStrs.length];
        for (int i=0; i < msgNumbers.length; i++) {
            msgNumbers[i] = Long.parseLong(msgNumberStrs[i]);
        }
        
        Iterator acks = element.getChildrenWithName(new QName(
                Constants.WSRM.NS_URI_RM, Constants.WSRM.ACK_RANGE));
        while (acks.hasNext()) {
            OMElement ack = (OMElement) acks.next();

            OMAttribute lowerAttrib = ack.getAttribute(
                    new QName(Constants.WSRM.LOWER));
            long lower = Long.parseLong(lowerAttrib.getAttributeValue());

            OMAttribute upperAttrib = ack.getAttribute(
                    new QName(Constants.WSRM.UPPER));
            long upper = Long.parseLong(upperAttrib.getAttributeValue());
            
            for (; lower <= upper; lower++) {    
                boolean found = false;
                for (int j = 0; j < msgNumbers.length; j++) {
                    if (lower == msgNumbers[j]) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    //TODO throw new InvalidAcknowledgement   
                }
            }
        }
        
        Iterator nacks = element.getChildrenWithName(new QName(
                Constants.WSRM.NS_URI_RM, Constants.WSRM.NACK));
        
        while (nacks.hasNext()) {
            OMElement nack = (OMElement) nacks.next();
            long msgNo = Long.parseLong(nack.getText());
            
            boolean found = false;
            for (int j = 0; j < msgNumbers.length; j++) {
                if (msgNo == msgNumbers[j]) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                //TODO throw new InvalidAcknowledgement   
            }
        }        
        return null;
    }

    public RMMsgContext checkSequenceMessage(MessageContext msgCtx) 
            throws SandeshaException {

        SOAPEnvelope envelope = msgCtx.getEnvelope();
        OMElement element;
        
        element = envelope.getHeader().getFirstChildWithName(
                new QName(Constants.WSRM.NS_URI_RM, Constants.WSRM.SEQUENCE));
        
        if (element != null) {
            // this is a Sequence message
            OMElement identifier = element.getFirstChildWithName(
                new QName(Constants.WSRM.NS_URI_RM, Constants.WSRM.IDENTIFIER));

            String identifierString = identifier.getText().trim();
            NextMsgBeanMgr mgr =
                   AbstractBeanMgrFactory.getInstance(msgCtx).getNextMsgBeanMgr();

            if (mgr.retrieve(identifierString) == null) {
                // throws new UnknownSequence fault
                return getFault(FaultMgr.UNKNOWN_SEQUENCE_TYPE, msgCtx);
            }

            OMElement msgNumber = element.getFirstChildWithName(
				new QName (Constants.WSRM.NS_URI_RM,Constants.WSRM.MSG_NUMBER));

            BigInteger bigInteger = new BigInteger(msgNumber.getText().trim());
            
            // throws new MessageNumberRollover fault
            if (bigInteger.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) == 1) {
                return getFault(FaultMgr.MESSAGE_NUMBER_ROLLOVER_TYPE,
                        msgCtx);
            }
            
            return null;            
        } 
        
       
        element = envelope.getHeader().getFirstChildWithName(
                new QName(Constants.WSRM.NS_URI_RM, Constants.WSRM.SEQUENCE_ACK));
        
        if (element != null) {
            // this is a SequenceAcknowledgement message
            OMElement identifierPart = element.getFirstChildWithName(
                    new QName(Constants.WSRM.NS_URI_RM, Constants.WSRM.IDENTIFIER));
            NextMsgBeanMgr mgr =
                AbstractBeanMgrFactory.getInstance(msgCtx).getNextMsgBeanMgr();
            if (mgr.retrieve(identifierPart.getText()) == null) {
                //throw UnknownSequenceFault
                return getFault(FaultMgr.UNKNOWN_SEQUENCE_TYPE, msgCtx);
            }
            
            String identifierString = identifierPart.getText().trim();
            StorageMapBeanMgr smgr = 
                    AbstractBeanMgrFactory.getInstance(msgCtx).getStorageMapBeanMgr();
            SequencePropertyBeanMgr propertyBeanMgr = 
                    AbstractBeanMgrFactory.getInstance(msgCtx).getSequencePropretyBeanMgr();
            
            SequencePropertyBean propertyBean = 
                propertyBeanMgr.retrieve(identifierString, 
                        Constants.SequenceProperties.TEMP_SEQUENCE_ID);
            
            //TODO 
            String acksString = ""; //propertyBean.getAcksString();
            
            String[] msgNumberStrs = acksString.split(",");

            //TODO move this to a util class
            long[] msgNumbers = new long[msgNumberStrs.length];
            for (int i=0; i < msgNumbers.length; i++) {
                msgNumbers[i] = Long.parseLong(msgNumberStrs[i]);
            }
            
            Iterator acks = element.getChildrenWithName(new QName(
                    Constants.WSRM.NS_URI_RM, Constants.WSRM.ACK_RANGE));
            while (acks.hasNext()) {
                OMElement ack = (OMElement) acks.next();

                OMAttribute lowerAttrib = ack.getAttribute(
                        new QName(Constants.WSRM.LOWER));
                long lower = Long.parseLong(lowerAttrib.getAttributeValue());

                OMAttribute upperAttrib = ack.getAttribute(
                        new QName(Constants.WSRM.UPPER));
                long upper = Long.parseLong(upperAttrib.getAttributeValue());
                
                for (; lower <= upper; lower++) {    
                    boolean found = false;
                    for (int j = 0; j < msgNumbers.length; j++) {
                        if (lower == msgNumbers[j]) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        //TODO throw new InvalidAcknowledgement   
                    }
                }
            }
            
            Iterator nacks = element.getChildrenWithName(new QName(
                    Constants.WSRM.NS_URI_RM, Constants.WSRM.NACK));
            
            while (nacks.hasNext()) {
                OMElement nack = (OMElement) nacks.next();
                long msgNo = Long.parseLong(nack.getText());
                
                boolean found = false;
                for (int j = 0; j < msgNumbers.length; j++) {
                    if (msgNo == msgNumbers[j]) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    //TODO throw new InvalidAcknowledgement   
                }
            }
        }
        
        return null;
    }
    
    public RMMsgContext checkInvalidAcknowledge() {
        return null;
    }
    
    public RMMsgContext getFault(int type, MessageContext msgCtx) 
            throws SandeshaException {

        FaultData data = new FaultData();

        switch (type) {
            case FaultMgr.UNKNOWN_SEQUENCE_TYPE:
                data.type = FaultMgr.UNKNOWN_SEQUENCE_TYPE;
                data.code = FaultMgr.UNKNOW_SEQUENCE_CODE;
                data.subcode = FaultMgr.UNKNOW_SEQUENCE_SUBCODE;
                data.reason = FaultMgr.UNKNOW_SEQUENCE_RESON;   
                break;
            
            case FaultMgr.MESSAGE_NUMBER_ROLLOVER_TYPE:  
                data.type = FaultMgr.MESSAGE_NUMBER_ROLLOVER_TYPE;
                data.code = FaultMgr.MESSAGE_NUMBER_ROLLOVER_CODE;
                data.subcode = FaultMgr.MESSAGE_NUMBER_ROLLOVER_SUBCODE;
                data.reason = FaultMgr.MESSAGE_NUMBER_ROLLOVER_REASON;
                break;
            
            case FaultMgr.INVALID_ACKNOWLEDGEMENT_TYPE:
                data.type = FaultMgr.INVALID_ACKNOWLEDGEMENT_TYPE;
                data.code = FaultMgr.INVALID_ACKNOWLEDGEMENT_CODE;
                data.subcode = FaultMgr.INVALID_ACKNOWLEDGEMENT_SUBCODE;
                data.reason = FaultMgr.INVALID_ACKNOWLEDGEMENT_REASON;
                break;
        }
        
        MessageContext newMsgCtx = SandeshaUtil.shallowCopy(msgCtx);
//        newMsgCtx.setServiceGroupContextId(msgCtx.getServiceGroupContextId());
//        newMsgCtx.setServiceContext(msgCtx.getServiceContext());
        RMMsgContext newRMMsgCtx = new RMMsgContext(newMsgCtx);
        
        SequenceFault seqFault = new SequenceFault();
        //seqFault.setSubcode(data.subcode);
        //seqFault.setReason(data.reason);
        
        FaultCode faultCode = new FaultCode();
        //faultCode.setFault(data.code);
        seqFault.setFaultCode(faultCode);
        
        //TODO is it SEQUENCE_FAULT or just FAULT
        //newRMMsgCtx.setMessagePart(SEQUENCE_FAULT, seqFault);
        
        SOAPEnvelope inMsgEnvelope = msgCtx.getEnvelope();
        
        if (SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(
                msgCtx.getEnvelope().getNamespace().getName())) {
            doSOAP11Encoding(data, newMsgCtx);
        
        } else if (SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(
                msgCtx.getEnvelope().getNamespace().getName())) {
            doSOAP12Encoding(data, newMsgCtx);
        
        } else {
            //TODO should I throw an exception ?
        }
        
        return newRMMsgCtx;
    }
    

    public void doSOAP12Encoding(FaultData data, MessageContext msgCtx) {
        msgCtx.setProperty(AddressingConstants.WSA_ACTION, WSA_ACTION_FAULT);
        
        SOAPFactory factory = 
                SOAPAbstractFactory.getSOAPFactory(Constants.SOAPVersion.v1_2);
        SOAPEnvelope envelope = factory.getDefaultFaultEnvelope();
                
        SOAPFault fault = factory.createSOAPFault(envelope.getBody());
        SOAPFaultCode faultCode = factory.createSOAPFaultCode(fault);
        SOAPFaultValue faultValue = factory.createSOAPFaultValue(faultCode);
        faultValue.setText(data.code);
        SOAPFaultSubCode faultSubCode = factory.createSOAPFaultSubCode(faultCode);
        SOAPFaultValue faultValue2 = factory.createSOAPFaultValue(faultSubCode);
        faultValue2.setText(data.subcode);
        
        //TODO
        faultCode.setSubCode(faultSubCode);
        
        SOAPFaultReason faultReason = factory.createSOAPFaultReason(fault);
        SOAPFaultText faultText = factory.createSOAPFaultText(faultReason);
        faultText.setText(data.reason);
        
        SOAPFaultDetail faultDetail = factory.createSOAPFaultDetail(fault);
        SOAPHeader header = data.msgCtx.getEnvelope().getHeader();
        OMElement sequence = header.getFirstChildWithName(
                new QName(Constants.WSRM.NS_URI_RM, Constants.WSRM.SEQUENCE));
        OMElement identifier = sequence.getFirstChildWithName(
                new QName(Constants.WSRM.NS_URI_RM, Constants.WSRM.IDENTIFIER));
        identifier.detach();
        
        faultDetail.addChild(identifier);
        
    }

    public void doSOAP11Encoding(FaultData data, MessageContext msgCtx) 
            throws SandeshaException {

        SOAPFactory factory = SOAPAbstractFactory.getSOAPFactory(Constants.SOAPVersion.v1_1);
        SOAPEnvelope faultMsgEnvelope = factory.getDefaultFaultEnvelope();

        SOAPFault fault = factory.createSOAPFault(faultMsgEnvelope.getBody());
        SOAPFaultCode soapFaultCode = factory.createSOAPFaultCode(fault);
        SOAPFaultValue soapFaultValue = 
                factory.createSOAPFaultValue(soapFaultCode);
        SOAPFaultReason faultReason = factory.createSOAPFaultReason(fault);

        if (data.type == FaultMgr.UNKNOWN_SEQUENCE_TYPE) {
            soapFaultValue.setText(data.code);

        } else {
//          TODO sets the attribute xml:lang = "en" ..
            soapFaultValue.setText(data.subcode);
        }

        faultReason.setText(data.reason);
        
        try {
            msgCtx.setEnvelope(faultMsgEnvelope);
        } catch (AxisFault axisFault) {
            throw new SandeshaException(axisFault.getMessage());
        }
    }

//    public void doSOAP12Encoding(FaultData data, MessageContext msgCtx) 
//            throws SandeshaException {
//
//        SOAPFactory factory = 
//                SOAPAbstractFactory.getSOAPFactory(Constants.SOAPVersion.v1_2);
//        SOAPEnvelope faultMsgEnvelope = factory.getDefaultFaultEnvelope();
//        
//        SOAPFault fault = faultMsgEnvelope.getBody().getFault();
//        SOAPFaultCode soapFaultCode = fault.getCode();
//        soapFaultCode.getValue().setText(data.code);
//          
//
//        SOAPFaultSubCode soapFaultSubCode
//                = factory.createSOAPFaultSubCode(soapFaultCode, soapFaultCode.getBuilder());
//        SOAPFaultValue soapFaultValue = factory.createSOAPFaultValue(soapFaultSubCode);
//        soapFaultValue.setText(data.subcode);
//
//
//        fault.getReason().getSOAPText().setText(data.reason);   
//        SOAPEnvelope envelope = msgCtx.getEnvelope();
//                
//
//        try {
//            msgCtx.setEnvelope(faultMsgEnvelope);
//        } catch (AxisFault axisFault) {
//            throw new SandeshaException(axisFault.getMessage());
//        }
//    }
    
    public int getMessageType(MessageContext msgCtx) {
        
        SOAPHeader header = msgCtx.getEnvelope().getHeader();
        
        if (header.getFirstChildWithName(
                    new QName(Constants.WSRM.NS_URI_RM, Constants.WSRM.SEQUENCE)) 
            != null) {
            return Constants.MessageTypes.APPLICATION;
            
        } else if (header.getFirstChildWithName(
                    new QName(Constants.WSRM.NS_URI_RM, Constants.WSRM.SEQUENCE_ACK))
            != null) {
            return Constants.MessageTypes.ACK;
            
        } else if (header.getFirstChildWithName(
                    new QName(Constants.WSRM.NS_URI_RM, Constants.WSRM.CREATE_SEQUENCE))
            != null) {
            return Constants.MessageTypes.CREATE_SEQ;
            
        } else if (header.getFirstChildWithName(
                    new QName(Constants.WSRM.NS_URI_RM, Constants.WSRM.CREATE_SEQUENCE_RESPONSE))
            != null) {
            return Constants.MessageTypes.CREATE_SEQ_RESPONSE;
            
        } else if (header.getFirstChildWithName(
                    new QName(Constants.WSRM.NS_URI_RM, Constants.WSRM.TERMINATE_SEQUENCE))
            != null) {
            return Constants.MessageTypes.TERMINATE_SEQ;
        
        } else if (header.getFirstChildWithName(
                    new QName(Constants.WSRM.NS_URI_RM, Constants.WSRM.ACK_REQUESTED))
            != null) {
            //TODO fix this ..            
            return Constants.MessageTypes.UNKNOWN;
            
        } else {
            return Constants.MessageTypes.UNKNOWN;
        }
        
        
    }
    
    


    
}
