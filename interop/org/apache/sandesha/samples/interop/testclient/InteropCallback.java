/*
 * Created on Apr 15, 2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.apache.sandesha.samples.interop.testclient;

import org.apache.sandesha.Constants;
import org.apache.sandesha.storage.Callback;
import org.apache.sandesha.storage.CallbackData;

/**
 * @author root
 *         <p/>
 *         To change the template for this generated type comment go to
 *         Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
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
                    result.getMessageId() +
                    "</font>";  // + result.getSequenceId() + " </font>";
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
                    result.getMessageId() +
                    "</font>";// + result.getSequenceId() + " </font>";
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
        String entry = "<br /><font color='red' size='2' > Error : " +  exp.getLocalizedMessage() +
                "</font>";// + result.getSequenceId() + " </font>";
        boolean b = writer.write(entry);
        if (!b)
            setTestFinished(true);

    }

}
