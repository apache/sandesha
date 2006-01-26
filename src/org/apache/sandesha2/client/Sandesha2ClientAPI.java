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
 */

package org.apache.sandesha2.client;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.sandesha2.AcknowledgementManager;
import org.apache.sandesha2.Sandesha2Constants;
import org.apache.sandesha2.SandeshaException;
import org.apache.sandesha2.storage.StorageManager;
import org.apache.sandesha2.storage.Transaction;
import org.apache.sandesha2.storage.beanmanagers.SequencePropertyBeanMgr;
import org.apache.sandesha2.storage.beans.SequencePropertyBean;
import org.apache.sandesha2.util.SandeshaUtil;
import org.apache.sandesha2.util.SequenceManager;

/**
 * Contains all the Sandesha2Constants of Sandesha2.
 * Please see sub-interfaces to see grouped data.
 * 
 * @author Chamikara Jayalath <chamikaramj@gmail.com>
 */

public class Sandesha2ClientAPI {

	private static Log log = LogFactory.getLog(Sandesha2ClientAPI.class);
	
	public static String AcksTo = "Sandesha2ClientAPIPropertyAcksTo";
	public static String LAST_MESSAGE = "Sandesha2ClientAPIPropertyWSRMLastMessage";
	public static String OFFERED_SEQUENCE_ID = "Sandesha2ClientAPIPropertyOfferedSequenceId";
	public static String SANDESHA_DEBUG_MODE = "Sandesha2ClientAPIPropertyDebugMode";
	public static String SEQUENCE_KEY = "Sandesha2ClientAPIPropertySequenceKey";

	public static SequenceReport getOutgoingSequenceReport (String to, String sequenceKey,ConfigurationContext configurationContext) throws SandeshaException {
		
		String internalSequenceID = SandeshaUtil.getInternalSequenceID (to,sequenceKey);
		SequenceReport sequenceReport = new SequenceReport ();
		
		StorageManager storageManager = SandeshaUtil.getSandeshaStorageManager(configurationContext);
		SequencePropertyBeanMgr seqpPropMgr = storageManager.getSequencePropretyBeanMgr();
		
		Transaction reportTransaction = storageManager.getTransaction();
		SequencePropertyBean findBean =  new SequencePropertyBean ();
		findBean.setName(Sandesha2Constants.SequenceProperties.INTERNAL_SEQUENCE_ID);
		findBean.setValue(internalSequenceID);
		SequencePropertyBean internalSequenceBean = seqpPropMgr.findUnique(findBean);
		
		if (internalSequenceBean==null) {
			log.debug("internal sequence bean is null.");
			throw new SandeshaException ("Data not available to give the sequence report");
		}
		
		String sequenceID = internalSequenceBean.getSequenceID();
		
		//finding the actual seq
		ArrayList completedMessageList =  AcknowledgementManager.getCompletedMessagesList (sequenceID,configurationContext);
		
		Iterator iter = completedMessageList.iterator();
		while (iter.hasNext()) {
			Long lng = new Long (Long.parseLong((String) iter.next()));
			sequenceReport.addCompletedMessage(lng);
		}
		
		sequenceReport.setSequenceDirection(SequenceReport.SEQUENCE_DIRECTION_OUT);
		boolean completed  = SequenceManager.isOutGoingSequenceCompleted(internalSequenceID,configurationContext);
		if (completed)
			sequenceReport.setSequenceStatus(SequenceReport.SEQUENCE_STATUS_COMPLETED);
		
		//TODO complete
		
		
		reportTransaction.commit();
		
		return sequenceReport;
	}
	
	public static SequenceReport getIncomingSequenceReport (String sequenceID,ConfigurationContext configurationContext) throws SandeshaException {
		
		SequenceReport sequenceReport = new SequenceReport ();

		ArrayList completedMessageList =  AcknowledgementManager.getCompletedMessagesList (sequenceID,configurationContext);

		Iterator iter = completedMessageList.iterator();
		while (iter.hasNext()) {
			Long lng = new Long (Long.parseLong((String) iter.next()));
			sequenceReport.addCompletedMessage(lng);
		}
		
		sequenceReport.setSequenceDirection(SequenceReport.SEQUENCE_DIRECTION_IN);
		boolean completed  = SequenceManager.isIncomingSequenceCompleted(sequenceID,configurationContext);
		if (completed)
			sequenceReport.setSequenceStatus(SequenceReport.SEQUENCE_STATUS_COMPLETED);
		
		//TODO complete
		
		return sequenceReport;
	}
	
	public static RMReport getRMReport1 () {
		RMReport rmReport = new RMReport ();
		return rmReport;
	}
}
