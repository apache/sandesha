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
import org.apache.axis.components.logger.LogFactory;
import org.apache.axis.message.addressing.AddressingHeaders;
import org.apache.sandesha.ws.rm.RMHeaders;
import org.apache.sandesha.util.PolicyLoader;
import org.apache.commons.logging.Log;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * RMMessageContext is used as the message context for Sandesha. It will carry all the required
 * paramerters through the processing logics including the MessageContext handed over by the
 * axis engine.
 *
 * @auther Jaliya Ekanayake
 * @auther Chamikara Jayalath
 */
public class RMMessageContext {

    private MessageContext msgContext;
    private String sequenceID;
    private String messageID;
    private AddressingHeaders addressingHeaders;
    private RMHeaders rmHeaders;
    private String outGoingAddress;
    private int messageType;
    private long reTransmissionCount;
    private long lastPrecessedTime;
    private long fristProcessedTime;
    private long retransmissionTime;

    private boolean locked=false;

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }


    public long getFristProcessedTime() {
        return fristProcessedTime;
    }

    public void setFristProcessedTime(long fristProcessedTime) {
        this.fristProcessedTime = fristProcessedTime;
    }

    public long getRetransmissionTime() {
        return retransmissionTime;
    }

    public void setRetransmissionTime(long retransmissionTime) {
        this.retransmissionTime = retransmissionTime;
    }

    private long lastSentTime;
    private boolean sync;
    private boolean hasResponse;
    private boolean lastMessage;
    private long msgNumber;
    private String sourceURL;
    private String action;
    private String from;
    private String replyTo;

    private boolean responseReceived;
    private boolean ackReceived;
    private String faultTo;
    private String acksTo = null;
    private String to;


    public RMMessageContext(){
        this.retransmissionTime=PolicyLoader.getInstance().getBaseRetransmissionInterval();
        this.fristProcessedTime=0;
    }


    public boolean isSendOffer() {
        return sendOffer;
    }

    public void setSendOffer(boolean sendOffer) {
        this.sendOffer = sendOffer;
    }

    private boolean sendOffer;

    private static final Log log = LogFactory.getLog(RMMessageContext.class.getName());
    private ArrayList msgIdList = new ArrayList();

    public void addToMsgIdList(String msgId) {
        msgIdList.add(msgId);
    }

    public ArrayList getMessageIdList() {
        return msgIdList;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getAcksTo() {
        return acksTo;
    }

    public void setAcksTo(String acksTo) {
        this.acksTo = acksTo;
    }

    /**
     * @param responseReceived The responseReceived to set.
     */
    public void setResponseReceived(boolean responseReceived) {
        this.responseReceived = responseReceived;
    }


    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getReplyTo() {
        return replyTo;
    }

    public void setReplyTo(String replyTo) {
        this.replyTo = replyTo;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    private String oldSequenceID;


    public boolean isHasResponse() {
        return hasResponse;
    }

    public void setHasResponse(boolean hasResponse) {
        this.hasResponse = hasResponse;
    }

    public boolean isLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(boolean lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getSourceURL() {
        return sourceURL;
    }

    public void setSourceURL(String sourceURL) {
        this.sourceURL = sourceURL;
    }

    public long getMsgNumber() {
        return msgNumber;
    }


    public void setMsgNumber(long msgNumber) {
        this.msgNumber = msgNumber;
    }

    public String getOldSequenceID() {
        return oldSequenceID;
    }

    public void setOldSequenceID(String oldSequenceID) {
        this.oldSequenceID = oldSequenceID;
    }

    public boolean isAckReceived() {
        return ackReceived;
    }

    public void setAckReceived(boolean ackReceived) {
        this.ackReceived = ackReceived;
    }

    public MessageContext getMsgContext() {
        return msgContext;
    }

    public String getSequenceID() {
        return sequenceID;
    }

    public void setMsgContext(MessageContext msgContext) {
        this.msgContext = msgContext;
    }

    public void setSequenceID(String sequenceID) {
        this.sequenceID = sequenceID;
    }

    public String getMessageID() {
        return messageID;
    }

    public void setMessageID(String string) {
        messageID = string;
    }

    public AddressingHeaders getAddressingHeaders() {
        return addressingHeaders;
    }

    public RMHeaders getRMHeaders() {
        return rmHeaders;
    }

    public void setAddressingHeaders(AddressingHeaders addressingHeaders) {
        this.addressingHeaders = addressingHeaders;
    }

    public void setRMHeaders(RMHeaders rmHeaders) {
        this.rmHeaders = rmHeaders;
    }

    public String getOutGoingAddress() {
        return outGoingAddress;
    }

    public void setOutGoingAddress(String outGoingAddress) {
        this.outGoingAddress = outGoingAddress;
    }


    public int getMessageType() {
        return messageType;
    }


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
        if (acksTo != null)
            rmMsgContext.setAcksTo(this.acksTo);
        if (to != null)
            rmMsgContext.setTo(this.to);

        rmMsgContext.setReTransmissionCount(this.reTransmissionCount);
        rmMsgContext.setLastPrecessedTime(this.lastPrecessedTime);
        rmMsgContext.setLastMessage(this.isLastMessage());

    }

    public long getLastPrecessedTime() {
        return lastPrecessedTime;
    }


    public long getReTransmissionCount() {
        return reTransmissionCount;
    }

    public void setLastPrecessedTime(long lastPrecessedTime) {
        this.lastPrecessedTime = lastPrecessedTime;
    }

    public void setReTransmissionCount(long reTransmissionCount) {
        this.reTransmissionCount = reTransmissionCount;
    }

    /**
     * This method will copy the contents of one MessageContext to another.
     *
     * @param msgContext1
     * @param msgContext2
     */
    public static void copyMessageContext(MessageContext msgContext1, MessageContext msgContext2) {

        try {
            if (msgContext1.getClassLoader() != null)
                msgContext2.setClassLoader(msgContext1.getClassLoader());

            if (msgContext1.getEncodingStyle() != null)
                msgContext2.setEncodingStyle(msgContext1.getEncodingStyle());

            if (msgContext1.getPassword() != null)
                msgContext2.setPassword(msgContext1.getPassword());

            if (msgContext1.getRoles() != null)
                msgContext2.setRoles(msgContext1.getRoles());

            if (msgContext1.getSchemaVersion() != null)
                msgContext2.setSchemaVersion(msgContext1.getSchemaVersion());

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

            if (msgContext1.getTransportName() != null)
                msgContext2.setTransportName(msgContext1.getTransportName());

            if (msgContext1.getTypeMappingRegistry() != null)
                msgContext2.setTypeMappingRegistry(msgContext1.getTypeMappingRegistry());

            if (msgContext1.getUsername() != null)
                msgContext2.setUsername(msgContext1.getUsername());

            if (msgContext1.getAllPropertyNames() != null) {
                Iterator ite = msgContext1.getAllPropertyNames();
                while (ite.hasNext()) {
                    String str = (String) ite.next();
                    msgContext2.setProperty(str, msgContext1.getProperty(str));
                }
            }

            msgContext2.setTimeout(msgContext1.getTimeout());
            msgContext2.setUseSOAPAction(msgContext1.useSOAPAction());
            msgContext2.setPastPivot(msgContext1.getPastPivot());
            msgContext2.setHighFidelity(msgContext1.isHighFidelity());
            msgContext2.setMaintainSession(msgContext1.getMaintainSession());

        } catch (AxisFault e) {
            log.error(e);
        }

    }

    public long getLastSentTime() {
        return lastSentTime;
    }

    public void setLastSentTime(long l) {
        lastSentTime = l;
    }


    public void setSync(boolean sync) {
        this.sync = sync;

    }

    public boolean getSync() {

        return sync;
    }

    public void setFaultTo(String faultTo) {
        this.faultTo = faultTo;
    }

    public String getFaultTo() {
        return this.faultTo;
    }
}