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

import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.MessageContextConstants;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.context.ServiceGroupContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.Handler;
import org.apache.ws.commons.om.OMElement;
import org.apache.ws.commons.soap.SOAP11Constants;
import org.apache.ws.commons.soap.SOAP12Constants;
import org.apache.ws.commons.soap.SOAPEnvelope;
import org.apache.ws.commons.soap.SOAPFactory;
import org.apache.ws.commons.soap.SOAPHeader;
import org.apache.axis2.util.UUIDGenerator;
import org.apache.axis2.util.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.sandesha2.RMMsgContext;
import org.apache.sandesha2.Sandesha2Constants;
import org.apache.sandesha2.SandeshaException;
import org.apache.sandesha2.storage.StorageManager;
import org.apache.sandesha2.storage.beanmanagers.SequencePropertyBeanMgr;
import org.apache.sandesha2.storage.beans.SequencePropertyBean;
import org.apache.sandesha2.workers.InOrderInvoker;
import org.apache.sandesha2.workers.Sender;
import org.apache.sandesha2.wsrm.AcknowledgementRange;

/**
 * Contains utility methods that are used in many plases of Sandesha2.
 * 
 * @author Chamikara Jayalath <chamikaramj@gmail.com>
 */

public class SandeshaUtil {

	//private static Hashtable storedMsgContexts = new Hashtable();

	private static StorageManager storageManager = null;

	private static Sender sender = new Sender();

	private static InOrderInvoker invoker = new InOrderInvoker();
	
	private static Log log = LogFactory.getLog(SandeshaUtil.class);

	/**
	 * Create a new UUID.
	 * 
	 * @return
	 */
	public static String getUUID() {
		//String uuid = "uuid:" + UUIDGenerator.getUUID();
		String uuid = UUIDGenerator.getUUID();

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
			SOAPFactory factory, String rmNamespaceValue) throws SandeshaException {

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
						factory,rmNamespaceValue);
				ackRange.setLowerValue(lower);
				ackRange.setUpperValue(upper);
				ackRanges.add(ackRange);

				lower = temp;
				upper = temp;
				completed = false;
			}
		}

		if (!completed) {
			AcknowledgementRange ackRange = new AcknowledgementRange(factory,rmNamespaceValue);
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
				String message = "Invalid msg number list";
				log.debug(message);
				throw new SandeshaException(message);
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

//	/**
//	 * Used to store message context objects. Currently they are stored in a in-memory HashMap.
//	 * Returned key can be used to retrieve the message context.
//	 * 
//	 * @param ctx
//	 * @return
//	 * @throws SandeshaException
//	 */
//	public static String storeMessageContext(MessageContext ctx)
//			throws SandeshaException {
//		if (ctx == null) {
//			String message = "Stored Msg Ctx is null";
//			log.debug(message);
//			throw new SandeshaException(message);
//		}
//
//		String key = getUUID();
//		storedMsgContexts.put(key, ctx);
//		return key;
//	}
	
//	/**
//	 * Retrieve the MessageContexts saved by the above method.
//	 * 
//	 * @param key
//	 * @return
//	 */
//	public static MessageContext getStoredMessageContext(String key) {
//		return (MessageContext) storedMsgContexts.get(key);
//	}

	public static void startSenderForTheSequence(ConfigurationContext context, String sequenceID) {
		sender.runSenderForTheSequence (context,sequenceID);
	}

	public static void stopSenderForTheSequence(String sequenceID) {
		sender.stopSenderForTheSequence (sequenceID);
	}
	
	public static void startInvokerForTheSequence(ConfigurationContext context, String sequenceID) {
		if (!invoker.isInvokerStarted()) {
			invoker.runInvokerForTheSequence(context,sequenceID);
		}
	}
	
	public static void stopInvokerForTheSequence(String sequenceID) {
		invoker.stopInvokerForTheSequence (sequenceID);
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

	/*public static SOAPEnvelope createSOAPMessage(MessageContext msgContext,
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

	}*/

	public static String getMessageTypeString(int messageType) {
		switch (messageType) {
		case Sandesha2Constants.MessageTypes.CREATE_SEQ:
			return "CreateSequence";
		case Sandesha2Constants.MessageTypes.CREATE_SEQ_RESPONSE:
			return "CreateSequenceResponse";
		case Sandesha2Constants.MessageTypes.ACK:
			return "Acknowledgement";
		case Sandesha2Constants.MessageTypes.APPLICATION:
			return "Application";
		case Sandesha2Constants.MessageTypes.TERMINATE_SEQ:
			return "TerminateSequence";
		case Sandesha2Constants.MessageTypes.ACK_REQUEST:
			return "AckRequest";
		case Sandesha2Constants.MessageTypes.CLOSE_SEQUENCE:
			return "CloseSequence";
		case Sandesha2Constants.MessageTypes.CLOSE_SEQUENCE_RESPONSE:
			return "CloseSequenceResponse";
		case Sandesha2Constants.MessageTypes.TERMINATE_SEQ_RESPONSE:
			return "TerminateSequenceResponse";
		case Sandesha2Constants.MessageTypes.UNKNOWN:
			return "Unknown";
		default:
			return "Error";
		}
	}

	public static boolean isGloballyProcessableMessageType(int type) {
		if (type == Sandesha2Constants.MessageTypes.ACK
				|| type == Sandesha2Constants.MessageTypes.TERMINATE_SEQ) {
			return true;
		}

		return false;
	}

	public static boolean isDuplicateDropRequiredMsgType(int rmMessageType) {
		if (rmMessageType == Sandesha2Constants.MessageTypes.APPLICATION)
			return true;

		if (rmMessageType == Sandesha2Constants.MessageTypes.CREATE_SEQ_RESPONSE)
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

	public static String getServerSideIncomingSeqIdFromInternalSeqId (
			String internalSequenceId) throws SandeshaException  {
		
		String startStr = Sandesha2Constants.SANDESHA2_INTERNAL_SEQUENCE_ID + ":";
		if (!internalSequenceId.startsWith(startStr)){
			throw new SandeshaException ("Invalid internal sequence ID");
		}
		
		String incomingSequenceId = internalSequenceId.substring(startStr.length());
		return incomingSequenceId;
	}

	public static String getServerSideInternalSeqIdFromIncomingSeqId(
			String incomingSequenceId) {
		String internalSequenceId =  Sandesha2Constants.SANDESHA2_INTERNAL_SEQUENCE_ID + ":" + incomingSequenceId;
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
		
		if (storageManager!=null)
			return storageManager;
		
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
			String message = "Cannot load the given storage manager";
			log.error(message);
			throw new SandeshaException(message);
		}
	}

	public static int getSOAPVersion(SOAPEnvelope envelope)
			throws SandeshaException {
		String namespaceName = envelope.getNamespace().getName();
		if (namespaceName.equals(SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI))
			return Sandesha2Constants.SOAPVersion.v1_1;
		else if (namespaceName
				.equals(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI))
			return Sandesha2Constants.SOAPVersion.v1_2;
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
		else {
			log.error("SOAP envelope is null");
			return false;
		}

		//TODO make this spec indipendent 
		
		OMElement sequenceElem = null;
		if (header != null)
			sequenceElem = header.getFirstChildWithName(new QName(
					Sandesha2Constants.SPEC_2005_02.NS_URI, Sandesha2Constants.WSRM_COMMON.SEQUENCE));

		if (sequenceElem==null)
			sequenceElem = header.getFirstChildWithName(new QName(
					Sandesha2Constants.SPEC_2005_10.NS_URI, Sandesha2Constants.WSRM_COMMON.SEQUENCE));
			
		if (sequenceElem != null)
			rmGlobalMsg = true;

		if (Sandesha2Constants.SPEC_2005_02.Actions.ACTION_SEQUENCE_ACKNOWLEDGEMENT
				.equals(action))
			rmGlobalMsg = true;
		
		if (Sandesha2Constants.SPEC_2005_02.Actions.ACTION_CREATE_SEQUENCE_RESPONSE.equals(action))
			rmGlobalMsg = true;

		if (Sandesha2Constants.SPEC_2005_02.Actions.ACTION_TERMINATE_SEQUENCE.equals(action))
			rmGlobalMsg = true;
		
		
		
		if (Sandesha2Constants.SPEC_2005_10.Actions.ACTION_SEQUENCE_ACKNOWLEDGEMENT
				.equals(action))
			rmGlobalMsg = true;

		if (Sandesha2Constants.SPEC_2005_10.Actions.ACTION_TERMINATE_SEQUENCE.equals(action))
			rmGlobalMsg = true;
		
		if (Sandesha2Constants.SPEC_2005_10.Actions.ACTION_CREATE_SEQUENCE_RESPONSE.equals(action))
			rmGlobalMsg = true;

		
		return rmGlobalMsg;
	}

//	public static RMMsgContext createResponseRMMessage(
//			RMMsgContext referenceRMMessage) throws SandeshaException {
//		try {
//			MessageContext referenceMessage = referenceRMMessage
//					.getMessageContext();
//			MessageContext faultMsgContext = Utils
//					.createOutMessageContext(referenceMessage);
//
//			RMMsgContext faultRMMsgCtx = MsgInitializer
//					.initializeMessage(faultMsgContext);
//
//			return faultRMMsgCtx;
//
//		} catch (AxisFault e) {
//			log.debug(e.getMessage());
//			throw new SandeshaException(e.getMessage());
//		}
//	}

	public static MessageContext createNewRelatedMessageContext(
			RMMsgContext referenceRMMessage, AxisOperation operation)
			throws SandeshaException {
		try {
			MessageContext referenceMessage = referenceRMMessage
					.getMessageContext();
			ConfigurationContext configContext = referenceMessage
					.getConfigurationContext();
			AxisConfiguration axisConfiguration = configContext.getAxisConfiguration();
			
			MessageContext newMessageContext = new MessageContext();
			newMessageContext.setConfigurationContext(configContext);

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
				AxisService axisService = new AxisService ("AnonymousRMService"); //just a dummy name.
				ServiceContext serviceContext = new ServiceContext (axisService,newMessageContext.getServiceGroupContext());
				
				newMessageContext.setAxisService(axisService);
				newMessageContext.setServiceContext(serviceContext);
			}

			newMessageContext.setAxisOperation(operation);

			
			//setting parent child relationships 
			AxisService service = newMessageContext.getAxisService();
			if (service!=null && operation!=null) {
				service.addChild(operation);
				operation.setParent(service);
			}
			
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

			copyNecessaryPropertiesFromRelatedContext (referenceMessage,newMessageContext);
			
			//copying transport info.
			newMessageContext.setProperty(MessageContext.TRANSPORT_OUT,
					referenceMessage.getProperty(MessageContext.TRANSPORT_OUT));
			newMessageContext.setProperty(Sandesha2Constants.WSP.RM_POLICY_BEAN,
					referenceMessage
					.getProperty(Sandesha2Constants.WSP.RM_POLICY_BEAN));

			newMessageContext.setProperty(Constants.OUT_TRANSPORT_INFO,referenceMessage.getProperty(Constants.OUT_TRANSPORT_INFO));
			newMessageContext.setProperty(MessageContext.TRANSPORT_HEADERS,referenceMessage.getProperty(MessageContext.TRANSPORT_HEADERS));
			newMessageContext.setProperty(MessageContext.TRANSPORT_IN,referenceMessage.getProperty(MessageContext.TRANSPORT_IN));
			newMessageContext.setProperty(MessageContext.TRANSPORT_OUT,referenceMessage.getProperty(MessageContext.TRANSPORT_OUT));
			newMessageContext.setExecutionChain(referenceMessage.getExecutionChain());
			
			return newMessageContext;

		} catch (AxisFault e) {
			log.debug(e.getMessage());
			throw new SandeshaException(e.getMessage());
		}

	}
	
	private static void copyNecessaryPropertiesFromRelatedContext (MessageContext fromMessage, MessageContext toMessage) {
		toMessage.setProperty(MessageContextConstants.TRANSPORT_URL,fromMessage.getProperty(MessageContextConstants.TRANSPORT_URL));
	}
	
	public static ArrayList getArrayListFromString (String str) throws SandeshaException {
		
		if (str==null || "".equals(str))
			return new ArrayList ();
		
		if (str.length()<2) {
			String message = "Invalid String array : " + str;
			log.debug(message);
			throw new SandeshaException (message);
		}
		
		int length = str.length();
		
		if (str.charAt(0)!='[' || str.charAt(length-1)!=']') {
			String message = "Invalid String array" + str;
			log.debug(message);
			throw new SandeshaException (message);
		}
		
		ArrayList retArr = new ArrayList ();
		
		String subStr = str.substring(1,length-1);
		
		String[] parts = subStr.split(",");
		
		for (int i=0;i<parts.length;i++) {
			if (!"".equals(parts[i]))
				retArr.add(parts[i].trim());
		}
		
		return retArr;
	}
	
	public static String getInternalSequenceID (String to, String sequenceKey) {
		if (to==null && sequenceKey==null)
			return null;
		else if (to==null) 
			return sequenceKey;
		else if (sequenceKey==null)
			return to;
		else 
			return Sandesha2Constants.INTERNAL_SEQUENCE_PREFIX + ":" + to + ":" +sequenceKey;
	}
	
	public static String getInternalSequenceID (String sequenceID) {
			return Sandesha2Constants.INTERNAL_SEQUENCE_PREFIX + ":" + sequenceID;
	}
	
	public static String getSequenceIDFromInternalSequenceID (String internalSequenceID, ConfigurationContext configurationContext)  throws SandeshaException {
		
		StorageManager storageManager = getSandeshaStorageManager(configurationContext);
		SequencePropertyBeanMgr sequencePropertyBeanMgr = storageManager.getSequencePropretyBeanMgr();
		
		SequencePropertyBean outSequenceBean = sequencePropertyBeanMgr.retrieve(internalSequenceID, Sandesha2Constants.SequenceProperties.OUT_SEQUENCE_ID);
		
		String sequeunceID = null;
		if (outSequenceBean!=null)
			sequeunceID = outSequenceBean.getValue();
		
		return sequeunceID;
	}
		
	public static QName getQNameFromString (String qnameStr) throws SandeshaException {
		String[] parts = qnameStr.split(Sandesha2Constants.QNAME_SEPERATOR);
		if (!(parts.length==3))
			throw new SandeshaException ("Invalid QName String");
		
		if (parts[0].equals(Sandesha2Constants.VALUE_NONE))
			parts[0]=null;
		
		if (parts[1].equals(Sandesha2Constants.VALUE_NONE))
			parts[1]=null;
		
		if (parts[2].equals(Sandesha2Constants.VALUE_NONE))
			parts[2]=null;
		
		if (parts[0].equals(Sandesha2Constants.VALUE_EMPTY))
			parts[0]="";
		
		if (parts[1].equals(Sandesha2Constants.VALUE_EMPTY))
			parts[1]="";
		
		if (parts[2].equals(Sandesha2Constants.VALUE_EMPTY))
			parts[2]="";
		
		String namespace = parts[0];
		String localPart = parts[1];
		String prefix = parts[2];
		
		QName name = new QName (namespace, localPart,prefix);
		return name;
	}
	
	public static String getStringFromQName (QName name) {
		String localPart = name.getLocalPart();
		String namespace = name.getNamespaceURI();
		String prefix = name.getPrefix();
		
		if (localPart==null)
			localPart = Sandesha2Constants.VALUE_NONE;

		if (namespace==null)
			namespace = Sandesha2Constants.VALUE_NONE;
		
		if (prefix==null)
			prefix = Sandesha2Constants.VALUE_NONE;
		
		if ("".equals(localPart))
			localPart = Sandesha2Constants.VALUE_EMPTY;

		if ("".equals(namespace))
			namespace = Sandesha2Constants.VALUE_EMPTY;
		
		if ("".equals(prefix))
			prefix = Sandesha2Constants.VALUE_EMPTY;
		
		String QNameStr = namespace + Sandesha2Constants.QNAME_SEPERATOR + localPart + Sandesha2Constants.QNAME_SEPERATOR 
								+ prefix;
		
		return QNameStr;
	}
	
	public static String getExecutionChainString (ArrayList executionChain) {
		Iterator iter = executionChain.iterator();
		
		String executionChainStr = "";
		while (iter.hasNext()) {
			Handler handler = (Handler) iter.next();
			QName name = handler.getName();
			String handlerStr = SandeshaUtil.getStringFromQName(name);
			executionChainStr = executionChainStr + Sandesha2Constants.EXECUTIN_CHAIN_SEPERATOR + handlerStr;
		}
		
		return executionChainStr;
	}
	
	
	//TODO complete below.
	public static ArrayList getExecutionChainFromString (String executionChainStr, ConfigurationContext configContext) throws SandeshaException {
		String[] QNameStrs = executionChainStr.split(Sandesha2Constants.EXECUTIN_CHAIN_SEPERATOR);
		
		AxisConfiguration axisConfiguration = configContext.getAxisConfiguration();
		
		int length = QNameStrs.length;
		for (int i=0;i<length;i++) {
			String QNameStr = QNameStrs[i];
			QName name = getQNameFromString(QNameStr);
			//axisConfiguration.get
			
		}
		
		return null;  //not complete yet.
	}
	
	public static void printSOAPEnvelope (SOAPEnvelope envelope, OutputStream out) throws SandeshaException {
		try {
			XMLStreamWriter writer =  XMLOutputFactory.newInstance().createXMLStreamWriter(out);
			System.out.println("\n");
			envelope.serialize(writer);
		} catch (XMLStreamException e) {
			throw new SandeshaException (e.getMessage());
		} catch (FactoryConfigurationError e) {
			throw new SandeshaException (e.getMessage());
		}
	}
	
	/**
	 * 
	 * @param propertyKey
	 * for the client side - internalSequenceID, for the server side - sequenceID
	 * @param configurationContext
	 * @return
	 * @throws SandeshaException
	 */
	public static String getRMVersion (String propertyKey, ConfigurationContext configurationContext) throws SandeshaException {
			StorageManager storageManager = getSandeshaStorageManager(configurationContext);
			
			SequencePropertyBeanMgr sequencePropertyBeanMgr = storageManager.getSequencePropretyBeanMgr();
			SequencePropertyBean specVersionBean = sequencePropertyBeanMgr.retrieve(propertyKey,Sandesha2Constants.SequenceProperties.RM_SPEC_VERSION);
			
			if (specVersionBean==null)
				return null;
	
			return specVersionBean.getValue();
	}

}