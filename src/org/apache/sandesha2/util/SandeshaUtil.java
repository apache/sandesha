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

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.StringTokenizer;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.context.ServiceGroupContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.impl.llom.builder.StAXBuilder;
import org.apache.axis2.om.impl.llom.builder.StAXOMBuilder;
import org.apache.axis2.soap.SOAP11Constants;
import org.apache.axis2.soap.SOAP12Constants;
import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.axis2.soap.SOAPFactory;
import org.apache.axis2.soap.SOAPHeader;
import org.apache.axis2.soap.impl.llom.builder.StAXSOAPModelBuilder;
import org.apache.axis2.soap.impl.llom.soap11.SOAP11Factory;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HTTPTransportUtils;
import org.apache.axis2.util.UUIDGenerator;
import org.apache.axis2.util.Utils;
import org.apache.sandesha2.Constants;
import org.apache.sandesha2.RMMsgContext;
import org.apache.sandesha2.SandeshaException;
import org.apache.sandesha2.storage.StorageManager;
import org.apache.sandesha2.workers.InOrderInvoker;
import org.apache.sandesha2.workers.Sender;
import org.apache.sandesha2.wsrm.AcknowledgementRange;

/**
 * Contains utility methods that are used in many plases of Sandesha2.
 * 
 * @author Chamikara Jayalath <chamikaramj@gmail.com>
 */

public class SandeshaUtil {

	private static Hashtable storedMsgContexts = new Hashtable();

	private static StorageManager storageManager = null;

	private static Sender sender = new Sender();

	private static InOrderInvoker invoker = new InOrderInvoker();

	/**
	 * Create a new UUID.
	 * 
	 * @return
	 */
	public static String getUUID() {
		String uuid = "uuid:" + UUIDGenerator.getUUID();
		return uuid;
	}

	/**
	 * Used to convert a message number list (a comma seperated list of message numbers) into
	 * a set of AcknowledgementRanges. This breaks the list, sort the items and group them to create
	 * the AcknowledgementRange objects.
	 * 
	 * @param msgNoStr
	 * @param factory
	 * @return
	 * @throws SandeshaException
	 */
	public static ArrayList getAckRangeArrayList(String msgNoStr,
			SOAPFactory factory) throws SandeshaException {

		ArrayList ackRanges = new ArrayList();

		StringTokenizer tokenizer = new StringTokenizer(msgNoStr, ",");
		ArrayList sortedMsgNoArrayList = getSortedMsgNoArrayList(tokenizer);

		Iterator iterator = sortedMsgNoArrayList.iterator();
		long lower = 0;
		long upper = 0;
		boolean completed = true;

		while (iterator.hasNext()) {
			Long tempLng = (Long) iterator.next();
			long temp = tempLng.longValue();
			if (lower == 0) {
				lower = temp;
				upper = temp;
				completed = false;
			} else if (temp == (upper + 1)) {
				upper = temp;
				completed = false;
			} else {
				//add ackRange (lower,upper)
				AcknowledgementRange ackRange = new AcknowledgementRange(
						factory);
				ackRange.setLowerValue(lower);
				ackRange.setUpperValue(upper);
				ackRanges.add(ackRange);

				lower = temp;
				upper = temp;
				completed = false;
			}
		}

		if (!completed) {
			AcknowledgementRange ackRange = new AcknowledgementRange(factory);
			ackRange.setLowerValue(lower);
			ackRange.setUpperValue(upper);
			ackRanges.add(ackRange);
			completed = true;
		}

		return ackRanges;
	}

	private static ArrayList getSortedMsgNoArrayList(StringTokenizer tokenizer)
			throws SandeshaException {
		ArrayList msgNubers = new ArrayList();

		while (tokenizer.hasMoreElements()) {
			String temp = tokenizer.nextToken();

			try {
				long msgNo = Long.parseLong(temp);
				msgNubers.add(new Long(msgNo));
			} catch (Exception ex) {
				throw new SandeshaException("Invalid msg number list");
			}
		}

		ArrayList sortedMsgNumberList = sort(msgNubers);
		return sortedMsgNumberList;
	}

	public static ArrayList sort(ArrayList list) {

		ArrayList sortedList = new ArrayList();

		long max = 0;
		Iterator it1 = list.iterator();
		while (it1.hasNext()) {
			Long tempLng = (Long) it1.next();
			long temp = tempLng.longValue();
			if (temp > max)
				max = temp;
		}

		int item = 0;
		for (long i = 1; i <= max; i++) {
			Long temp = new Long(i);
			if (list.contains(temp)) {
				sortedList.add(item, temp);
				item++;
			}
		}

		return sortedList;
	}

	/**
	 * Used to store message context objects. Currently they are stored in a in-memory HashMap.
	 * Returned key can be used to retrieve the message context.
	 * 
	 * @param ctx
	 * @return
	 * @throws SandeshaException
	 */
	public static String storeMessageContext(MessageContext ctx)
			throws SandeshaException {
		if (ctx == null)
			throw new SandeshaException("Stored Msg Ctx is null");

		String key = getUUID();
		storedMsgContexts.put(key, ctx);
		return key;
	}

	/**
	 * Retrieve the MessageContexts saved by the above method.
	 * 
	 * @param key
	 * @return
	 */
	public static MessageContext getStoredMessageContext(String key) {
		return (MessageContext) storedMsgContexts.get(key);
	}

	public static void startSenderIfStopped(ConfigurationContext context) {
		if (!sender.isSenderStarted()) {
			sender.start(context);
		}
	}

	public static void startInvokerIfStopped(ConfigurationContext context) {
		if (!invoker.isInvokerStarted()) {
			invoker.start(context);
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

	public static SOAPEnvelope createSOAPMessage(MessageContext msgContext,
			String soapNamespaceURI) throws AxisFault {
		try {

			InputStream inStream = (InputStream) msgContext
					.getProperty(MessageContext.TRANSPORT_IN);
			msgContext.setProperty(MessageContext.TRANSPORT_IN, null);
			//this inputstram is set by the TransportSender represents a two
			// way transport or
			//by a Transport Recevier
			if (inStream == null) {
				throw new AxisFault(Messages.getMessage("inputstreamNull"));
			}

			String contentType = null;

			StAXBuilder builder = null;
			SOAPEnvelope envelope = null;

			String charSetEnc = (String) msgContext
					.getProperty(MessageContext.CHARACTER_SET_ENCODING);
			if (charSetEnc == null) {
				charSetEnc = MessageContext.DEFAULT_CHAR_SET_ENCODING;
			}

			if (contentType != null) {
				msgContext.setDoingMTOM(true);
				builder = HTTPTransportUtils.selectBuilderForMIME(msgContext,
						inStream, (String) contentType);
				envelope = (SOAPEnvelope) builder.getDocumentElement();
			} else if (msgContext.isDoingREST()) {
				XMLStreamReader xmlreader = XMLInputFactory.newInstance()
						.createXMLStreamReader(inStream, charSetEnc);
				SOAPFactory soapFactory = new SOAP11Factory();
				builder = new StAXOMBuilder(xmlreader);
				builder.setOmbuilderFactory(soapFactory);
				envelope = soapFactory.getDefaultEnvelope();
				envelope.getBody().addChild(builder.getDocumentElement());
			} else {
				XMLStreamReader xmlreader = XMLInputFactory.newInstance()
						.createXMLStreamReader(inStream, charSetEnc);
				builder = new StAXSOAPModelBuilder(xmlreader, soapNamespaceURI);
				envelope = (SOAPEnvelope) builder.getDocumentElement();
			}
			return envelope;
		} catch (Exception e) {
			throw new AxisFault(e);
		}

	}

	public static String getMessageTypeString(int messageType) {
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

	public static boolean isGloballyProcessableMessageType(int type) {
		if (type == Constants.MessageTypes.ACK
				|| type == Constants.MessageTypes.TERMINATE_SEQ) {
			return true;
		}

		return false;
	}

	public static boolean isDuplicateDropRequiredMsgType(int rmMessageType) {
		if (rmMessageType == Constants.MessageTypes.APPLICATION)
			return true;

		if (rmMessageType == Constants.MessageTypes.CREATE_SEQ_RESPONSE)
			return true;

		return false;
	}

	public static ArrayList getSplittedMsgNoArraylist(String str) {

		StringTokenizer tokenizer = new StringTokenizer(str, ",");

		ArrayList results = new ArrayList();

		while (tokenizer.hasMoreTokens()) {
			results.add(tokenizer.nextToken());
		}

		return results;
	}

	public static String getServerSideIncomingSeqIdFromInternalSeqId(
			String internalSequenceId) {
		String incomingSequenceId = internalSequenceId;
		return incomingSequenceId;
	}

	public static String getServerSideInternalSeqIdFromIncomingSeqId(
			String incomingSequenceId) {
		String internalSequenceId = incomingSequenceId;
		return internalSequenceId;
	}

	/**
	 * Used to obtain the storage Manager Implementation. 
	 * @param context
	 * @return
	 * @throws SandeshaException
	 */
	public static StorageManager getSandeshaStorageManager(
			ConfigurationContext context) throws SandeshaException {
		
		String srotageManagerClassStr = PropertyManager.getInstance().getStorageManagerClass();

		if (storageManager != null)
			return storageManager;

		try {
			Class c = Class.forName(srotageManagerClassStr);
			Class configContextClass = Class.forName(context.getClass()
					.getName());
			Constructor constructor = c
					.getConstructor(new Class[] { configContextClass });
			Object obj = constructor.newInstance(new Object[] { context });

			if (obj == null || !(obj instanceof StorageManager))
				throw new SandeshaException(
						"StorageManager must implement org.apache.sandeshat.storage.StorageManager");

			StorageManager mgr = (StorageManager) obj;
			storageManager = mgr;
			return storageManager;

		} catch (Exception e) {
			System.out.println(e.getMessage());
			throw new SandeshaException(e.getMessage());
		}
	}

	public static int getSOAPVersion(SOAPEnvelope envelope)
			throws SandeshaException {
		String namespaceName = envelope.getNamespace().getName();
		if (namespaceName.equals(SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI))
			return Constants.SOAPVersion.v1_1;
		else if (namespaceName
				.equals(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI))
			return Constants.SOAPVersion.v1_2;
		else
			throw new SandeshaException("Unknown SOAP version");
	}

	public static boolean isRMGlobalMessage(MessageContext msgCtx) {
		boolean rmGlobalMsg = false;

		String action = msgCtx.getWSAAction();
		SOAPEnvelope env = msgCtx.getEnvelope();
		SOAPHeader header = null;
		if (env != null)
			header = env.getHeader();

		OMElement sequenceElem = null;
		if (header != null)
			sequenceElem = header.getFirstChildWithName(new QName(
					Constants.WSRM.NS_URI_RM, Constants.WSRM.SEQUENCE));

		if (sequenceElem != null)
			rmGlobalMsg = true;

		if (Constants.WSRM.Actions.ACTION_SEQUENCE_ACKNOWLEDGEMENT
				.equals(action))
			rmGlobalMsg = true;

		if (Constants.WSRM.Actions.ACTION_TERMINATE_SEQUENCE.equals(action))
			rmGlobalMsg = true;

		return rmGlobalMsg;
	}

	public static RMMsgContext createResponseRMMessage(
			RMMsgContext referenceRMMessage) throws SandeshaException {
		try {
			MessageContext referenceMessage = referenceRMMessage
					.getMessageContext();
			MessageContext faultMsgContext = Utils
					.createOutMessageContext(referenceMessage);

			RMMsgContext faultRMMsgCtx = MsgInitializer
					.initializeMessage(faultMsgContext);

			return faultRMMsgCtx;

		} catch (AxisFault e) {
			throw new SandeshaException(e.getMessage());
		}
	}

	public static MessageContext createNewRelatedMessageContext(
			RMMsgContext referenceRMMessage, AxisOperation operation)
			throws SandeshaException {
		try {
			MessageContext referenceMessage = referenceRMMessage
					.getMessageContext();
			ConfigurationContext configContext = referenceMessage
					.getSystemContext();
			AxisConfiguration axisConfiguration = configContext.getAxisConfiguration();
			
			MessageContext newMessageContext = new MessageContext(configContext);

			if (referenceMessage.getAxisServiceGroup() != null) {
				newMessageContext.setAxisServiceGroup(referenceMessage
						.getAxisServiceGroup());
				newMessageContext.setServiceGroupContext(referenceMessage
						.getServiceGroupContext());
				newMessageContext.setServiceGroupContextId(referenceMessage
						.getServiceGroupContextId());
			} else {
				AxisServiceGroup axisServiceGroup = new AxisServiceGroup (axisConfiguration);
				ServiceGroupContext serviceGroupContext = new ServiceGroupContext (configContext,axisServiceGroup);
				
				newMessageContext.setAxisServiceGroup(axisServiceGroup);
				newMessageContext.setServiceGroupContext (serviceGroupContext);
			}

			if (referenceMessage.getAxisService() != null) {
				newMessageContext.setAxisService(referenceMessage
						.getAxisService());
				newMessageContext.setServiceContext(referenceMessage
						.getServiceContext());
				newMessageContext.setServiceContextID(referenceMessage
						.getServiceContextID());
			} else {
				AxisService axisService = new AxisService (new QName ("AnonymousRMService")); //just a dummy name.
				ServiceContext serviceContext = new ServiceContext (axisService,newMessageContext.getServiceGroupContext());
				
				newMessageContext.setAxisService(axisService);
				newMessageContext.setServiceContext(serviceContext);
			}

			newMessageContext.setAxisOperation(operation);

			OperationContext operationContext = new OperationContext(operation);
			newMessageContext.setOperationContext(operationContext);
			operationContext.addMessageContext(newMessageContext);

			//adding a blank envelope
			SOAPFactory factory = SOAPAbstractFactory
					.getSOAPFactory(SandeshaUtil
							.getSOAPVersion(referenceMessage.getEnvelope()));
			newMessageContext.setEnvelope(factory.getDefaultEnvelope());

			newMessageContext.setTransportIn(referenceMessage.getTransportIn());
			newMessageContext.setTransportOut(referenceMessage
					.getTransportOut());

			//copying transport info.
			newMessageContext.setProperty(MessageContext.TRANSPORT_OUT,
					referenceMessage.getProperty(MessageContext.TRANSPORT_OUT));
			newMessageContext.setProperty(HTTPConstants.HTTPOutTransportInfo,
					referenceMessage
							.getProperty(HTTPConstants.HTTPOutTransportInfo));
			newMessageContext.setProperty(Constants.WSP.RM_POLICY_BEAN,
					referenceMessage
					.getProperty(Constants.WSP.RM_POLICY_BEAN));

			return newMessageContext;

		} catch (AxisFault e) {
			throw new SandeshaException(e.getMessage());
		}

	}

}