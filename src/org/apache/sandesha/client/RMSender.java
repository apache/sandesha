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
import org.apache.axis.MessageContext;
import org.apache.axis.handlers.BasicHandler;

public class RMSender extends BasicHandler {

    /**
     * Initialize the StorageManager Add the messsag to the queue and just
     * return Create SimpleAxisServer
     */
    public void invoke(MessageContext arg0) throws AxisFault {
        //Check whether we have messages or not in the queue.
        //If yes, just add
        //If no, need to add a priority message.
        //return.

    }

}