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

package org.apache.sandesha2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.sandesha2.storage.StorageManager;
import org.apache.sandesha2.storage.beanmanagers.CreateSeqBeanMgr;
import org.apache.sandesha2.storage.beanmanagers.NextMsgBeanMgr;
import org.apache.sandesha2.storage.beanmanagers.SenderBeanMgr;
import org.apache.sandesha2.storage.beanmanagers.SequencePropertyBeanMgr;
import org.apache.sandesha2.storage.beanmanagers.InvokerBeanMgr;
import org.apache.sandesha2.storage.beans.CreateSeqBean;
import org.apache.sandesha2.storage.beans.NextMsgBean;
import org.apache.sandesha2.storage.beans.SenderBean;
import org.apache.sandesha2.storage.beans.SequencePropertyBean;
import org.apache.sandesha2.storage.beans.InvokerBean;
import org.apache.sandesha2.util.PropertyManager;
import org.apache.sandesha2.util.SandeshaUtil;

/**
 * Contains logic to remove all the storad data of a sequence.
 * Methods of this are called by sending side and the receiving side when appropriate
 * 
 * @author Chamikara Jayalath <chamikaramj@gmail.com>
 */

public class TerminateManager {

	/**
	 * Called by the receiving side to remove data related to a sequence.
	 * e.g. After sending the TerminateSequence message. Calling this methods will complete all
	 * the data if InOrder invocation is not sequired.
	 * 
	 * @param configContext
	 * @param sequenceID
	 * @throws SandeshaException
	 */
	public static void terminateReceivingSide (ConfigurationContext configContext, String sequenceID) throws SandeshaException {
		StorageManager storageManager = SandeshaUtil.getSandeshaStorageManager(configContext);
		NextMsgBeanMgr nextMsgBeanMgr = storageManager.getNextMsgBeanMgr();
		
		//removing nextMsgMgr entries
		NextMsgBean findNextMsgBean = new NextMsgBean ();
		findNextMsgBean.setSequenceID(sequenceID);
		Collection collection = nextMsgBeanMgr.find(findNextMsgBean);
		Iterator iterator = collection.iterator();
		while (iterator.hasNext()) {
			NextMsgBean nextMsgBean = (NextMsgBean) iterator.next();
			nextMsgBeanMgr.delete(nextMsgBean.getSequenceID());
		}
		
		boolean inOrderInvocation = PropertyManager.getInstance().isInOrderInvocation();
		if(!inOrderInvocation) { 
			terminateAfterInvocation(configContext,sequenceID);
		}

	}
	
	/**
	 * When InOrder invocation is anabled this had to be called to clean the data left by the 
	 * above method. This had to be called after the Invocation of the Last Message.
	 * 
	 * @param configContext
	 * @param sequenceID
	 * @throws SandeshaException
	 */
	public static void terminateAfterInvocation (ConfigurationContext configContext, String sequenceID) throws SandeshaException {
		StorageManager storageManager = SandeshaUtil.getSandeshaStorageManager(configContext);
		InvokerBeanMgr storageMapBeanMgr = storageManager.getStorageMapBeanMgr();

		//removing storageMap entries
		InvokerBean findStorageMapBean = new InvokerBean ();
		findStorageMapBean.setSequenceID(sequenceID);
		findStorageMapBean.setInvoked(true);
		Collection collection = storageMapBeanMgr.find(findStorageMapBean);
		Iterator iterator = collection.iterator();
		while (iterator.hasNext()) {
			InvokerBean storageMapBean = (InvokerBean) iterator.next();
			storageMapBeanMgr.delete(storageMapBean.getMessageContextRefKey());
		}
		
		removeReceivingSideProperties(configContext,sequenceID);

	}
	
	private static void removeReceivingSideProperties (ConfigurationContext configContext, String sequenceID) throws SandeshaException {
		StorageManager storageManager = SandeshaUtil.getSandeshaStorageManager(configContext);
		SequencePropertyBeanMgr sequencePropertyBeanMgr = storageManager.getSequencePropretyBeanMgr();
		SequencePropertyBean allSequenceBean = sequencePropertyBeanMgr.retrieve(Sandesha2Constants.SequenceProperties.ALL_SEQUENCES,Sandesha2Constants.SequenceProperties.INCOMING_SEQUENCE_LIST);
		ArrayList allSequenceList = SandeshaUtil.getArrayListFromString(allSequenceBean.getValue());
		allSequenceList.remove(sequenceID);
		
		//updating 
		allSequenceBean.setValue(allSequenceList.toString());
		sequencePropertyBeanMgr.update(allSequenceBean);
	}
	
	private static boolean isRequiredForResponseSide (String name) {
		if (name==null && name.equals(Sandesha2Constants.SequenceProperties.LAST_OUT_MESSAGE))
			return false;
		
		if (name.equals(Sandesha2Constants.SequenceProperties.LAST_OUT_MESSAGE))
			return false;
		
		return false;
	}
	
	
	/**
	 * This is called by the sending side to clean data related to a sequence.
	 * e.g. after sending the TerminateSequence message.
	 * 
	 * @param configContext
	 * @param sequenceID
	 * @throws SandeshaException
	 */
	public static void terminateSendingSide (ConfigurationContext configContext, String sequenceID) throws SandeshaException {
		StorageManager storageManager = SandeshaUtil.getSandeshaStorageManager(configContext);
		SequencePropertyBeanMgr sequencePropertyBeanMgr = storageManager.getSequencePropretyBeanMgr();
		SenderBeanMgr retransmitterBeanMgr = storageManager.getRetransmitterBeanMgr();
		CreateSeqBeanMgr createSeqBeanMgr = storageManager.getCreateSeqBeanMgr();
		
		SequencePropertyBean internalSequenceBean = sequencePropertyBeanMgr.retrieve(sequenceID,Sandesha2Constants.SequenceProperties.INTERNAL_SEQUENCE_ID);
		if (internalSequenceBean==null)
			throw new SandeshaException ("TempSequence entry not found");
		
		String internalSequenceId = (String) internalSequenceBean.getValue();
		
		//removing retransmitterMgr entries
		//SenderBean findRetransmitterBean = new SenderBean ();
		//findRetransmitterBean.setInternalSequenceID(internalSequenceId);
		Collection collection = retransmitterBeanMgr.find(internalSequenceId);
		Iterator iterator = collection.iterator();
		while (iterator.hasNext()) {
			Object obj = iterator.next();
			System.out.println(obj);
			SenderBean retransmitterBean = (SenderBean) obj;
			retransmitterBeanMgr.delete(retransmitterBean.getMessageID());
		}
		
		//removing the createSeqMgrEntry
		CreateSeqBean findCreateSequenceBean = new CreateSeqBean ();
		findCreateSequenceBean.setInternalSequenceID(internalSequenceId);
		collection = createSeqBeanMgr.find(findCreateSequenceBean);
		iterator = collection.iterator();
		while (iterator.hasNext()) {
			CreateSeqBean createSeqBean = (CreateSeqBean) iterator.next();
			createSeqBeanMgr.delete(createSeqBean.getCreateSeqMsgID());
		}
		
		//removing sequence properties
		SequencePropertyBean findSequencePropertyBean1 = new SequencePropertyBean ();
		findSequencePropertyBean1.setSequenceID(internalSequenceId);
		collection = sequencePropertyBeanMgr.find(findSequencePropertyBean1);
		iterator = collection.iterator();
		while (iterator.hasNext()) {
			SequencePropertyBean sequencePropertyBean = (SequencePropertyBean) iterator.next();
			sequencePropertyBeanMgr.delete(sequencePropertyBean.getSequenceID(),sequencePropertyBean.getName());
		}
		
		SequencePropertyBean findSequencePropertyBean2 = new SequencePropertyBean ();
		findSequencePropertyBean2.setSequenceID(internalSequenceId);
		collection = sequencePropertyBeanMgr.find(findSequencePropertyBean2);
		iterator = collection.iterator();
		while (iterator.hasNext()) {
			SequencePropertyBean sequencePropertyBean = (SequencePropertyBean) iterator.next();
			sequencePropertyBeanMgr.delete(sequencePropertyBean.getSequenceID(),sequencePropertyBean.getName());
		}
	}
	
	private boolean isProportyDeletable (String name) {
		boolean deleatable = true;
		
		if (Sandesha2Constants.SequenceProperties.TERMINATE_ADDED.equals(name))
			deleatable = false;
		
		if (Sandesha2Constants.SequenceProperties.NO_OF_OUTGOING_MSGS_ACKED.equals(name))
			deleatable = false;
		
		if (Sandesha2Constants.SequenceProperties.INTERNAL_SEQUENCE_ID.equals(name))
			deleatable = false;
		
		return deleatable;
	}
}
