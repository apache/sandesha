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
import org.apache.axis.handlers.soap.SOAPService;
import org.apache.axis.message.SOAPEnvelope;
import org.apache.axis.message.addressing.AddressingHeaders;
import org.apache.axis.providers.java.RPCProvider;
import org.apache.commons.logging.Log;
import org.apache.sandesha.Constants;
import org.apache.sandesha.IStorageManager;
import org.apache.sandesha.RMInitiator;
import org.apache.sandesha.RMMessageContext;
import org.apache.sandesha.server.MessageValidator;
import org.apache.sandesha.server.RMMessageProcessorIdentifier;
import org.apache.sandesha.server.msgprocessors.FaultProcessor;
import org.apache.sandesha.server.msgprocessors.IRMMessageProcessor;
import org.apache.sandesha.storage.dao.SandeshaQueueDAO;
import org.apache.sandesha.ws.rm.RMHeaders;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

/**
 * class RMProvider
 *
 * @author Amila Navarathna
 * @author Jaliya Ekanayaka
 * @author Sudar Nimalan
 */

public class RMProvider extends RPCProvider {

    private boolean client;
    private static final Log log = LogFactory.getLog(RMProvider.class.getName());


    public void processMessage(MessageContext msgContext, SOAPEnvelope reqEnv, SOAPEnvelope resEnv, Object obj)
            throws Exception {

        RMProvider.log.info(Constants.InfomationMessage.PROVIDER_RECEIVED_MSG);
        //Some actions may need to be ignored. e.g.  http://schemas.xmlsoap.org/ws/2005/02/trust/RST/SCT
        //user can specify them in the server-config.wsdd as parameters to the service
        //parameter names should be in  ignoreAction1, ignoreAction2 format.

        if (isIgnorableMessage(msgContext)) {
            RPCProvider rpcProvider = new RPCProvider();
            rpcProvider.invoke(msgContext);

        } else {

            IStorageManager storageManager = RMInitiator.init(client);
            storageManager.init();

            RMMessageContext rmMessageContext = new RMMessageContext();
            rmMessageContext.setMsgContext(msgContext);
            try {
                MessageValidator.validate(rmMessageContext, client);
            } catch (AxisFault af) {
                FaultProcessor faultProcessor = new FaultProcessor(storageManager, af);

                if (!faultProcessor.processMessage(rmMessageContext)) {
                    msgContext.setResponseMessage(null);
                    return;
                }
                return;
            }

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
                } else {
                    // TODO Get the from envecreator

                    // SOAPEnvelope resEn=EnvelopeCreator.createAcknowledgementEnvelope()
                }
            } catch (AxisFault af) {
                RMProvider.log.error(af);

                FaultProcessor faultProcessor = new FaultProcessor(storageManager, af);

                if (!faultProcessor.processMessage(rmMessageContext)) {
                    msgContext.setResponseMessage(null);
                    return;
                }
                return;
            }

        }
    }

    //This is used by the Client to set the
    //set the side that the RMProvider is used.
    public void setClient(boolean client) {
        this.client = client;
    }

    private boolean isIgnorableMessage(MessageContext msgContext) throws Exception {
        boolean result = false;
        AddressingHeaders addrH = new AddressingHeaders(msgContext.getRequestMessage().getSOAPEnvelope());
        List lst = getIgnorableActions(msgContext);
        if (lst != null && addrH.getAction() != null) {
            Iterator ite = lst.iterator();
            while (ite.hasNext()) {
                String str = (String) ite.next();
                if (str.equals(addrH.getAction().toString()))
                    result = true;
            }
        }

        return result;
    }

    private List getIgnorableActions(MessageContext msgContext) {
        SOAPService soapService = msgContext.getService();
        Hashtable options = soapService.getOptions();
        Iterator ite = options.keySet().iterator();
        List actionList = new ArrayList();
        while (ite.hasNext()) {
            String key = (String) ite.next();

            if (key.regionMatches(0, Constants.IGNORE_ACTION, 0, Constants.IGNORE_ACTION.length()))
                actionList.add(options.get(key));
        }

        return actionList;
    }

}