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

import org.apache.axis.MessageContext;
import org.apache.axis.AxisFault;
import org.apache.axis.message.SOAPEnvelope;
import org.apache.axis.message.addressing.AddressingHeaders;
import org.apache.axis.providers.java.RPCProvider;
import org.apache.sandesha.IStorageManager;
import org.apache.sandesha.RMException;
import org.apache.sandesha.RMInitiator;
import org.apache.sandesha.RMMessageContext;
import org.apache.sandesha.server.IRMMessageProcessor;
import org.apache.sandesha.server.RMMessageProcessorIdentifier;
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

    private static boolean senderStarted = false;

    private static boolean client = false;

    /**
     * Method processMessage
     * 
     * @param msgContext
     * @param reqEnv
     * @param resEnv
     * @param obj
     * @throws Exception
     */

    public void processMessage(MessageContext msgContext, SOAPEnvelope reqEnv,
            SOAPEnvelope resEnv, Object obj) throws Exception {

        /** ********************************************************************* */
        System.out.println("RMProvider GOT SOAP REQUEST.....\n");
        // System.out.println(reqEnv.toString());
        //Initiates the StorageManager
        IStorageManager storageManager = RMInitiator.init(client);
        storageManager.init();

        //Get the addressing headers.
        AddressingHeaders addressingHeaders = null;
        addressingHeaders = (AddressingHeaders) msgContext
                .getProperty(org.apache.axis.message.addressing.Constants.ENV_ADDRESSING_REQUEST_HEADERS);

        //Get the RM headers
        RMHeaders rmHeaders = new RMHeaders();
        rmHeaders.fromSOAPEnvelope(reqEnv);

        //Set the RMMessageContext
        RMMessageContext rmMessageContext = new RMMessageContext();

        if (rmHeaders.getSequence() != null) {
            rmMessageContext.setSequenceID(rmHeaders.getSequence().getIdentifier().toString());

            if(rmHeaders.getSequence().getLastMessage()!=null){
               System.out.println("SETTING THE LAST MESSAGE");
               rmMessageContext.setLastMessage(true);
             }
        }

        if (addressingHeaders.getMessageID() != null) {
            rmMessageContext.setMessageID(addressingHeaders.getMessageID()
                    .toString());
            //System.out.println("MSG ID :
            // "+addressingHeaders.getMessageID().toString());
        }
        //This should be there in the final version.
        else {
            System.out
                    .println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~111");
            throw new RMException("MessageID should be present in the message.");
        }

        rmMessageContext.setMsgContext(msgContext);
        rmMessageContext.setAddressingHeaders(addressingHeaders);
        rmMessageContext.setRMHeaders(rmHeaders);

        new RMMessageProcessorIdentifier();
        IRMMessageProcessor rmMessageProcessor = RMMessageProcessorIdentifier
                .getMessageProcessor(rmMessageContext, storageManager);
        //Process message.
        try {
            if (!rmMessageProcessor.processMessage(rmMessageContext)) {
                msgContext.setResponseMessage(null);
            }
        } catch (RMException rmEx) {
            rmEx.printStackTrace();
            throw new AxisFault(rmEx.getStackTrace().toString());
            //TODO
            //throw a SOAPFault.
        }
    }

   //This is used by the Client to set the
   //set the side that the RMProvider is used.
    public void setClient(boolean client) {
        RMProvider.client = client;
    }

}