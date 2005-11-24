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
import org.apache.sandesha2.util.SandeshaUtil;

/**
 * @author Chamikara Jayalath <chamikaramj@gmail.com>
 */

public class TerminateManager {

	public static void terminateReceivingSide (ConfigurationContext configContext, String sequenceID) throws SandeshaException {
		StorageManager storageManager = SandeshaUtil.getSandeshaStorageManager(configContext);
		NextMsgBeanMgr nextMsgBeanMgr = storageManager.getNextMsgBeanMgr();
		
		//removing nextMsgMgr entries
		NextMsgBean findNextMsgBean = new NextMsgBean ();
		findNextMsgBean.setSequenceId(sequenceID);
		Collection collection = nextMsgBeanMgr.find(findNextMsgBean);
		Iterator iterator = collection.iterator();
		while (iterator.hasNext()) {
			NextMsgBean nextMsgBean = (NextMsgBean) iterator.next();
			nextMsgBeanMgr.delete(nextMsgBean.getSequenceId());
		}
		
		if(Constants.QOS.DeliveryAssurance.DEFAULT_DELIVERY_ASSURANCE!=Constants.QOS.DeliveryAssurance.IN_ORDER) { 
			terminateAfterInvocation(configContext,sequenceID);
		}

	}
	
	public static void terminateAfterInvocation (ConfigurationContext configContext, String sequenceID) throws SandeshaException {
		StorageManager storageManager = SandeshaUtil.getSandeshaStorageManager(configContext);
		SequencePropertyBeanMgr sequencePropertyBeanMgr = storageManager.getSequencePropretyBeanMgr();
		InvokerBeanMgr storageMapBeanMgr = storageManager.getStorageMapBeanMgr();

		//removing storageMap entries
		InvokerBean findStorageMapBean = new InvokerBean ();
		findStorageMapBean.setSequenceId(sequenceID);
		Collection collection = storageMapBeanMgr.find(findStorageMapBean);
		Iterator iterator = collection.iterator();
		while (iterator.hasNext()) {
			InvokerBean storageMapBean = (InvokerBean) iterator.next();
			storageMapBeanMgr.delete(storageMapBean.getKey());
		}
		
		//TODO - refine below (removing sequence properties of the receiving side).
		//removing sequencePropertyEntries.
//		SequencePropertyBean findSequencePropBean = new SequencePropertyBean ();
//		findSequencePropBean.setSequenceId(sequenceID);
//		collection = sequencePropertyBeanMgr.find(findSequencePropBean);
//		iterator = collection.iterator();
//		while (iterator.hasNext()) {
//			SequencePropertyBean sequencePropertyBean = (SequencePropertyBean) iterator.next();
//			boolean propertyRequiredForResponses = isRequiredForResponseSide (sequencePropertyBean.getName());
//			if (!propertyRequiredForResponses)
//				sequencePropertyBeanMgr.delete(sequencePropertyBean.getSequenceId(),sequencePropertyBean.getName());
//		}
		
		SequencePropertyBean allSequenceBean = sequencePropertyBeanMgr.retrieve(Constants.SequenceProperties.ALL_SEQUENCES,Constants.SequenceProperties.INCOMING_SEQUENCE_LIST);
		ArrayList allSequenceList = (ArrayList) allSequenceBean.getValue();
		
		allSequenceList.remove(sequenceID);
	}
	
	private static boolean isRequiredForResponseSide (String name) {
		if (name==null && name.equals(Constants.SequenceProperties.LAST_OUT_MESSAGE))
			return false;
		
		if (name.equals(Constants.SequenceProperties.LAST_OUT_MESSAGE))
			return false;
		
		return false;
	}
	
	public static void terminateSendingSide (ConfigurationContext configContext, String sequenceID) throws SandeshaException {
		StorageManager storageManager = SandeshaUtil.getSandeshaStorageManager(configContext);
		SequencePropertyBeanMgr sequencePropertyBeanMgr = storageManager.getSequencePropretyBeanMgr();
		SenderBeanMgr retransmitterBeanMgr = storageManager.getRetransmitterBeanMgr();
		CreateSeqBeanMgr createSeqBeanMgr = storageManager.getCreateSeqBeanMgr();
		
		SequencePropertyBean internalSequenceBean = sequencePropertyBeanMgr.retrieve(sequenceID,Constants.SequenceProperties.INTERNAL_SEQUENCE_ID);
		if (internalSequenceBean==null)
			throw new SandeshaException ("TempSequence entry not found");
		
		String internalSequenceId = (String) internalSequenceBean.getValue();
		
		//removing retransmitterMgr entries
		SenderBean findRetransmitterBean = new SenderBean ();
		findRetransmitterBean.setInternalSequenceId(internalSequenceId);
		Collection collection = retransmitterBeanMgr.find(findRetransmitterBean);
		Iterator iterator = collection.iterator();
		while (iterator.hasNext()) {
			SenderBean retransmitterBean = (SenderBean) iterator.next();
			retransmitterBeanMgr.delete(retransmitterBean.getMessageId());
		}
		
		//removing the createSeqMgrEntry
		CreateSeqBean findCreateSequenceBean = new CreateSeqBean ();
		findCreateSequenceBean.setInternalSequenceId(internalSequenceId);
		collection = createSeqBeanMgr.find(findCreateSequenceBean);
		iterator = collection.iterator();
		while (iterator.hasNext()) {
			CreateSeqBean createSeqBean = (CreateSeqBean) iterator.next();
			createSeqBeanMgr.delete(createSeqBean.getCreateSeqMsgId());
		}
		
		//removing sequence properties
		SequencePropertyBean findSequencePropertyBean1 = new SequencePropertyBean ();
		findSequencePropertyBean1.setSequenceId(internalSequenceId);
		collection = sequencePropertyBeanMgr.find(findSequencePropertyBean1);
		iterator = collection.iterator();
		while (iterator.hasNext()) {
			SequencePropertyBean sequencePropertyBean = (SequencePropertyBean) iterator.next();
			sequencePropertyBeanMgr.delete(sequencePropertyBean.getSequenceId(),sequencePropertyBean.getName());
		}
		
		SequencePropertyBean findSequencePropertyBean2 = new SequencePropertyBean ();
		findSequencePropertyBean2.setSequenceId(internalSequenceId);
		collection = sequencePropertyBeanMgr.find(findSequencePropertyBean2);
		iterator = collection.iterator();
		while (iterator.hasNext()) {
			SequencePropertyBean sequencePropertyBean = (SequencePropertyBean) iterator.next();
			sequencePropertyBeanMgr.delete(sequencePropertyBean.getSequenceId(),sequencePropertyBean.getName());
		}
		
	}
}
