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

import org.apache.axis.Message;
import org.apache.axis.MessageContext;
import org.apache.axis.components.uuid.UUIDGen;
import org.apache.axis.components.uuid.UUIDGenFactory;
import org.apache.axis.message.SOAPEnvelope;
import org.apache.axis.message.addressing.AddressingHeaders;
import org.apache.axis.message.addressing.Constants;
import org.apache.sandesha.EnvelopeCreator;
import org.apache.sandesha.IStorageManager;
import org.apache.sandesha.RMException;
import org.apache.sandesha.RMMessageContext;
import org.apache.sandesha.ws.rm.RMHeaders;

/**
 * @author
 */
public class CreateSequenceProcessor implements IRMMessageProcessor {

    IStorageManager storageManger = null;

    public CreateSequenceProcessor(IStorageManager storageManger) {
        this.storageManger = storageManger;
    }

    public boolean processMessage(RMMessageContext rmMessageContext) throws RMException {

        AddressingHeaders addrHeaders = rmMessageContext.getAddressingHeaders();
        RMHeaders rmHeaders = rmMessageContext.getRMHeaders();

        if (addrHeaders.getReplyTo() == null)
            rmMessageContext.setSync(true);
        else
            rmMessageContext.setSync(false);

        /*
         * We may let the user to decide on the UUID generation process. If
         * the user specify a method or service for generating UUIDs then
         * this request will be used to invoke that service and a UUID is
         * acquired. However if we want to do it, the user shoudl specify
         * the provider for that service as an parameter in the RM
         * Configuration. Currently the RMProvider will send create sequence
         * responses.
         */
        UUIDGen uuidGen = UUIDGenFactory.getUUIDGen();
        String uuid = uuidGen.nextUUID();

        //TODO
        //storageManger.addSequence("uuid:"+uuid);

        //Create the SOAPEnvelop to send and set it to the resEnv of
        // rmMessageCotext.
        SOAPEnvelope resEnvelope = EnvelopeCreator
                .createCreateSequenceResponseEnvelope(uuid,
                        rmMessageContext);

        //Set the message type.
        rmMessageContext
                .setMessageType(org.apache.sandesha.Constants.MSG_TYPE_CREATE_SEQUENCE_RESPONSE);

        //FIX THIS FIX THIS
        //Need to change the ANONYMOUS URI to the new one after completion.
        //We have some synchronous stuff here
        //TODO  Do we need to support the null replyZo case?
        //If the from is also missing, and the reply to is alos null then we will assume it is
        //synchornous.
        if (addrHeaders.getReplyTo() == null || addrHeaders.getReplyTo().getAddress().toString()
                .equals("http://schemas.xmlsoap.org/ws/2003/03/addressing/role/anonymous") || addrHeaders.getReplyTo().getAddress().toString()
                .equals(Constants.NS_URI_ANONYMOUS)) {

            //Inform that we have a synchronous response.
            rmMessageContext.getMsgContext().setResponseMessage(new Message(resEnvelope));
            rmMessageContext.setSync(true);
            return true;
        } else {

            MessageContext msgContext = new MessageContext(rmMessageContext
                    .getMsgContext().getAxisEngine());
            msgContext.setResponseMessage(new Message(resEnvelope));
            rmMessageContext.setMsgContext(msgContext);

            rmMessageContext.setOutGoingAddress(addrHeaders.getReplyTo()
                    .getAddress().toString());
            rmMessageContext.setSync(false);
            storageManger.addCreateSequenceResponse(rmMessageContext);
            return false;
        }


    }

}