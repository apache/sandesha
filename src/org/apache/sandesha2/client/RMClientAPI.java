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

import javax.xml.namespace.QName;

import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.impl.llom.soap11.SOAP11Factory;
import org.apache.axiom.soap.impl.llom.soap12.SOAP12Factory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.client.async.AsyncResult;
import org.apache.axis2.client.async.Callback;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.sandesha2.Sandesha2Constants;
import org.apache.sandesha2.SandeshaException;
import org.apache.sandesha2.storage.StorageManager;
import org.apache.sandesha2.storage.Transaction;
import org.apache.sandesha2.storage.beanmanagers.CreateSeqBeanMgr;
import org.apache.sandesha2.storage.beanmanagers.NextMsgBeanMgr;
import org.apache.sandesha2.storage.beanmanagers.SequencePropertyBeanMgr;
import org.apache.sandesha2.storage.beans.CreateSeqBean;
import org.apache.sandesha2.storage.beans.NextMsgBean;
import org.apache.sandesha2.storage.beans.SequencePropertyBean;
import org.apache.sandesha2.util.AcknowledgementManager;
import org.apache.sandesha2.util.SandeshaUtil;
import org.apache.sandesha2.util.SpecSpecificConstants;
import org.apache.sandesha2.wsrm.Identifier;
import org.apache.sandesha2.wsrm.TerminateSequence;

/**
 * Contains all the Sandesha2Constants of Sandesha2.
 * Please see sub-interfaces to see grouped data.
 * 
 * @author Chamikara Jayalath <chamikaramj@gmail.com>
 */

public class RMClientAPI {

	private static Log log = LogFactory.getLog(RMClientAPI.class);
	
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
		report.setSequenceID(outSequenceID);
		
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
		while (iter.hasNext()) {;
			sequenceReport.addCompletedMessage((Long) iter.next());
		}
		
		sequenceReport.setSequenceID(sequenceID);
		sequenceReport.setInternalSequenceID(sequenceID);  //for the incoming side   internalSequenceID=sequenceID
		sequenceReport.setSequenceDirection(SequenceReport.SEQUENCE_DIRECTION_IN);

		sequenceReport.setSequenceStatus(getServerSequenceStatus (sequenceID,storageManager));
	
		return sequenceReport;
	}
	
	
	public static ArrayList getAllIncomingSequenceReports (ConfigurationContext configCtx) throws SandeshaException {
		
		RMReport report = getRMReport(configCtx);
		ArrayList incomingSequenceIDs = report.getIncomingSequenceList();
		Iterator incomingSequenceIDIter = incomingSequenceIDs.iterator();
		
		ArrayList incomingSequenceReports = new ArrayList ();

		while (incomingSequenceIDIter.hasNext()) {
			String sequnceID = (String) incomingSequenceIDIter.next();
			SequenceReport incomingSequenceReport = getIncomingSequenceReport(sequnceID,configCtx);
			if (incomingSequenceReport==null) {
				throw new SandeshaException ("An incoming sequence report is not present for the given sequenceID");
			}
			
			incomingSequenceReports.add(incomingSequenceReport);
		}
		
		return incomingSequenceReports;
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
	
	public static String getInternalSequenceID (String to, String sequenceKey) {
		return SandeshaUtil.getInternalSequenceID(to,sequenceKey);
	}
	

	public static void terminateSequence (String toEPR, String sequenceKey, ServiceClient serviceClient,ConfigurationContext configurationContext) throws SandeshaException { 

		String internalSequenceID = SandeshaUtil.getInternalSequenceID(toEPR,sequenceKey);
				
		SequenceReport sequenceReport = RMClientAPI.getOutgoingSequenceReport(internalSequenceID,configurationContext);
		if (sequenceReport==null)
			throw new SandeshaException ("Cannot generate the sequence report for the given internalSequenceID");
		if (sequenceReport.getSequenceStatus()!=SequenceReport.SEQUENCE_STATUS_ESTABLISHED)
			throw new SandeshaException ("Canot terminate the sequence since it is not active");
		
		StorageManager storageManager = SandeshaUtil.getSandeshaStorageManager(configurationContext);
		SequencePropertyBeanMgr seqPropMgr = storageManager.getSequencePropretyBeanMgr();
		SequencePropertyBean sequenceIDBean = seqPropMgr.retrieve(internalSequenceID,Sandesha2Constants.SequenceProperties.OUT_SEQUENCE_ID);
		if (sequenceIDBean==null)
			throw new SandeshaException ("SequenceIdBean is not set");
		
		String sequenceID = sequenceIDBean.getValue();

		if (sequenceID==null)
			throw new SandeshaException ("Cannot find the sequenceID");
		
		Options options = serviceClient.getOptions();
		
		String rmSpecVersion = (String) options.getProperty(RMClientConstants.RM_SPEC_VERSION);

		if (rmSpecVersion==null) 
			rmSpecVersion = SpecSpecificConstants.getDefaultSpecVersion ();
		
		String oldAction = options.getAction();
		
		options.setAction(SpecSpecificConstants.getTerminateSequenceAction(rmSpecVersion));		
		
		SOAPEnvelope dummyEnvelope = null;
		SOAPFactory factory = null;
		String soapNamespaceURI = options.getSoapVersionURI();
		if (SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(soapNamespaceURI)) {
			factory = new SOAP12Factory ();
			dummyEnvelope = factory.getDefaultEnvelope();
		}else  {
			factory = new SOAP11Factory ();
			dummyEnvelope = factory.getDefaultEnvelope();
		}
		
		String rmNamespaceValue = SpecSpecificConstants.getRMNamespaceValue(rmSpecVersion);
		
		TerminateSequence terminateSequence = new TerminateSequence (factory,rmNamespaceValue);
		Identifier identifier = new Identifier (factory,rmNamespaceValue);
		identifier.setIndentifer(sequenceID);
		terminateSequence.setIdentifier(identifier);
		
		terminateSequence.toSOAPEnvelope(dummyEnvelope);
		
	    String oldSequenceKey = (String) options.getProperty(RMClientConstants.SEQUENCE_KEY);
	    options.setProperty(RMClientConstants.SEQUENCE_KEY,sequenceKey);
		try {
			DummyCallback callback = new RMClientAPI().new DummyCallback();
			serviceClient.fireAndForget(dummyEnvelope.getBody().getFirstChildWithName(new QName (rmNamespaceValue,Sandesha2Constants.WSRM_COMMON.TERMINATE_SEQUENCE))); 
		} catch (AxisFault e) {
			throw new SandeshaException ("Could not invoke the service client", e);
		}

		if (oldSequenceKey!=null)
			options.setProperty(RMClientConstants.SEQUENCE_KEY,oldSequenceKey);
		
//		options.setAction(oldAction);
	}
	
	private class DummyCallback extends Callback {

		public void onComplete(AsyncResult result) {
			// TODO Auto-generated method stub
			System.out.println("Error: dummy callback was called");
		}

		public void onError(Exception e) {
			// TODO Auto-generated method stub
			System.out.println("Error: dummy callback received an error");
			
		}
		
	}
	
	//This blocks the system until the messages u have sent hv been completed.
	public void blockForCompletion () {
		
	}
	
}
