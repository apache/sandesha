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
package org.apache.sandesha;

import org.apache.axis.AxisFault;
import org.apache.axis.MessageContext;
import org.apache.axis.message.addressing.AddressingHeaders;
import org.apache.sandesha.ws.rm.RMHeaders;

import java.util.Iterator;

/**
 * @author JEkanayake
 */
public class RMMessageContext {

    private MessageContext msgContext;

    private String sequenceID;

    private String messageID;

    private AddressingHeaders addressingHeaders;

    private RMHeaders rmHeaders;

    private String outGoingAddress;

    private int messageType;

    private long reTransmissionCount = 0;

    private long lastPrecessedTime = 0;

    private long lastSentTime = 0;

    private boolean sync = false;

    private boolean hasResponse = false;

    private boolean lastMessage = false;

    private long msgNumber = 0;

    private String sourceURL = null;

    private String action = null;

    private String from = null;

    private String replyTo = null;

    private boolean responseReceived = false;

    private boolean ackReceived = false;

    /**
     * @return Returns the responseReceived.
     */
    public boolean isResponseReceived() {
        return responseReceived;
    }

    /**
     * @param responseReceived The responseReceived to set.
     */
    public void setResponseReceived(boolean responseReceived) {
        this.responseReceived = responseReceived;
    }

    /**
     * @return Returns the from.
     */
    public String getFrom() {
        return from;
    }

    /**
     * @param from The from to set.
     */
    public void setFrom(String from) {
        this.from = from;
    }

    /**
     * @return Returns the replyTo.
     */
    public String getReplyTo() {
        return replyTo;
    }

    /**
     * @param replyTo The replyTo to set.
     */
    public void setReplyTo(String replyTo) {
        this.replyTo = replyTo;
    }

    /**
     * @return Returns the action.
     */
    public String getAction() {
        return action;
    }

    /**
     * @param action The action to set.
     */
    public void setAction(String action) {
        this.action = action;
    }

    //This will be used to handle the relates to field.
    //When sending the response messages from the server
    private String oldSequenceID = null;

    /**
     * @return Returns the hasResponse.
     */
    public boolean isHasResponse() {
        return hasResponse;
    }

    /**
     * @param hasResponse The hasResponse to set.
     */
    public void setHasResponse(boolean hasResponse) {
        this.hasResponse = hasResponse;
    }

    /**
     * @return Returns the lastMessage.
     */
    public boolean isLastMessage() {
        return lastMessage;
    }

    /**
     * @param lastMessage The lastMessage to set.
     */
    public void setLastMessage(boolean lastMessage) {
        this.lastMessage = lastMessage;
    }

    /**
     * @return Returns the rmHeaders.
     */
    public RMHeaders getRmHeaders() {
        return rmHeaders;
    }


    /**
     * @return Returns the sourceURL.
     */
    public String getSourceURL() {
        return sourceURL;
    }

    /**
     * @param sourceURL The sourceURL to set.
     */
    public void setSourceURL(String sourceURL) {
        this.sourceURL = sourceURL;
    }

    /**
     * @return Returns the msgNumber.
     */
    public long getMsgNumber() {
        return msgNumber;
    }

    /**
     * @param msgNumber The msgNumber to set.
     */
    public void setMsgNumber(long msgNumber) {
        this.msgNumber = msgNumber;
    }

    /**
     * @return Returns the oldSequenceID.
     */
    public String getOldSequenceID() {
        return oldSequenceID;
    }

    /**
     * @param oldSequenceID The oldSequenceID to set.
     */
    public void setOldSequenceID(String oldSequenceID) {
        this.oldSequenceID = oldSequenceID;
    }

    public boolean isAckReceived() {
        return ackReceived;
    }

    public void setAckReceived(boolean ackReceived) {
        this.ackReceived = ackReceived;
    }

    /**
     * @return
     */
    public MessageContext getMsgContext() {
        return msgContext;
    }


    /**
     * @return
     */
    public String getSequenceID() {
        return sequenceID;
    }

    /**
     * @param context
     */
    public void setMsgContext(MessageContext msgContext) {
        this.msgContext = msgContext;
    }

    /**
     * @param object
     */
   

    /**
     * @param string
     */
    public void setSequenceID(String sequenceID) {
        this.sequenceID = sequenceID;
    }

    /**
     * @return
     */
    public String getMessageID() {
        return messageID;
    }

    /**
     * @param string
     */
    public void setMessageID(String string) {
        messageID = string;
    }

    /**
     * @return
     */
    public AddressingHeaders getAddressingHeaders() {
        return addressingHeaders;
    }

    /**
     * @return
     */
    public RMHeaders getRMHeaders() {
        return rmHeaders;
    }

    /**
     * @param headers
     */
    public void setAddressingHeaders(AddressingHeaders addressingHeaders) {
        this.addressingHeaders = addressingHeaders;
    }

    /**
     * @param headers
     */
    public void setRMHeaders(RMHeaders rmHeaders) {
        this.rmHeaders = rmHeaders;
    }

    /**
     * @return
     */
    public String getOutGoingAddress() {
        return outGoingAddress;
    }

    /**
     * @param string
     */
    public void setOutGoingAddress(String outGoingAddress) {
        this.outGoingAddress = outGoingAddress;
    }

    /**
     * @return
     */
    public int getMessageType() {
        return messageType;
    }

    /**
     * @param i
     */
    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }

    public void copyContents(RMMessageContext rmMsgContext) {
        if (addressingHeaders != null)
            rmMsgContext.setAddressingHeaders(this.addressingHeaders);
        if (messageID != null)
            rmMsgContext.setMessageID(this.messageID);
        if (rmHeaders != null)
            rmMsgContext.setRMHeaders(this.rmHeaders);
        if (sequenceID != null)
            rmMsgContext.setSequenceID(this.sequenceID);
        if (outGoingAddress != null)
            rmMsgContext.setOutGoingAddress(this.outGoingAddress);
        if (msgContext != null)
            rmMsgContext.setMsgContext(this.msgContext);
        rmMsgContext.setReTransmissionCount(this.reTransmissionCount);
        rmMsgContext.setLastPrecessedTime(this.lastPrecessedTime);
        rmMsgContext.setLastMessage(this.isLastMessage());

    }

    /**
     * @return
     */
    public long getLastPrecessedTime() {
        return lastPrecessedTime;
    }

    /**
     * @return
     */
    public long getReTransmissionCount() {
        return reTransmissionCount;
    }

    /**
     * @param l
     */
    public void setLastPrecessedTime(long lastPrecessedTime) {
        this.lastPrecessedTime = lastPrecessedTime;
    }

    /**
     * @param l
     */
    public void setReTransmissionCount(long reTransmissionCount) {
        this.reTransmissionCount = reTransmissionCount;
    }

    /**
     * This method will copy the contents of one MessageContext to another.
     *
     * @param msgContext1
     * @param msgContext2
     */
    public static void copyMessageContext(MessageContext msgContext1,
                                          MessageContext msgContext2) {

        try {
            if (msgContext1.getClassLoader() != null)
                msgContext2.setClassLoader(msgContext1.getClassLoader());

            //if (msgContext1.getCurrentMessage() != null)
            //    msgContext2.setCurrentMessage(msgContext1.getCurrentMessage());

            if (msgContext1.getEncodingStyle() != null)
                msgContext2.setEncodingStyle(msgContext1.getEncodingStyle());

            //if(msgContext1.isHighFidelity()!=null)
            msgContext2.setHighFidelity(msgContext1.isHighFidelity());

            //if(msgContext1.getMaintainSession()!=null)
            msgContext2.setMaintainSession(msgContext1.getMaintainSession());

            // if (msgContext1.getMessage() != null)
            //     msgContext2.setMessage(msgContext1.getMessage());

            // if (msgContext1.getOperation() != null)
            //     msgContext2.setOperation(msgContext1.getOperation());

            if (msgContext1.getPassword() != null)
                msgContext2.setPassword(msgContext1.getPassword());

            //if(msgContext1.getPastPivot()!=null)
            msgContext2.setPastPivot(msgContext1.getPastPivot());

            // if (msgContext1.getRequestMessage() != null)
            //     msgContext2.setRequestMessage(msgContext1.getRequestMessage());

            // if (msgContext1.getResponseMessage() != null)
            //     msgContext2
            //             .setResponseMessage(msgContext1.getResponseMessage());

            if (msgContext1.getRoles() != null)
                msgContext2.setRoles(msgContext1.getRoles());

            if (msgContext1.getSchemaVersion() != null)
                msgContext2.setSchemaVersion(msgContext1.getSchemaVersion());

            // if (msgContext1.getService() != null)
            //     msgContext2.setService(msgContext1.getService());

            if (msgContext1.getSession() != null)
                msgContext2.setSession(msgContext1.getSession());


            if (msgContext1.getSession() != null)
                msgContext2.setSession(msgContext1.getSession());

            if (msgContext1.getSOAPActionURI() != null)
                msgContext2.setSOAPActionURI(msgContext1.getSOAPActionURI());

            if (msgContext1.getSOAPConstants() != null)
                msgContext2.setSOAPConstants(msgContext1.getSOAPConstants());

            if (msgContext1.getTargetService() != null)
                msgContext2.setTargetService(msgContext1.getTargetService());

            //if(msgContext1.getTimeout()!=null)
            msgContext2.setTimeout(msgContext1.getTimeout());

            if (msgContext1.getTransportName() != null)
                msgContext2.setTransportName(msgContext1.getTransportName());

            if (msgContext1.getTypeMappingRegistry() != null)
                msgContext2.setTypeMappingRegistry(msgContext1
                        .getTypeMappingRegistry());

            if (msgContext1.getUsername() != null)
                msgContext2.setUsername(msgContext1.getUsername());

            //if(msgContext1.useSOAPAction()!=null)
            msgContext2.setUseSOAPAction(msgContext1.useSOAPAction());

            if (msgContext1.getAllPropertyNames() != null) {
                Iterator ite = msgContext1.getAllPropertyNames();

                while (ite.hasNext()) {
                    String str = (String) ite.next();
                    msgContext2.setProperty(str, msgContext1.getProperty(str));
                }
            }

        } catch (AxisFault e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public long getLastSentTime() {
        return lastSentTime;
    }

    public void setLastSentTime(long l) {
        lastSentTime = l;
    }

    /**
     * @param b
     */
    public void setSync(boolean sync) {
        this.sync = sync;

    }

    public boolean getSync() {

        return sync;
    }

}