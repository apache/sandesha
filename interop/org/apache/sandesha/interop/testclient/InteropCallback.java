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
package org.apache.sandesha.interop.testclient;

import org.apache.sandesha.Constants;
import org.apache.sandesha.storage.Callback;
import org.apache.sandesha.storage.CallbackData;

/**
 * This Callback is used to write the results of various points in the Sandesha engine.
 *
 * @auther Chamikara Jayalath
 */
public class InteropCallback extends Callback {

    private ResponseWriter writer = null;
    private boolean testFinished = false;

    public synchronized void setTestFinished(boolean finished) {
        this.testFinished = finished;
    }

    public synchronized boolean isTestFinished() {
        return testFinished;
    }

    public InteropCallback(ResponseWriter writer) {
        this.writer = writer;
    }

    public synchronized void onIncomingMessage(CallbackData result) {

        String action = result.getAction();
        String msgType = action;
        if (action != null) {
            if (action.equals(Constants.WSRM.ACTION_CREATE_SEQUENCE))
                msgType = "Create Sequence";
            else if (action.equals(Constants.WSRM.ACTION_CREATE_SEQUENCE_RESPONSE))
                msgType = "Create Sequence Response";
            else if (action.equals(Constants.WSRM.ACTION_TERMINATE_SEQUENCE))
                msgType = "Terminate Sequence";
            else if (action.equals(Constants.WSRM.SEQUENCE_ACKNOWLEDGEMENT_ACTION))
                msgType = "Sequence Acknowledgement";
        } else {
            msgType = "";

        }

        String entry = "";
        if (result.getMessageId() != null && result.getMessageId() != "")
            entry = "<br /><font color='green' size='2' > Received " + msgType + " Message. ID : " +
                    result.getMessageId() + "</font>";  // + result.getSequenceId() + " </font>";
        else
            entry = "<br /><font color='green' size='2' > Received " + msgType + "</font>";


        boolean b = writer.write(entry);
        if (!b)
            setTestFinished(true);
    }

    public synchronized void onOutgoingMessage(CallbackData result) {

        int type = result.getMessageType();
        String msgType = null;

        if (type == Constants.MSG_TYPE_CREATE_SEQUENCE_REQUEST)
            msgType = "Create Sequence";
        else if (type == Constants.MSG_TYPE_CREATE_SEQUENCE_RESPONSE)
            msgType = "Create Sequence Response";
        else if (type == Constants.MSG_TYPE_TERMINATE_SEQUENCE)
            msgType = "Terminate Sequence";
        else if (type == Constants.MSG_TYPE_ACKNOWLEDGEMENT)
            msgType = "Sequence Acknowledgement";
        else if (type == Constants.MSG_TYPE_SERVICE_REQUEST)
            msgType = "Service Request";
        else if (type == Constants.MSG_TYPE_SERVICE_RESPONSE)
            msgType = "Service Response";

        String entry = "";
        if (result.getMessageId() != null && result.getMessageId() != "")
            entry = "<br /><font color='blue' size='2' > Sent " + msgType + " Message. ID : " +
                    result.getMessageId() + "</font>";// + result.getSequenceId() + " </font>";
        else {
            entry = "<br /><font color='blue' size='2' > Sent " + msgType + "</font>";
        }

        boolean b = writer.write(entry);
        if (!b)
            setTestFinished(true);

        if (result.getMessageType() == 6)
            setTestFinished(true);

    }

    public synchronized void onError(Exception exp) {
        String message = "Error Occured During the Interop Test";
        if (exp.getMessage() != null) {
            message = exp.getMessage();
        }
        String entry = "<br /><font color='red' size='2' > Error : " + message + "</font>";
        boolean b = writer.write(entry);
        if (!b)
            setTestFinished(true);
    }

}
