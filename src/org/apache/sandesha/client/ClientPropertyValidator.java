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

import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;

import javax.xml.namespace.QName;

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
        
        boolean inOut=getInOut(call);
        long msgNumber = getMessageNumber(call);
        boolean lastMessage = getLastMessage(call);
        boolean sync=getSync(call);
        String action = getAction(call);
        String sourceURL = null;
        String from=getFrom(call);
        String replyTo=getReplyTo(call);
 
        try {
            sourceURL = getSourceURL(call);
            System.out.println("Souce URI " + sourceURL);
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            throw new AxisFault(e.getMessage());
        }

        String errorMsg = getValidated(msgNumber,action, replyTo,sync,inOut);
        if (errorMsg == null) {
            rmMessageContext = new RMMessageContext();
            //Assume that the service invocations with respect to client is done
            //in synchronus manner in all the cases. This has to be changed when 
            //a callback mechanism is introduced.
            //TODO
            rmMessageContext.setSync(sync);
            rmMessageContext.setHasResponse(inOut);
            rmMessageContext.setMsgNumber(msgNumber);
            rmMessageContext.setLastMessage(lastMessage);
            rmMessageContext.setSourceURL(sourceURL);
            rmMessageContext.setSequenceID(action);
            rmMessageContext.setReplyTo(replyTo);
            rmMessageContext.setFrom(from);
            return rmMessageContext;
        } else
            throw new AxisFault(errorMsg);

    }

    /**
     * This will decide whether we have an IN-OUT style service request
     * or IN-ONLY service request by checking the value of the QNane
     * returned by the call.getReturnType(). 
     * 
     * @param call
     * @return
     */
    private static boolean getInOut(Call call) {
        QName returnQName = (QName) call.getReturnType();
        if (returnQName != null)
            return true;
        else
            return false;

    }

    /**
     * @param call
     * @return
     * @throws URISyntaxException
     */
    private static String getAction(Call call) {
        String action=(String)call.getProperty("action");
         if(action!=null)
                return action;
       else
           return null;
        
    }

    private static boolean getSync(Call call) {
        Boolean synchronous = (Boolean) call.getProperty("sync");

        if (synchronous != null) {
           return synchronous.booleanValue();
        } else
            return  true;//If the user has not specified the synchronous
    }

  /*  private static boolean getHasResponse(Call call) {
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
*/
    private static String getSourceURL(Call call) throws UnknownHostException {
        String sourceURI = null;
        InetAddress addr = InetAddress.getLocalHost();
        
        sourceURI="http://"+addr.getHostAddress()+":" + Constants.SOURCE_ADDRESS_PORT
        + "/axis/services/RMService";
         
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
        Boolean lastMessage = (Boolean) call.getProperty("lastMessage");
        if(lastMessage!=null)        
        return lastMessage.booleanValue();
        else
        return false;

    }

    private static String getValidated(long msgNumber,String action, String replyTo, boolean sync, boolean inOut) {
        String errorMsg = null;
        
        if(sync && inOut && replyTo==null){
            errorMsg="ERROR: To perform the operation, ReplyTo address must be specified." +
            		" This EPR will not be the Sandesha end point. " +
            		"If it should be Sandesha end point, please set the propety 'sync' to false in call.";
            return errorMsg;   
        }
        
        if ((msgNumber == 0)||(action==null)){
            errorMsg = "ERROR: Message Number Not Specified or Action is null";
            return errorMsg;   
        }
        return errorMsg;  
    }
    
    
    private static String getFrom(Call call){
       return  (String)call.getProperty("from");
    
    }
    
    private static String getReplyTo(Call call){
        return  (String)call.getProperty("replyTo");
     
     }

}