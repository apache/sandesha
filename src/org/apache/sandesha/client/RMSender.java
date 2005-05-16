/*
* Copyright 1999-2004 The Apache Software Foundation.
*
* Licensed under the Apache License, Version 2.0 (the "License"); you may not
* use this file except in compliance with the License. You may obtain a copy of
* the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
* WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
* License for the specific language governing permissions and limitations under
* the License.
*
*/
package org.apache.sandesha.client;

import org.apache.axis.AxisFault;
import org.apache.axis.Message;
import org.apache.axis.MessageContext;
import org.apache.axis.client.Call;
import org.apache.axis.components.logger.LogFactory;
import org.apache.axis.components.uuid.UUIDGen;
import org.apache.axis.components.uuid.UUIDGenFactory;
import org.apache.axis.handlers.BasicHandler;
import org.apache.axis.message.addressing.AddressingHeaders;
import org.apache.commons.logging.Log;
import org.apache.sandesha.Constants;
import org.apache.sandesha.IStorageManager;
import org.apache.sandesha.RMInitiator;
import org.apache.sandesha.RMMessageContext;
import org.apache.sandesha.util.PolicyLoader;
import org.apache.sandesha.util.RMMessageCreator;
import org.apache.sandesha.ws.rm.RMHeaders;

/**
 * In the client side of axis there is a flexibility of using custom sender to send SOAP messages.
 * However axis's use of senders are mainly to handle transport related funtionalites.
 * <code>RMSender</code>has to be used by the users who wish to use WS-ReliableMessaging capability
 * in their clients.<P>
 * The main funtionality of <code>RMSender</code> is to insert the messages coming from client and
 * also the generated messages to the <code>SandeshaQueue</code>.
 * If the message coming in from the client is request/response in nature then <code>RMSender</code>
 * will wait polling <code>SandeshaQueue</code> till it gets an appropriate response.
 * Due to the above reason, if the client is sending several messages
 * (of request/response in nature) to be sent reliably, they will be sent reliably by
 * <b>Sandesha</b> however the client will wait at each message till it gets the response, to send
 * the next. To avoid this client can use callbacks provided by axis
 * in <code>org.apache.axis.client.async</code> package.
 */
public class RMSender extends BasicHandler {

    private IStorageManager storageManager;
    private static final Log log = LogFactory.getLog(RMSender.class.getName());
    private static final UUIDGen uuidGen = UUIDGenFactory.getUUIDGen();

    /**
     * This is the main method that is invoked by the axis engine. This method will add the reqest
     * messages from the client to <code>SandeshaQueue</code> with the generated messages such as
     * Create Sequence message and Terminate Sequence message.
     *
     * @param msgContext
     * @throws AxisFault
     */

    public void invoke(MessageContext msgContext) throws AxisFault {

        //Initialize the storage manager. We are in the client side So initialize the
        //Client Storage Manager.
        storageManager = new ClientStorageManager();

        try {
            RMMessageContext reqMsgCtx = getRMMessageContext(msgContext);
            String tempSeqID = reqMsgCtx.getSequenceID();

            long msgNo = reqMsgCtx.getMsgNumber();

            if (msgNo == 1) {
                reqMsgCtx = processFirstRequestMessage(reqMsgCtx, reqMsgCtx.getSync());
            } else {
                reqMsgCtx = processRequestMessage(reqMsgCtx);
            }

            if (reqMsgCtx.isLastMessage()) {
                storageManager.insertTerminateSeqMessage(
                        RMMessageCreator.createTerminateSeqMsg(reqMsgCtx, Constants.CLIENT));
            }

            if (reqMsgCtx.isHasResponse()) {
                RMMessageContext responseMessageContext = null;
                long startingTime = System.currentTimeMillis();
                long inactivityTimeOut = PolicyLoader.getInstance().getInactivityTimeout();
                while (responseMessageContext == null) {
                    responseMessageContext =
                            checkTheQueueForResponse(tempSeqID, reqMsgCtx.getMessageID());
                    if ((System.currentTimeMillis() - startingTime) >= inactivityTimeOut) {
                        RMInitiator.stopClientByForce();
                    }
                    Thread.sleep(Constants.CLIENT_RESPONSE_CHECKING_INTERVAL);
                }
                //We need these steps to filter all addressing and rm related headers.
                Message resMsg = responseMessageContext.getMsgContext().getRequestMessage();
                RMHeaders.removeHeaders(resMsg.getSOAPEnvelope());
                AddressingHeaders addHeaders = new AddressingHeaders(resMsg.getSOAPEnvelope(),
                        null, true, false, false, null);

                msgContext.setResponseMessage(resMsg);
            } else {
                msgContext.setResponseMessage(null);
            }

        } catch (Exception ex) {
            log.error(ex);
            throw new AxisFault(ex.getLocalizedMessage());

        }
    }

    /**
     * This method will process the first request message.
     *
     * @param reqRMMsgContext
     * @param sync
     * @return
     * @throws Exception
     */
    private RMMessageContext processFirstRequestMessage(RMMessageContext reqRMMsgContext,
                                                        boolean sync) throws Exception {
        String msgID = Constants.UUID + uuidGen.nextUUID();
        String offerID = null;
        if (reqRMMsgContext.isHasResponse()) {
            offerID = Constants.UUID + uuidGen.nextUUID();
            storageManager.addRequestedSequence(offerID);
            storageManager.addOffer(msgID, offerID);

        }

        RMMessageContext createSeqRMMsgContext = RMMessageCreator.createCreateSeqMsg(
                reqRMMsgContext, Constants.CLIENT, msgID, offerID);
        storageManager.addOutgoingSequence(reqRMMsgContext.getSequenceID());
        storageManager.setTemporaryOutSequence(reqRMMsgContext.getSequenceID(),
                createSeqRMMsgContext.getMessageID());

        createSeqRMMsgContext.setSync(sync);
        storageManager.addCreateSequenceRequest(createSeqRMMsgContext);
        RMMessageContext serviceRequestMsg = RMMessageCreator.createServiceRequestMessage(
                reqRMMsgContext);
        processRequestMessage(serviceRequestMsg);
        return reqRMMsgContext;
    }

    private RMMessageContext processRequestMessage(RMMessageContext reqRMMsgContext)
            throws Exception {
        RMMessageContext serviceRequestMsg = RMMessageCreator.createServiceRequestMessage(
                reqRMMsgContext);
        storageManager.insertOutgoingMessage(serviceRequestMsg);
        return reqRMMsgContext;
    }

    private RMMessageContext checkTheQueueForResponse(String sequenceId, String reqMessageID) {
        return storageManager.checkForResponseMessage(sequenceId, reqMessageID);
    }

    private RMMessageContext getRMMessageContext(MessageContext msgCtx) throws Exception {
        //Get a copy of the MessageContext. This is required when sending multiple messages from
        //one call object
        MessageContext newMsgContext = RMMessageCreator.cloneMsgContext(msgCtx);
        RMMessageContext requestMesssageContext = new RMMessageContext();
        Call call = (Call) newMsgContext.getProperty(MessageContext.CALL);

        requestMesssageContext = ClientPropertyValidator.validate(call);
        requestMesssageContext.setOutGoingAddress(
                (String) msgCtx.getProperty(MessageContext.TRANS_URL));
        requestMesssageContext.setMsgContext(newMsgContext);
        return requestMesssageContext;
    }


}



