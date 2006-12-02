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

import javax.xml.stream.XMLStreamReader;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisModule;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.impl.builder.StAXSOAPModelBuilder;
import org.apache.sandesha2.i18n.SandeshaMessageHelper;
import org.apache.sandesha2.i18n.SandeshaMessageKeys;
import org.apache.sandesha2.storage.SandeshaStorageException;
import org.apache.sandesha2.storage.StorageManager;
import org.apache.sandesha2.storage.Transaction;
import org.apache.sandesha2.storage.beanmanagers.RMSBeanMgr;
import org.apache.sandesha2.storage.beanmanagers.InvokerBeanMgr;
import org.apache.sandesha2.storage.beanmanagers.RMDBeanMgr;
import org.apache.sandesha2.storage.beanmanagers.SenderBeanMgr;
import org.apache.sandesha2.storage.beanmanagers.SequencePropertyBeanMgr;
import org.apache.sandesha2.util.SandeshaUtil;

public class InMemoryStorageManager extends StorageManager {

	private static InMemoryStorageManager instance = null;
    private final String MESSAGE_MAP_KEY = "Sandesha2MessageMap";
    private final String ENVELOPE_MAP_KEY = "Sandesha2EnvelopeMap";
    private RMSBeanMgr  rmsBeanMgr = null;
    private RMDBeanMgr nextMsgBeanMgr = null;
    private SequencePropertyBeanMgr sequencePropertyBeanMgr = null;
    private SenderBeanMgr senderBeanMgr = null;
    private InvokerBeanMgr invokerBeanMgr = null;
    
	public InMemoryStorageManager(ConfigurationContext context) {
		super(context);
		
		this.rmsBeanMgr = new InMemoryRMSBeanMgr (context);
		this.nextMsgBeanMgr = new InMemoryRMDBeanMgr (context);
		this.senderBeanMgr = new InMemorySenderBeanMgr (context);
		this.invokerBeanMgr = new InMemoryInvokerBeanMgr (context);
		this.sequencePropertyBeanMgr = new InMemorySequencePropertyBeanMgr (context);
	}

	public Transaction getTransaction() {
		return new InMemoryTransaction();
	}

	public RMSBeanMgr getRMSBeanMgr() {
		return rmsBeanMgr;
	}

	public RMDBeanMgr getRMDBeanMgr() {
		return nextMsgBeanMgr;
	}

	public SenderBeanMgr getSenderBeanMgr() {
		return senderBeanMgr;
	}

	public SequencePropertyBeanMgr getSequencePropertyBeanMgr() {
		return sequencePropertyBeanMgr;
	}

	public InvokerBeanMgr getInvokerBeanMgr() {
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
	
	public MessageContext retrieveMessageContext(String key,ConfigurationContext context) throws SandeshaStorageException {
		HashMap storageMap = (HashMap) getContext().getProperty(MESSAGE_MAP_KEY);
		if (storageMap==null)
			return null;
		
		MessageContext messageContext = (MessageContext) storageMap.get(key);
		
		HashMap envMap = (HashMap) getContext().getProperty(ENVELOPE_MAP_KEY);
		if(envMap==null) {
			return null;
		}
		
		//Get hold of the original SOAP envelope
		SOAPEnvelope envelope = (SOAPEnvelope)envMap.get(key);
		
		//Now clone the env and set it in the message context
		if (envelope!=null) {
			envelope.build();
			XMLStreamReader streamReader = envelope.cloneOMElement().getXMLStreamReader();
			SOAPEnvelope clonedEnvelope = new StAXSOAPModelBuilder(streamReader, null).getSOAPEnvelope();
			try {
				messageContext.setEnvelope(clonedEnvelope);
			} catch (AxisFault e) {
				throw new SandeshaStorageException (e);
			}
		}
		
		return messageContext; 
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
		
		//Now get hold of the SOAP envelope and store it in the env map
		HashMap envMap = (HashMap) getContext().getProperty(ENVELOPE_MAP_KEY);
		
		if(envMap==null) {
			envMap = new HashMap ();
			getContext().setProperty(ENVELOPE_MAP_KEY, envMap);
		}
		
		envMap.put(key, msgContext.getEnvelope());
		
	}

	public void updateMessageContext(String key,MessageContext msgContext) throws SandeshaStorageException { 
		HashMap storageMap = (HashMap) getContext().getProperty(MESSAGE_MAP_KEY);
		
		if (storageMap==null) {
			throw new SandeshaStorageException (SandeshaMessageHelper.getMessage(
					SandeshaMessageKeys.storageMapNotPresent));
		}
		
		Object oldEntry = storageMap.get(key);
		if (oldEntry==null)
			throw new SandeshaStorageException (SandeshaMessageHelper.getMessage(
					SandeshaMessageKeys.entryNotPresentForUpdating));
		
		HashMap envMap = (HashMap) getContext().getProperty(ENVELOPE_MAP_KEY);
		
		if(envMap==null) {
			throw new SandeshaStorageException (SandeshaMessageHelper.getMessage(
					SandeshaMessageKeys.envelopeMapNotPresent));
		}
		
		oldEntry = envMap.get(key);
		if (oldEntry==null)
			throw new SandeshaStorageException (SandeshaMessageHelper.getMessage(
					SandeshaMessageKeys.entryNotPresentForUpdating));
		
		envMap.remove(key);
		envMap.put(key, msgContext.getEnvelope());
		
		storeMessageContext(key,msgContext);
	}
	
	public void removeMessageContext(String key) throws SandeshaStorageException { 
		HashMap storageMap = (HashMap) getContext().getProperty(MESSAGE_MAP_KEY);
		
		if (storageMap==null) {
			return;
		}
		
		Object entry = storageMap.get(key);
		if (entry!=null)
			storageMap.remove(key);
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
