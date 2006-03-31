/*
 * Copyright 2004,2005 The Apache Software Foundation.
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

package org.apache.sandesha2.storage.inmemory;

import java.util.HashMap;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisModule;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.sandesha2.storage.SandeshaStorageException;
import org.apache.sandesha2.storage.StorageManager;
import org.apache.sandesha2.storage.Transaction;
import org.apache.sandesha2.storage.beanmanagers.CreateSeqBeanMgr;
import org.apache.sandesha2.storage.beanmanagers.InvokerBeanMgr;
import org.apache.sandesha2.storage.beanmanagers.NextMsgBeanMgr;
import org.apache.sandesha2.storage.beanmanagers.SenderBeanMgr;
import org.apache.sandesha2.storage.beanmanagers.SequencePropertyBeanMgr;
import org.apache.sandesha2.util.SandeshaUtil;

/**
 * @author Chamikara Jayalath <chamikaramj@gmail.com>
 * @author Sanka Samaranayaka <ssanka@gmail.com>
 */

public class InMemoryStorageManager extends StorageManager {

	private static InMemoryStorageManager instance = null;
    private final String MESSAGE_MAP_KEY = "Sandesha2MessageMap";
    private CreateSeqBeanMgr  createSeqBeanMgr = null;
    private NextMsgBeanMgr nextMsgBeanMgr = null;
    private SequencePropertyBeanMgr sequencePropertyBeanMgr = null;
    private SenderBeanMgr senderBeanMgr = null;
    private InvokerBeanMgr invokerBeanMgr = null;
    
	public InMemoryStorageManager(ConfigurationContext context) {
		super(context);
		
		this.createSeqBeanMgr = new InMemoryCreateSeqBeanMgr (context);
		this.nextMsgBeanMgr = new InMemoryNextMsgBeanMgr (context);
		this.senderBeanMgr = new InMemorySenderBeanMgr (context);
		this.invokerBeanMgr = new InMemoryInvokerBeanMgr (context);
		this.sequencePropertyBeanMgr = new InMemorySequencePropertyBeanMgr (context);
	}

	public Transaction getTransaction() {
		return new InMemoryTransaction();
	}

	public CreateSeqBeanMgr getCreateSeqBeanMgr() {
		return createSeqBeanMgr;
	}

	public NextMsgBeanMgr getNextMsgBeanMgr() {
		return nextMsgBeanMgr;
	}

	public SenderBeanMgr getRetransmitterBeanMgr() {
		return senderBeanMgr;
	}

	public SequencePropertyBeanMgr getSequencePropretyBeanMgr() {
		return sequencePropertyBeanMgr;
	}

	public InvokerBeanMgr getStorageMapBeanMgr() {
		return invokerBeanMgr;
	}

	public void init(ConfigurationContext context) {
		setContext(context);
	}

	public static InMemoryStorageManager getInstance(
			ConfigurationContext context) {
		if (instance == null)
			instance = new InMemoryStorageManager(context);

		return instance;
	}
	
	public MessageContext retrieveMessageContext(String key,ConfigurationContext context) {
		HashMap storageMap = (HashMap) getContext().getProperty(MESSAGE_MAP_KEY);
		if (storageMap==null)
			return null;
		
		return (MessageContext) storageMap.get(key);
	}

	public void storeMessageContext(String key,MessageContext msgContext) {
		HashMap storageMap = (HashMap) getContext().getProperty(MESSAGE_MAP_KEY);
		
		if (storageMap==null) {
			storageMap = new HashMap ();
			getContext().setProperty(MESSAGE_MAP_KEY,storageMap);
		}
		
		if (key==null)
		    key = SandeshaUtil.getUUID();
		
		storageMap.put(key,msgContext);
		
	}
	
//	public MessageContext retrieveMessageContext(String key, ConfigurationContext configContext) throws SandeshaStorageException {
//		// TODO remove this temporary method
//		
//	
//		HashMap storageMap = (HashMap) getContext().getProperty(MESSAGE_MAP_KEY);
//
//		if (storageMap==null)
//			return null;
//		
//		MessageStoreBean messageStoreBean = (MessageStoreBean) storageMap.get(key);
//		if (messageStoreBean==null)
//			return null;
//		
//		MessageContext messageContext = new MessageContext ();
//		try {
//			String soapEnvelopeStr = messageStoreBean.getSOAPEnvelopeString();
//			StringReader strReader = new StringReader (soapEnvelopeStr);
//			
//			XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(strReader);
//			
//			SOAPFactory factory = null;
//			if (messageStoreBean.getSOAPVersion()==Sandesha2Constants.SOAPVersion.v1_1) {
//				factory = OMAbstractFactory.getSOAP11Factory();
//			} else if (messageStoreBean.getSOAPVersion()==Sandesha2Constants.SOAPVersion.v1_2) {
//				factory = OMAbstractFactory.getSOAP12Factory();
//			} else {
//				throw new SandeshaStorageException ("Unknows SOAP version");
//			}
//			
//			//TODO make this SOAP version indipendent
//			OMXMLParserWrapper wrapper = OMXMLBuilderFactory.createStAXSOAPModelBuilder(
//			        factory, reader);
//			SOAPEnvelope envelope = (SOAPEnvelope) wrapper.getDocumentElement();
//			messageContext.setEnvelope(envelope);
//			
//			AxisConfiguration axisConfiguration = configContext.getAxisConfiguration();
//			
//			String transportOutStr = messageStoreBean.getTransportOut();
//			if (transportOutStr!=null) {
//				QName transportOutName = SandeshaUtil.getQNameFromString(transportOutStr);
//				TransportOutDescription transportOut = configContext.getAxisConfiguration().getTransportOut(transportOutName);
//				messageContext.setTransportOut(transportOut);
//			}
//			
//			String serviceGroupName = messageStoreBean.getAxisServiceGroup();
//			if (serviceGroupName!=null) {
//				AxisServiceGroup serviceGroup = axisConfiguration.getServiceGroup(serviceGroupName);
//				if (serviceGroup!=null)
//					messageContext.setAxisServiceGroup(serviceGroup);
//			} else {
//				AxisServiceGroup serviceGroup = new AxisServiceGroup (axisConfiguration);
//				messageContext.setAxisServiceGroup(serviceGroup);
//			}
//			
//			String serviceName = messageStoreBean.getAxisService();
//			if (serviceName!=null) {
//				AxisService service = axisConfiguration.getService(serviceName);
//				if (service!=null)
//					messageContext.setAxisService(service);
//			}
//			
//			String operationNameStr = messageStoreBean.getAxisOperation();
//			String operationMEPStr = messageStoreBean.getAxisOperationMEP();
//			if ((operationNameStr!=null || operationMEPStr!=null) && messageContext.getAxisService()!=null) {
//				
//				AxisOperation operation = null;
//				if (operationNameStr!=null) {
//					QName operationName = SandeshaUtil.getQNameFromString(operationNameStr);
//					operation = messageContext.getAxisService().getOperation(operationName);
//				}
//				
//				if (operation==null && operationMEPStr!=null)
//					operation = AxisOperationFactory.getOperationDescription(operationMEPStr);
//				
//				if (operation!=null) {
//					messageContext.setAxisOperation(operation);
//					
//					//TODO set the correct message receiver.	
//					MessageReceiver messageReceiver = axisConfiguration.getMessageReceiver(messageStoreBean.getAxisOperationMEP());
//
//				}
//			}
//			
//			
//			//setting contexts TODO is this necessary?
//			if (messageContext.getAxisServiceGroup()!=null){
//				ServiceGroupContext serviceGroupCtx = new ServiceGroupContext (configContext,messageContext.getAxisServiceGroup());
//				messageContext.setServiceGroupContext(serviceGroupCtx);
//			}
//			
//			if (messageContext.getAxisService()!=null) {
//				ServiceContext serviceContext = new ServiceContext (messageContext.getAxisService(),messageContext.getServiceGroupContext());
//				serviceContext.setParent(messageContext.getServiceGroupContext());
//				messageContext.setServiceContext(serviceContext);
//			}
//			
//			if (messageContext.getAxisOperation()!=null) {
//				OperationContext operationContext = new OperationContext (messageContext.getAxisOperation());
//				operationContext.setParent(messageContext.getServiceContext());
//				messageContext.setOperationContext(operationContext);
//			}
//			
//			
//			messageContext.setServerSide(messageStoreBean.isServerSide());
//			messageContext.setFLOW(messageStoreBean.getFlow());
//			
//			messageContext.setProperty(MessageContextConstants.TRANSPORT_URL,messageStoreBean.getTransportTo());
//			messageContext.setTo(new EndpointReference (messageStoreBean.getToURL()));
//			
//		} catch (XMLStreamException e) {
//			throw new SandeshaStorageException (e.getMessage());
//		} catch (FactoryConfigurationError e) {
//			throw new SandeshaStorageException (e.getMessage());
//		} catch (AxisFault e) {
//			throw new SandeshaStorageException (e.getMessage());
//		}
//         
//		return messageContext;
//	}
//
//	public void storeMessageContext(String key,MessageContext msgContext) {
//		// TODO Auto-generated method stub
//		
//		HashMap storageMap = (HashMap) getContext().getProperty(MESSAGE_MAP_KEY);
//		
//		if (storageMap==null) {
//			storageMap = new HashMap ();
//			getContext().setProperty(MESSAGE_MAP_KEY,storageMap);
//		}
//		
//		SOAPEnvelope envelope = msgContext.getEnvelope();
//		String str = envelope.toString();
//		
//		int SOAPVersion = 0;
//		if (msgContext.isSOAP11())
//			SOAPVersion = Sandesha2Constants.SOAPVersion.v1_1;
//		else
//			SOAPVersion = Sandesha2Constants.SOAPVersion.v1_2;
//				
//		MessageStoreBean bean = new MessageStoreBean ();
//		
//		TransportOutDescription transportOutDescription = msgContext.getTransportOut();
//		AxisServiceGroup serviceGroup = msgContext.getAxisServiceGroup();
//		AxisService service = msgContext.getAxisService();
//		AxisOperation operation = msgContext.getAxisOperation();
//		
//		if (transportOutDescription!=null) {
//			QName name = transportOutDescription.getName();
//			bean.setTransportOut(SandeshaUtil.getStringFromQName(name));
//		}
//		
//		if (serviceGroup!=null) {
//			bean.setAxisServiceGroup(serviceGroup.getServiceGroupName());
//		}
//		
//		if (service!=null) {
//			bean.setAxisService(service.getName());
//		}
//		
//		if (operation!=null) {
//			QName name = operation.getName();
//			if (name!=null)
//				bean.setAxisOperation(SandeshaUtil.getStringFromQName(name));
//			bean.setAxisOperationMEP(operation.getMessageExchangePattern());
//		}
//		
//		bean.setFlow(msgContext.getFLOW());
//		bean.setServerSide(msgContext.isServerSide());
//		
//		bean.setStoredKey(key);
//		bean.setSOAPEnvelopeString(str);
//		bean.setSOAPVersion(SOAPVersion);
//
//		bean.setToURL(msgContext.getTo().getAddress());
//		bean.setTransportTo((String) msgContext.getProperty(MessageContextConstants.TRANSPORT_URL));
//				
//		storageMap.put(key,bean);
//	}

	public void updateMessageContext(String key,MessageContext msgContext) throws SandeshaStorageException { 
		HashMap storageMap = (HashMap) getContext().getProperty(MESSAGE_MAP_KEY);
		
		if (storageMap==null) {
			throw new SandeshaStorageException ("Storage Map not present");
		}
		
		Object oldEntry = storageMap.get(key);
		if (oldEntry==null)
			throw new SandeshaStorageException ("Entry is not present for updating");
		
		storeMessageContext(key,msgContext);
	}
	
	public void  initStorage (AxisModule moduleDesc) {
		
	}

	public SOAPEnvelope retrieveSOAPEnvelope(String key) throws SandeshaStorageException {
		// TODO no real value
		return null;
	}

	public void storeSOAPEnvelope(SOAPEnvelope envelope, String key) throws SandeshaStorageException {
		// TODO no real value
	}
	
	
}