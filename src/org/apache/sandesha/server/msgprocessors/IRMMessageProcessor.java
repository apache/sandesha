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
import org.apache.sandesha.RMMessageContext;

/**
 * Interface for message processors. Message processors handle various types of messages.
 * Finding of the appropriate message processor is done by the
 * org.apache.sandesha.server.RMMessageProcessorIdentifier.
 *
 * @auther Jaliya Ekanayake
 */

public interface IRMMessageProcessor {

    //Returns true if the message has a synchronous response or ack.
    public boolean processMessage(RMMessageContext rmMessageContext) throws AxisFault;

}