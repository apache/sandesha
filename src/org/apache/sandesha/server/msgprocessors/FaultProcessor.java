/*
 * Created on Sep 1, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.apache.sandesha.server.msgprocessors;

import org.apache.axis.AxisFault;
import org.apache.axis.Message;
import org.apache.axis.MessageContext;
import org.apache.axis.components.logger.LogFactory;
import org.apache.axis.message.SOAPFault;
import org.apache.axis.message.addressing.AddressingHeaders;
import org.apache.commons.logging.Log;
import org.apache.sandesha.IStorageManager;
import org.apache.sandesha.RMMessageContext;
import org.apache.sandesha.Constants;
import org.apache.sandesha.storage.dao.SandeshaQueueDAO;
import org.apache.sandesha.ws.rm.RMHeaders;

/**
 * @author JEkanayake
 *         <p/>
 *         To change the template for this generated type comment go to
 *         Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class FaultProcessor implements IRMMessageProcessor {
    private IStorageManager storageManager = null;
    private AxisFault axisFault = null;
    private static final Log log = LogFactory.getLog(SandeshaQueueDAO.class.getName());

    public FaultProcessor(IStorageManager storageManager) {
        this.storageManager = storageManager;
    }

    public FaultProcessor(IStorageManager storageManager, AxisFault axisFault) {
        this.storageManager = storageManager;
        this.axisFault = axisFault;
    }

    public IStorageManager getStorageManager() {
        return storageManager;
    }

    public void setStorageManager(IStorageManager storageManager) {
        this.storageManager = storageManager;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.sandesha.server.msgprocessors.IRMMessageProcessor#processMessage(org.apache.sandesha.RMMessageContext)
     */
    public boolean processMessage(RMMessageContext rmMessageContext) throws AxisFault {

        //Check the fault type.
        //Create the fault envelop
        //identify the target endpoin i.e whether async or sync
        //if sync, rmMessageContext.getmsgctx.setresopnes(new msg(fault));
        //return true
        //else inset to the storage
        //return false
        SOAPFault soapFault = null;

        /*
        if (Constants.FaultCodes.IN_CORRECT_MESSAGE.equalsIgnoreCase(axisFault.getFaultCode().getLocalPart())){
            soapFault = new SOAPFault(this.axisFault);
        }

         if (Constants.FaultCodes.WSRM_FAULT_UNKNOWN_SEQUENCE.equalsIgnoreCase(axisFault.getFaultCode().getLocalPart())){
            soapFault = new SOAPFault(this.axisFault);
        }

          if (Constants.FaultCodes.WSRM_FAULR_LAST_MSG_NO_EXCEEDED.equalsIgnoreCase(axisFault.getFaultCode().getLocalPart())){
            soapFault = new SOAPFault(this.axisFault);
        }
            */

        soapFault = new SOAPFault(this.axisFault);
        return sendFault(rmMessageContext, soapFault);


    }

    private boolean sendFault(RMMessageContext rmMessageContext, SOAPFault soapFault) {

        AddressingHeaders addrHeaders;
        RMHeaders rmHeaders;
        MessageContext msgContext = rmMessageContext.getMsgContext();

        if (rmMessageContext.getAddressingHeaders() != null) {
            addrHeaders = rmMessageContext.getAddressingHeaders();
            if (addrHeaders.getFaultTo() != null) {
                if (addrHeaders.getFaultTo().getAddress().toString().equals(Constants.WSA.NS_ADDRESSING_ANONYMOUS)) {
                    msgContext.setResponseMessage(new Message(soapFault));
                    return true;
                } else {
                    storageManager.insertFault(rmMessageContext);
                    return false;
                }
            } else if (addrHeaders.getReplyTo() != null) {
                if (addrHeaders.getReplyTo().getAddress().toString().equals(Constants.WSA.NS_ADDRESSING_ANONYMOUS)) {
                    msgContext.setResponseMessage(new Message(soapFault));
                    return true;
                } else {
                    storageManager.insertFault(rmMessageContext);
                    return false;
                }
            } else if (addrHeaders.getFrom() != null) {
                if (addrHeaders.getFrom().getAddress().toString().equals(Constants.WSA.NS_ADDRESSING_ANONYMOUS)) {
                    msgContext.setResponseMessage(new Message(soapFault));
                    return true;
                } else {
                    storageManager.insertFault(rmMessageContext);
                    return false;
                }
            }
        } else {
            FaultProcessor.log.error(this.axisFault);
            msgContext.setResponseMessage(new Message(soapFault));
            return true;
        }
        return true;
    }

}