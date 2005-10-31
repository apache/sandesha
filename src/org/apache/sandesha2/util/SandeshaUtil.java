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
package org.apache.sandesha2.util;

import java.awt.datatransfer.StringSelection;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.StringTokenizer;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.addressing.MessageInformationHeaders;
import org.apache.axis2.addressing.miheaders.RelatesTo;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;

import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.impl.MIMEOutputUtils;
import org.apache.axis2.om.impl.llom.builder.StAXBuilder;
import org.apache.axis2.om.impl.llom.builder.StAXOMBuilder;
import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.axis2.soap.SOAPFactory;
import org.apache.axis2.soap.impl.llom.builder.StAXSOAPModelBuilder;
import org.apache.axis2.soap.impl.llom.soap11.SOAP11Factory;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HTTPTransportUtils;
import org.apache.axis2.util.UUIDGenerator;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.sandesha2.Constants;
import org.apache.sandesha2.RMMsgContext;
import org.apache.sandesha2.SandeshaException;
import org.apache.sandesha2.msgreceivers.RMMessageReceiver;
import org.apache.sandesha2.workers.InOrderInvoker;
import org.apache.sandesha2.workers.Sender;
import org.apache.sandesha2.wsrm.AcknowledgementRange;

/**
 * @author chamikara
 * @author sanka
 */

public class SandeshaUtil {

	private static Hashtable storedMsgContexts = new Hashtable();

	private static Sender sender = new Sender();
	private static InOrderInvoker invoker = new InOrderInvoker ();

	public static String getUUID() {
		String uuid = "uuid:" + UUIDGenerator.getUUID();
		return uuid;
	}

	public static AcknowledgementRange[] getAckRangeArray(String msgNoStr) {
		String[] msgNoStrs = msgNoStr.split(",");
		long[] msgNos = getLongArr(msgNoStrs);

		long[] sortedMsgNos = sort(msgNos);

		int length = sortedMsgNos.length;
		if (length == 0)
			return null;
		//for (int i=0;i<length;i++)
		//	System.out.println (sortedMsgNos[i]);

		ArrayList ackRanges = new ArrayList();
		// upper = 0;
		long lower = sortedMsgNos[0];
		//long max = sortedMsgNos[sortedMsgNos.length];
		long temp = sortedMsgNos[0];

		for (long i = 1; i < length; i++) {
			int intI = (int) i;
			if ((sortedMsgNos[intI] == (temp + 1))
					|| (sortedMsgNos[intI] == (temp))) {
				temp = sortedMsgNos[intI];
				continue;
			}

			AcknowledgementRange ackRange = new AcknowledgementRange();
			ackRange.setLowerValue(lower);
			ackRange.setUpperValue(temp);
			ackRanges.add(ackRange);

			lower = sortedMsgNos[intI];
			temp = sortedMsgNos[intI];

		}

		AcknowledgementRange ackRange = new AcknowledgementRange();
		ackRange.setLowerValue(lower);
		ackRange.setUpperValue(temp);
		ackRanges.add(ackRange);

		Object[] objs = ackRanges.toArray();
		int l = objs.length;
		AcknowledgementRange[] ackRangeArr = new AcknowledgementRange[l];
		for (int i = 0; i < l; i++)
			ackRangeArr[i] = (AcknowledgementRange) objs[i];

		return ackRangeArr;
	}

	//	TODO remove int from folowing methods. (to make them truly Long :) )

	private static long[] sort(long[] input) {
		int length = input.length;

		long temp = 0;
		for (int i = 0; i < length; i++) {
			temp = 0;
			for (int j = i; j < length; j++) {
				if (input[j] < input[i]) {
					//swap
					temp = input[i];
					input[i] = input[j];
					input[j] = temp;
				}
			}
		}

		return input;
	}

	public static long[] getLongArr(String[] strings) {
		int length = strings.length;
		long[] longs = new long[length];
		for (int i = 0; i < length; i++) {
			longs[i] = Long.parseLong(strings[i]);
		}

		return longs;
	}

	public static String storeMessageContext(MessageContext ctx)
			throws SandeshaException {
		if (ctx == null)
			throw new SandeshaException("Stored Msg Ctx is null");

		String key = getUUID();
		storedMsgContexts.put(key, ctx);
		return key;
	}

	public static MessageContext getStoredMessageContext(String key) {
		return (MessageContext) storedMsgContexts.get(key);
	}

	//	public static void main (String[] args) {
	//		String msgList = "13,2,6,4,4,1,999,12,3";
	//		getAckRangeArray( msgList);
	//		
	//	}

	public static MessageContext deepCopy(MessageContext msgCtx)
			throws SandeshaException {

		try {
			MessageContext newMessageContext = shallowCopy(msgCtx);
			newMessageContext.setDoingMTOM(msgCtx.isDoingMTOM());
			newMessageContext.setDoingREST(msgCtx.isDoingREST());
			newMessageContext.setMessageID(getUUID());
			newMessageContext.setOutPutWritten(msgCtx.isOutPutWritten());
			newMessageContext.setParent(msgCtx.getParent());
			newMessageContext.setPausedPhaseName(msgCtx.getPausedPhaseName());
			newMessageContext.setProcessingFault(msgCtx.isProcessingFault());
			newMessageContext.setResponseWritten(msgCtx.isResponseWritten());
			newMessageContext.setRestThroughPOST(msgCtx.isRestThroughPOST());
			if (msgCtx.getAxisOperation()!=null)
				newMessageContext.setAxisOperation(msgCtx.getAxisOperation());

			if (msgCtx.getEnvelope() != null)
				newMessageContext.setEnvelope(msgCtx.getEnvelope());

			//copying transport info. TODO remove http specific ness.
			newMessageContext.setProperty(MessageContext.TRANSPORT_OUT, msgCtx
					.getProperty(MessageContext.TRANSPORT_OUT));
			newMessageContext.setProperty(HTTPConstants.HTTPOutTransportInfo,
					msgCtx.getProperty(HTTPConstants.HTTPOutTransportInfo));
			return newMessageContext;

		} catch (AxisFault e) {
			throw new SandeshaException("Cannot copy message");
		}
	}

	public static MessageContext shallowCopy(MessageContext msgCtx)
			throws SandeshaException {
		ConfigurationContext configCtx = msgCtx.getSystemContext();
		TransportInDescription transportIn = msgCtx.getTransportIn();
		TransportOutDescription transportOut = msgCtx.getTransportOut();
		MessageInformationHeaders msgInfoHeaders1 = new MessageInformationHeaders();
		MessageInformationHeaders oldMsgInfoHeaders = msgCtx
				.getMessageInformationHeaders();

		msgInfoHeaders1.setTo(oldMsgInfoHeaders.getTo());
		msgInfoHeaders1.setFrom(oldMsgInfoHeaders.getFrom());
		msgInfoHeaders1.setReplyTo(oldMsgInfoHeaders.getReplyTo());
		msgInfoHeaders1.setFaultTo(oldMsgInfoHeaders.getFaultTo());
		msgInfoHeaders1.setMessageId(getUUID());
		msgInfoHeaders1.setRelatesTo(oldMsgInfoHeaders.getRelatesTo());
		msgInfoHeaders1.setAction(oldMsgInfoHeaders.getAction());
		msgInfoHeaders1.setReferenceParameters(oldMsgInfoHeaders
				.getReferenceParameters());

		try {

			MessageContext newMessageContext = new MessageContext(configCtx);
			newMessageContext.setTransportIn(transportIn);
			newMessageContext.setTransportOut(transportOut);
			
			newMessageContext.setProperty(MessageContext.TRANSPORT_OUT, msgCtx
					.getProperty(MessageContext.TRANSPORT_OUT));
			newMessageContext.setProperty(HTTPConstants.HTTPOutTransportInfo,
					msgCtx.getProperty(HTTPConstants.HTTPOutTransportInfo));

			//Setting the charater set encoding
			Object charSetEncoding = msgCtx.getProperty(MessageContext.CHARACTER_SET_ENCODING);

			//TODO - this is required due to a bug in axis2. Remove this when it get fixed
			//BUG - Commons HTTP transport sender sets a NameValuepair to as the CHAR_SET_ENCODING 
			//instead of a String
			if (charSetEncoding instanceof NameValuePair)
				charSetEncoding = ((NameValuePair) charSetEncoding).getValue();
				
			newMessageContext
					.setProperty(MessageContext.CHARACTER_SET_ENCODING, charSetEncoding);

			newMessageContext.setMessageInformationHeaders(msgInfoHeaders1);
			newMessageContext.setAxisService(msgCtx
					.getAxisService());
			if (msgCtx.getAxisServiceGroup() != null)
				newMessageContext.setAxisServiceGroup(msgCtx
						.getAxisServiceGroup());

			newMessageContext.setSoapAction(msgCtx.getSoapAction());
			newMessageContext.setWSAAction(msgCtx.getWSAAction());

			newMessageContext.setServerSide(msgCtx.isServerSide());
			return newMessageContext;

		} catch (AxisFault e) {
			throw new SandeshaException("Cannot copy message");
		}

	}

	public static RMMsgContext deepCopy(RMMsgContext rmMsgContext)
			throws SandeshaException {
		MessageContext msgCtx = null;
		if (rmMsgContext.getMessageContext() != null)
			msgCtx = deepCopy(rmMsgContext.getMessageContext());

		RMMsgContext newRMMsgCtx = new RMMsgContext();
		if (msgCtx != null)
			newRMMsgCtx.setMessageContext(msgCtx);

		return newRMMsgCtx;
	}

	public static RMMsgContext shallowCopy(RMMsgContext rmMsgContext)
			throws SandeshaException {
		MessageContext msgCtx = null;
		if (rmMsgContext.getMessageContext() != null)
			msgCtx = shallowCopy(rmMsgContext.getMessageContext());

		RMMsgContext newRMMsgCtx = new RMMsgContext();
		if (msgCtx != null)
			newRMMsgCtx.setMessageContext(msgCtx);

		return newRMMsgCtx;
	}

	public static void startSenderIfStopped(ConfigurationContext context) {
		if (!sender.isSenderStarted()) {
			sender.start(context);
			System.out.println ("Sender started....");
		}
	}
	
	public static void startInvokerIfStopped(ConfigurationContext context) {
		if (!invoker.isInvokerStarted()) {
			invoker.start(context);
			System.out.println ("Invoker started....");
		}
	}

	public static boolean verifySequenceCompletion(Iterator ackRangesIterator,
			long lastMessageNo) {
		HashMap startMap = new HashMap();

		while (ackRangesIterator.hasNext()) {
			AcknowledgementRange temp = (AcknowledgementRange) ackRangesIterator
					.next();
			startMap.put(new Long(temp.getLowerValue()), temp);
		}

		long start = 1;
		boolean loop = true;
		while (loop) {
			AcknowledgementRange temp = (AcknowledgementRange) startMap
					.get(new Long(start));
			if (temp == null) {
				loop = false;
				continue;
			}

			if (temp.getUpperValue() >= lastMessageNo)
				return true;

			start = temp.getUpperValue() + 1;
		}

		return false;
	}
	
	public static SOAPEnvelope createSOAPMessage (MessageContext msgContext, String soapNamespaceURI) throws AxisFault {
        try {
        	
            InputStream inStream = (InputStream) msgContext.getProperty(
                    MessageContext.TRANSPORT_IN);
            msgContext.setProperty(MessageContext.TRANSPORT_IN, null);
            //this inputstram is set by the TransportSender represents a two way transport or
            //by a Transport Recevier
            if (inStream == null) {
                throw new AxisFault(Messages.getMessage("inputstreamNull"));
            }
            
            //This should be set later
            //TODO check weather this affects MTOM
            String contentType = null;

            StAXBuilder builder = null;
            SOAPEnvelope envelope = null;

            String charSetEnc = (String)msgContext.getProperty(MessageContext.CHARACTER_SET_ENCODING);
            if(charSetEnc == null) {
            	charSetEnc = MessageContext.DEFAULT_CHAR_SET_ENCODING;
            }
            
			if (contentType != null) {
                msgContext.setDoingMTOM(true);
                builder =
                        HTTPTransportUtils.selectBuilderForMIME(msgContext,
                                inStream,
                                (String) contentType);
                envelope = (SOAPEnvelope) builder.getDocumentElement();
            } else if (msgContext.isDoingREST()) {
                XMLStreamReader xmlreader =
                        XMLInputFactory.newInstance().createXMLStreamReader(
                                inStream,charSetEnc);
                SOAPFactory soapFactory = new SOAP11Factory();
                builder = new StAXOMBuilder(xmlreader);
                builder.setOmbuilderFactory(soapFactory);
                envelope = soapFactory.getDefaultEnvelope();
                envelope.getBody().addChild(builder.getDocumentElement());
            } else {
                XMLStreamReader xmlreader =
                        XMLInputFactory.newInstance().createXMLStreamReader(
                        		inStream,charSetEnc);
                builder = new StAXSOAPModelBuilder(xmlreader, soapNamespaceURI);
                envelope = (SOAPEnvelope) builder.getDocumentElement();
            }
            return envelope;
        } catch (Exception e) {
            throw new AxisFault(e);
        }

	}
	
	public static String getMessageTypeString (int messageType) {
		switch (messageType) {
		case Constants.MessageTypes.CREATE_SEQ:
			return "CreateSequence";
		case Constants.MessageTypes.CREATE_SEQ_RESPONSE:
			return "CreateSequenceResponse";
		case Constants.MessageTypes.ACK:
			return "Acknowledgement";
		case Constants.MessageTypes.APPLICATION:
			return "Application";
		case Constants.MessageTypes.TERMINATE_SEQ:
			return "TerminateSequence";
		case Constants.MessageTypes.UNKNOWN:
			return "Unknown";
		default:
			return "Error";	
		}
	}
	
	public static boolean isGloballyProcessableMessageType (int type) {
		if (type==Constants.MessageTypes.ACK || type==Constants.MessageTypes.TERMINATE_SEQ) {
			return true;
		}
		
		return false;
	}
	
	public static boolean isDuplicateDropRequiredMsgType (int rmMessageType) {
		if (rmMessageType==Constants.MessageTypes.APPLICATION)
			return true;
		
		if (rmMessageType==Constants.MessageTypes.CREATE_SEQ_RESPONSE)
			return true;
		
		return false;
	}
	
	//TODO: correct following to work for long.
	public static ArrayList getSplittedMsgNoArraylist (String str) {
		String[] splitted = str.split(",");
		ArrayList results = new ArrayList ();
		
		long count = splitted.length;
		for (int i=0;i<count;i++) {
			String s = splitted[i];
			results.add(s);
		}
		
		return results;
	}
	
	public static String getServerSideIncomingSeqIdFromInternalSeqId (String internalSequenceId) {
		String incomingSequenceId = internalSequenceId;
		return incomingSequenceId;
	}
	
	public static String getServerSideInternalSeqIdFromIncomingSeqId (String incomingSequenceId) {
		String internalSequenceId = incomingSequenceId;
		return internalSequenceId;
	}
}