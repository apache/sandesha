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
import org.apache.axis.message.addressing.Action;
import org.apache.axis.message.addressing.AddressingHeaders;
import org.apache.axis.providers.java.RPCProvider;
import org.apache.commons.logging.Log;
import org.apache.sandesha.Constants;
import org.apache.sandesha.IStorageManager;
import org.apache.sandesha.RMMessageContext;
import org.apache.sandesha.client.ClientStorageManager;
import org.apache.sandesha.server.MessageValidator;
import org.apache.sandesha.server.RMMessageProcessorIdentifier;
import org.apache.sandesha.server.msgprocessors.FaultProcessor;
import org.apache.sandesha.server.msgprocessors.IRMMessageProcessor;
import org.apache.sandesha.storage.Callback;
import org.apache.sandesha.storage.CallbackData;
import org.apache.sandesha.ws.rm.RMHeaders;
import org.apache.sandesha.ws.rm.providers.RMProvider;

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

public class RMClientProvider extends RMProvider {


    public void processMessage(MessageContext msgContext, SOAPEnvelope reqEnv, SOAPEnvelope resEnv,
                               Object obj) throws Exception {
        super.setClient(true);
        super.processMessage(msgContext,reqEnv,resEnv,obj);

    }

}