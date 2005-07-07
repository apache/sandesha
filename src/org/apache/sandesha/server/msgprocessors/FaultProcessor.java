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
package org.apache.sandesha.server.msgprocessors;

import org.apache.axis.AxisFault;
import org.apache.axis.Message;
import org.apache.axis.MessageContext;
import org.apache.axis.components.logger.LogFactory;
import org.apache.axis.message.SOAPEnvelope;
import org.apache.axis.message.SOAPFault;
import org.apache.axis.message.addressing.AddressingHeaders;
import org.apache.commons.logging.Log;
import org.apache.sandesha.Constants;
import org.apache.sandesha.IStorageManager;
import org.apache.sandesha.RMMessageContext;
import org.apache.sandesha.storage.dao.SandeshaQueueDAO;
import org.apache.sandesha.ws.rm.RMHeaders;

import javax.xml.namespace.QName;

/**
 * This class is the message processor for the faults.
 *
 * @author Jaliy Ekanayake
 */
public class FaultProcessor implements IRMMessageProcessor {
    private IStorageManager storageManager;
    private AxisFault axisFault;
    private static final Log log = LogFactory.getLog(SandeshaQueueDAO.class.getName());

    public FaultProcessor(IStorageManager storageManager) {
        this.storageManager = storageManager;
    }

    public FaultProcessor(IStorageManager storageManager, AxisFault axisFault) {
        this.storageManager = storageManager;
        this.axisFault = axisFault;

    }

    public boolean processMessage(RMMessageContext rmMessageContext) throws AxisFault {
        axisFault = new AxisFault(new QName(Constants.FaultCodes.IN_CORRECT_MESSAGE),
                Constants.FaultMessages.INVALID_MESSAGE, null, null);
        try {
            return sendFault(rmMessageContext);
        } catch (Exception e) {
            log.error(e);
            return true;
        }

    }

    public boolean sendFault(RMMessageContext rmMessageContext) throws Exception {

        AddressingHeaders addrHeaders;
        RMHeaders rmHeaders;
        MessageContext msgContext = rmMessageContext.getMsgContext();

        if (rmMessageContext.getRMHeaders() != null) {
            rmHeaders = rmMessageContext.getRMHeaders();
            String acksTo = getAcksTo(rmHeaders);
            if (acksTo != null && acksTo.equals(Constants.WSA.NS_ADDRESSING_ANONYMOUS)) {
                return sendFaultSync(msgContext);
            } else if (acksTo != null) {
                storageManager.insertFault(rmMessageContext);
                return false;
            }
        }

        if (rmMessageContext.getAddressingHeaders() != null) {
            addrHeaders = rmMessageContext.getAddressingHeaders();
            if (addrHeaders.getFaultTo() != null) {
                if (addrHeaders.getFaultTo().getAddress().toString().equals(Constants.WSA.NS_ADDRESSING_ANONYMOUS)) {
                    return sendFaultSync(msgContext);
                } else {
                    storageManager.insertFault(rmMessageContext);
                    return false;
                }
            }
        } else {
            FaultProcessor.log.error(axisFault);
            return sendFaultSync(msgContext);
        }
        return true;
    }

    private boolean sendFaultSync(MessageContext msgContext) throws Exception {
        SOAPFault soapFault = new SOAPFault(axisFault);
        SOAPEnvelope sEnv = new SOAPEnvelope();
        sEnv.getBody().addChildElement(soapFault);
        msgContext.setResponseMessage(new Message(sEnv));
        return true;
    }

    private String getAcksTo(RMHeaders rmHeaders) {
        if (rmHeaders.getSequence() != null)
            return storageManager.getAcksTo(rmHeaders.getSequence().getIdentifier().getIdentifier());
        else if (rmHeaders.getCreateSequence() != null)
            return rmHeaders.getCreateSequence().getAcksTo().getAddress().toString();
        else
            return null;
    }

}