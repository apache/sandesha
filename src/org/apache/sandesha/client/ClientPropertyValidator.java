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
package org.apache.sandesha.client;

import org.apache.axis.AxisFault;
import org.apache.axis.client.Call;
import org.apache.sandesha.Constants;
import org.apache.sandesha.RMMessageContext;

/**
 * @author Jaliya
 *  
 */
public class ClientPropertyValidator {

    public static RMMessageContext validate(Call call) throws AxisFault {

        RMMessageContext rmMessageContext = null;

        boolean sync = getSync(call);
        boolean hasRes = getHasResponse(call);
        long msgNumber = getMessageNumber(call);
        boolean lastMessage = getLastMessage(call);

        String sourceURL = null;
        if (!sync)
            sourceURL = getSourceURL(call);

        String errorMsg = getValidated(sync, hasRes, lastMessage, msgNumber,
                sourceURL);
        if (errorMsg == null) {
            rmMessageContext = new RMMessageContext();
            rmMessageContext.setSync(sync);
            rmMessageContext.setHasResponse(hasRes);
            rmMessageContext.setMsgNumber(msgNumber);
            rmMessageContext.setLastMessage(lastMessage);
            rmMessageContext.setSourceURL(sourceURL);
            return rmMessageContext;
        } else
            throw new AxisFault(errorMsg);

    }

    private static boolean getSync(Call call) {
        String synchronous = (String) call.getProperty("synchronous");
        boolean sync = false;
        if (synchronous != null) {
            if (synchronous.equals("true"))
                sync = true;
            else
                sync = false;
        } else
            sync = true;//If the user has not specified the synchronous
        // property sync=true
        return sync;
    }

    private static boolean getHasResponse(Call call) {
        String hasResponse = (String) call.getProperty("hasResponse");
        boolean hasRes = false;
        if (hasResponse != null) {
            if (hasResponse.equals("true"))
                hasRes = true;
            else
                hasRes = false;
        } else
            hasRes = false;//If the user has not specified the hasResponse
        // property hasRes=false
        return hasRes;

    }

    private static String getSourceURL(Call call) {
        Object temp = call.getProperty("sourceURI");
        String sourceURI = null;
        if (temp != null) {
            sourceURI = (String) temp;
            sourceURI = sourceURI + ":" + Constants.SOURCE_ADDRESS_PORT
                    + "/axis/services/MyService";
        }
        return sourceURI;
    }

    /**
     * @param call
     * @return
     */
    private static long getMessageNumber(Call call) {
        Object temp = call.getProperty("msgNumber");
        long msgNumber = 0;
        if (temp != null)
            msgNumber = ((Long) temp).longValue();
        return msgNumber;
    }

    private static boolean getLastMessage(Call call) {
        String lastMessage = (String) call.getProperty("lastMessage");
        boolean lastMsg = false;
        if (lastMessage != null) {
            if (lastMessage.equals("true"))
                lastMsg = true;
            else
                lastMsg = false;
        }
        return lastMsg;

    }

    private static String getValidated(boolean sync, boolean hasRes,
            boolean lastMsg, long msgNumber, String sourceURL) {
        String errorMsg = null;

        if (!sync && msgNumber == 0)
            errorMsg = "ERROR: Message Number Not Specified";
        else {
            if (sync && hasRes)
                errorMsg = "ERROR: Cannot Handle a Response Under Synchronus Messaging";

            if (!sync && sourceURL == null)
                errorMsg = "ERROR: Asynchronous Mode without Souceh URL";
        }
        return errorMsg;
    }

}