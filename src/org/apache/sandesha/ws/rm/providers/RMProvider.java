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
package org.apache.sandesha.ws.rm.providers;

import org.apache.axis.AxisFault;
import org.apache.axis.MessageContext;
import org.apache.axis.components.logger.LogFactory;
import org.apache.axis.message.SOAPEnvelope;
import org.apache.axis.message.addressing.AddressingHeaders;
import org.apache.axis.providers.java.RPCProvider;
import org.apache.commons.logging.Log;
import org.apache.sandesha.IStorageManager;
import org.apache.sandesha.RMException;
import org.apache.sandesha.RMInitiator;
import org.apache.sandesha.RMMessageContext;
import org.apache.sandesha.server.MessageValidator;
import org.apache.sandesha.server.RMMessageProcessorIdentifier;
import org.apache.sandesha.server.msgprocessors.FaultProcessor;
import org.apache.sandesha.server.msgprocessors.IRMMessageProcessor;
import org.apache.sandesha.storage.dao.SandeshaQueueDAO;
import org.apache.sandesha.storage.queue.SandeshaQueue;
import org.apache.sandesha.ws.rm.RMHeaders;

/**
 * class RMProvider
 *
 * @author Amila Navarathna
 * @author Jaliya Ekanayaka
 * @author Sudar Nimalan
 */

public class RMProvider extends RPCProvider {

    private static boolean rmInvokerStarted = false;
    private static boolean senderStarted;
    private boolean client;
    private static final Log log = LogFactory.getLog(SandeshaQueueDAO.class.getName());


    public void processMessage(MessageContext msgContext, SOAPEnvelope reqEnv, SOAPEnvelope resEnv, Object obj)
            throws Exception {

        System.out.println("RMProvider Received a SOAP REQUEST.....\n");
        RMProvider.log.info("RMProvider Received a SOAP REQUEST");


        IStorageManager storageManager = RMInitiator.init(client);
        storageManager.init();
       
        RMMessageContext rmMessageContext = new RMMessageContext();
        rmMessageContext.setMsgContext(msgContext);
        try {
            MessageValidator.validate(rmMessageContext,client);
        } catch (AxisFault af) { 
        	FaultProcessor faultProcessor = new FaultProcessor(storageManager, af);
           
            if (!faultProcessor.processMessage(rmMessageContext)) {
                msgContext.setResponseMessage(null);
                return;
            }
            return;
        }

        System.out.println("VALIDATION IS PASSED .................................................");

        RMHeaders rmHeaders = rmMessageContext.getRMHeaders();
        AddressingHeaders addrHeaders = rmMessageContext.getAddressingHeaders();

        if (null != rmHeaders.getSequence()) {
            rmMessageContext.setSequenceID(rmHeaders.getSequence().getIdentifier().toString());
            if (null != rmHeaders.getSequence().getLastMessage()) {
                rmMessageContext.setLastMessage(true);
            }
        }

        rmMessageContext.setMessageID(addrHeaders.getMessageID().toString());
        IRMMessageProcessor rmMessageProcessor = RMMessageProcessorIdentifier.getMessageProcessor(rmMessageContext, storageManager);

        try {
            if (!rmMessageProcessor.processMessage(rmMessageContext)) {
                msgContext.setResponseMessage(null);
            }
         } catch (AxisFault af) {
            System.out.println("GGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGG");
            RMProvider.log.error(af);

        	FaultProcessor faultProcessor = new FaultProcessor(storageManager, af);

            if (!faultProcessor.processMessage(rmMessageContext)) {
                msgContext.setResponseMessage(null);
                return;
            }
            return;
        }
        
        SandeshaQueue sq = SandeshaQueue.getInstance();
        sq.displayIncomingMap();
        sq.displayOutgoingMap();
    }

    //This is used by the Client to set the
    //set the side that the RMProvider is used.
    public void setClient(boolean client) {
        this.client = client;
    }

}