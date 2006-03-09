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
import java.util.Collection;
import java.util.Iterator;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.sandesha2.AcknowledgementManager;
import org.apache.sandesha2.Sandesha2Constants;
import org.apache.sandesha2.SandeshaException;
import org.apache.sandesha2.client.reports.RMReport;
import org.apache.sandesha2.client.reports.SequenceReport;
import org.apache.sandesha2.storage.StorageManager;
import org.apache.sandesha2.storage.Transaction;
import org.apache.sandesha2.storage.beanmanagers.CreateSeqBeanMgr;
import org.apache.sandesha2.storage.beanmanagers.NextMsgBeanMgr;
import org.apache.sandesha2.storage.beanmanagers.SequencePropertyBeanMgr;
import org.apache.sandesha2.storage.beans.CreateSeqBean;
import org.apache.sandesha2.storage.beans.NextMsgBean;
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
	public static String MESSAGE_NUMBER = "Sandesha2ClientAPIPropertyMessageNumber";
	public static String RM_SPEC_VERSION = "Sandesha2ClientAPIPropertyRMSpecVersion";
	public static String DUMMY_MESSAGE = "Sandesha2ClientAPIDummyMessage"; //If this property is set, even though this message will invoke the RM handlers, this will not be sent as an actual application message
	public static String VALUE_TRUE = "true";
	public static String VALUE_FALSE = "false";
	
	public static SequenceReport getOutgoingSequenceReport (String internalSequenceID,ConfigurationContext configurationContext) throws SandeshaException {
		
		SequenceReport sequenceReport = new SequenceReport ();
		sequenceReport.setSequenceDirection(SequenceReport.SEQUENCE_DIRECTION_OUT);
		
		StorageManager storageManager = SandeshaUtil.getSandeshaStorageManager(configurationContext);
		SequencePropertyBeanMgr seqPropMgr = storageManager.getSequencePropretyBeanMgr();
		CreateSeqBeanMgr createSeqMgr = storageManager.getCreateSeqBeanMgr();
		
		Transaction reportTransaction = storageManager.getTransaction();
		
		CreateSeqBean createSeqFindBean = new CreateSeqBean ();
		createSeqFindBean.setInternalSequenceID(internalSequenceID);
		
		CreateSeqBean createSeqBean = createSeqMgr.findUnique(createSeqFindBean);
		
		if (createSeqBean==null) {
			//check weather this is an terminated sequence.
			if (isTerminatedSequence(internalSequenceID,seqPropMgr)) {
				fillTerminatedOutgoingSequenceInfo (sequenceReport,internalSequenceID,seqPropMgr);
				
				return sequenceReport;
			}
			
			if (isTimedOutSequence(internalSequenceID,seqPropMgr)) {
				fillTimedoutOutgoingSequenceInfo (sequenceReport,internalSequenceID,seqPropMgr);
				
				return sequenceReport;
			}
			
			String message = "Unrecorder internalSequenceID";
			log.debug(message);
			return null;
		}
		
		String outSequenceID = createSeqBean.getSequenceID();
		if (outSequenceID==null) {
			 sequenceReport.setInternalSequenceID(internalSequenceID);
			 sequenceReport.setSequenceStatus(SequenceReport.SEQUENCE_STATUS_INITIAL);
			 sequenceReport.setSequenceDirection(SequenceReport.SEQUENCE_DIRECTION_OUT);
			 
			 return sequenceReport;
		}
		
		sequenceReport.setSequenceStatus(SequenceReport.SEQUENCE_STATUS_ESTABLISHED);
		
		fillOutgoingSequenceInfo(sequenceReport,outSequenceID,seqPropMgr);
		
		reportTransaction.commit();
		
		return sequenceReport;	
	}
	
	private static boolean isTerminatedSequence (String internalSequenceID, SequencePropertyBeanMgr seqPropMgr) throws SandeshaException { 
		SequencePropertyBean internalSequenceFindBean = new SequencePropertyBean ();
		internalSequenceFindBean.setValue(internalSequenceID);
		internalSequenceFindBean.setName(Sandesha2Constants.SequenceProperties.INTERNAL_SEQUENCE_ID);
		
		SequencePropertyBean internalSequenceBean = seqPropMgr.findUnique(internalSequenceFindBean);
		if (internalSequenceBean==null) {
			String message = "Internal sequence Bean is not available for the given sequence";
			log.debug (message);
			
			return false;
		}
		
		String outSequenceID = internalSequenceBean.getSequenceID();
	
		SequencePropertyBean sequenceTerminatedBean = seqPropMgr.retrieve(outSequenceID,Sandesha2Constants.SequenceProperties.SEQUENCE_TERMINATED);
		if (sequenceTerminatedBean!=null && Sandesha2Constants.VALUE_TRUE.equals(sequenceTerminatedBean.getValue())) {
			return true;
		}
		
		return false;
		
	}
	
	private static boolean isTimedOutSequence (String internalSequenceID, SequencePropertyBeanMgr seqPropMgr) throws SandeshaException { 
		SequencePropertyBean internalSequenceFindBean = new SequencePropertyBean ();
		internalSequenceFindBean.setValue(internalSequenceID);
		internalSequenceFindBean.setName(Sandesha2Constants.SequenceProperties.INTERNAL_SEQUENCE_ID);
		
		SequencePropertyBean internalSequenceBean = seqPropMgr.findUnique(internalSequenceFindBean);
		if (internalSequenceBean==null) {
			String message = "Internal sequence Bean is not available for the given sequence";
			log.debug (message);
			
			return false;
		}
		
		String outSequenceID = internalSequenceBean.getSequenceID();
	
		SequencePropertyBean sequenceTerminatedBean = seqPropMgr.retrieve(outSequenceID,Sandesha2Constants.SequenceProperties.SEQUENCE_TIMED_OUT);
		if (sequenceTerminatedBean!=null && Sandesha2Constants.VALUE_TRUE.equals(sequenceTerminatedBean.getValue())) {
			return true;
		}
		
		return false;
	}
	
	private static void fillTerminatedOutgoingSequenceInfo (SequenceReport report,String internalSequenceID,SequencePropertyBeanMgr seqPropMgr) throws SandeshaException  { 
		SequencePropertyBean internalSequenceFindBean = new SequencePropertyBean ();
		internalSequenceFindBean.setValue(internalSequenceID);
		internalSequenceFindBean.setName(Sandesha2Constants.SequenceProperties.INTERNAL_SEQUENCE_ID);
		
		SequencePropertyBean internalSequenceBean = seqPropMgr.findUnique(internalSequenceFindBean);
		if (internalSequenceBean==null) {
			String message = "Not a valid terminated sequence. Internal sequence Bean is not available for the given sequence";
			log.debug (message);
			
			throw new SandeshaException (message);
		}
		
		report.setSequenceStatus(SequenceReport.SEQUENCE_STATUS_TERMINATED);
		
		String outSequenceID = internalSequenceBean.getSequenceID();
		fillOutgoingSequenceInfo(report,outSequenceID,seqPropMgr);
	}
	
	private static void fillTimedoutOutgoingSequenceInfo (SequenceReport report,String internalSequenceID, SequencePropertyBeanMgr seqPropMgr) throws SandeshaException  { 
		SequencePropertyBean internalSequenceFindBean = new SequencePropertyBean ();
		internalSequenceFindBean.setValue(internalSequenceID);
		internalSequenceFindBean.setName(Sandesha2Constants.SequenceProperties.INTERNAL_SEQUENCE_ID);
		
		SequencePropertyBean internalSequenceBean = seqPropMgr.findUnique(internalSequenceFindBean);
		if (internalSequenceBean==null) {
			String message = "Not a valid timedOut sequence. Internal sequence Bean is not available for the given sequence";
			log.debug (message);
			
			throw new SandeshaException (message);
		}
		
		report.setSequenceStatus(SequenceReport.SEQUENCE_STATUS_TIMED_OUT);
		String outSequenceID = internalSequenceBean.getSequenceID();
		fillOutgoingSequenceInfo(report,outSequenceID,seqPropMgr);
	}
	
	private static void fillOutgoingSequenceInfo (SequenceReport report,String outSequenceID, SequencePropertyBeanMgr seqPropMgr) throws SandeshaException  { 
		ArrayList completedMessageList =  AcknowledgementManager.getClientCompletedMessagesList (outSequenceID,seqPropMgr);
		
		Iterator iter = completedMessageList.iterator();
		while (iter.hasNext()) {
			Long lng = new Long (Long.parseLong((String) iter.next()));
			report.addCompletedMessage(lng);
		}
	}
	
	
	public static SequenceReport getOutgoingSequenceReport (String to, String sequenceKey,ConfigurationContext configurationContext) throws SandeshaException {
		
		String internalSequenceID = SandeshaUtil.getInternalSequenceID (to,sequenceKey);
		return getOutgoingSequenceReport(internalSequenceID,configurationContext);
	
	}
	
	public static SequenceReport getIncomingSequenceReport (String sequenceID,ConfigurationContext configCtx) throws SandeshaException {
		
		StorageManager storageManager = SandeshaUtil.getSandeshaStorageManager(configCtx);
		SequencePropertyBeanMgr seqPropMgr = storageManager.getSequencePropretyBeanMgr();
		
		SequenceReport sequenceReport = new SequenceReport ();

		ArrayList completedMessageList =  AcknowledgementManager.getServerCompletedMessagesList (sequenceID,seqPropMgr);

		Iterator iter = completedMessageList.iterator();
		while (iter.hasNext()) {
			Long lng = new Long (Long.parseLong((String) iter.next()));
			sequenceReport.addCompletedMessage(lng);
		}
		
		sequenceReport.setSequenceDirection(SequenceReport.SEQUENCE_DIRECTION_IN);

		sequenceReport.setSequenceStatus(getServerSequenceStatus (sequenceID,storageManager));
	
		return sequenceReport;
	}
	
	private static byte getServerSequenceStatus (String sequenceID,StorageManager storageManager) throws SandeshaException {
		
		SequencePropertyBeanMgr seqPropMgr = storageManager.getSequencePropretyBeanMgr();
		
		SequencePropertyBean terminatedBean = seqPropMgr.retrieve(sequenceID,Sandesha2Constants.SequenceProperties.SEQUENCE_TERMINATED);
		if (terminatedBean!=null) {
			return SequenceReport.SEQUENCE_STATUS_TERMINATED;
		}
		
		SequencePropertyBean timedOutBean = seqPropMgr.retrieve(sequenceID,Sandesha2Constants.SequenceProperties.SEQUENCE_TIMED_OUT);
		if (timedOutBean!=null) {
			return SequenceReport.SEQUENCE_STATUS_TIMED_OUT;
		}
		
		NextMsgBeanMgr nextMsgMgr = storageManager.getNextMsgBeanMgr();
		NextMsgBean nextMsgBean = nextMsgMgr.retrieve(sequenceID);
		
		if (nextMsgBean!=null) {
			return SequenceReport.SEQUENCE_STATUS_ESTABLISHED;
		}
		
		throw new SandeshaException ("Unrecorded sequenceID");
	}
	
	/**
	 * RM Report gives the details of incoming and outgoing sequences.
	 * The outgoing sequence have to pass the initial state (CS/CSR exchange) to be included in a RMReport
	 * 
	 * @param configurationContext
	 * @return
	 * @throws SandeshaException
	 */
	public static RMReport getRMReport (ConfigurationContext configurationContext) throws SandeshaException {
		
		StorageManager storageManager = SandeshaUtil.getSandeshaStorageManager(configurationContext);
		SequencePropertyBeanMgr seqPropMgr = storageManager.getSequencePropretyBeanMgr();
		
		RMReport rmReport = new RMReport ();

		SequencePropertyBean internalSequenceFindBean = new SequencePropertyBean ();
		internalSequenceFindBean.setName(Sandesha2Constants.SequenceProperties.INTERNAL_SEQUENCE_ID);
		
		Collection collection = seqPropMgr.find(internalSequenceFindBean);
		Iterator iterator = collection.iterator();
		while (iterator.hasNext()) {
			SequencePropertyBean bean = (SequencePropertyBean) iterator.next();
			String sequenceID = bean.getSequenceID();
			rmReport.addToOutgoingSequenceList (sequenceID);
			rmReport.addToOutgoingInternalSequenceMap(sequenceID,bean.getValue());
			
			SequenceReport report = getOutgoingSequenceReport(bean.getValue(),configurationContext);
			
			rmReport.addToNoOfCompletedMessagesMap(sequenceID ,report.getCompletedMessages().size());
			rmReport.addToSequenceStatusMap(sequenceID ,report.getSequenceStatus());
		}
		
		
		//incoming sequences
		SequencePropertyBean serverCompletedMsgsFindBean = new SequencePropertyBean ();
		serverCompletedMsgsFindBean.setName(Sandesha2Constants.SequenceProperties.SERVER_COMPLETED_MESSAGES);
		
		Collection serverCompletedMsgsBeans = seqPropMgr.find(serverCompletedMsgsFindBean);
		Iterator iter = serverCompletedMsgsBeans.iterator();
		while (iter.hasNext()) {
			SequencePropertyBean serverCompletedMsgsBean = (SequencePropertyBean) iter.next();
			String sequenceID = serverCompletedMsgsBean.getSequenceID();
			rmReport.addToIncomingSequenceList(sequenceID);
			
			SequenceReport sequenceReport = getIncomingSequenceReport(sequenceID,configurationContext);
			
			rmReport.addToNoOfCompletedMessagesMap(sequenceID,sequenceReport.getCompletedMessages().size());
			rmReport.addToSequenceStatusMap(sequenceID,sequenceReport.getSequenceStatus());
		}
		
		return rmReport;
	}
	
	public String InternalSequenceID (String to, String sequenceKey) {
		return SandeshaUtil.getInternalSequenceID(to,sequenceKey);
	}
	

}
