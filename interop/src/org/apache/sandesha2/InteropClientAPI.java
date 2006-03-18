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

import javax.xml.namespace.QName;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.sandesha2.client.Sandesha2ClientAPI;
import org.apache.sandesha2.client.reports.SequenceReport;
import org.apache.sandesha2.storage.StorageManager;
import org.apache.sandesha2.storage.beanmanagers.SequencePropertyBeanMgr;
import org.apache.sandesha2.storage.beans.SequencePropertyBean;
import org.apache.sandesha2.util.SandeshaUtil;
import org.apache.sandesha2.wsrm.AckRequested;
import org.apache.sandesha2.wsrm.CloseSequence;
import org.apache.sandesha2.wsrm.Identifier;
import org.apache.ws.commons.om.OMElement;
import org.apache.ws.commons.soap.SOAP12Constants;
import org.apache.ws.commons.soap.SOAPEnvelope;
import org.apache.ws.commons.soap.SOAPFactory;
import org.apache.ws.commons.soap.impl.llom.soap11.SOAP11Factory;
import org.apache.ws.commons.soap.impl.llom.soap12.SOAP12Factory;

/**
 * Contains logic to remove all the storad data of a sequence.
 * Methods of this are called by sending side and the receiving side when appropriate
 * 
 * @author Chamikara Jayalath <chamikaramj@gmail.com>
 */

public class InteropClientAPI {
	
	public static String generateInternalSequenceIDForTheClientSide (String toEPR,String sequenceKey) {
		return SandeshaUtil.getInternalSequenceID(toEPR,sequenceKey);
	}
	
	//gives the out sequenceID if CS/CSR exchange is done. Otherwise a SandeshaException
	public static String getSequenceID (String to,String sequenceKey,ServiceClient serviceClient,ConfigurationContext configurationContext) throws AxisFault {
		
		String internalSequenceID = generateInternalSequenceIDForTheClientSide(to,sequenceKey);
		
		SequenceReport sequenceReport = Sandesha2ClientAPI.getOutgoingSequenceReport(to,sequenceKey , configurationContext);
		if (sequenceReport==null)
			throw new SandeshaException ("Cannot get a sequence report from the given data");
		
		if (sequenceReport.getSequenceStatus()!=SequenceReport.SEQUENCE_STATUS_ESTABLISHED) {
			throw new SandeshaException ("Sequence is not in a active state. Either create sequence response has not being received or sequence has been terminated," +
										 " cannot get sequenceID");
		}
		
		StorageManager storageManager = SandeshaUtil.getSandeshaStorageManager(configurationContext);
		SequencePropertyBeanMgr seqPropMgr = storageManager.getSequencePropretyBeanMgr();
		
		SequencePropertyBean sequenceIDBean = seqPropMgr.retrieve(internalSequenceID,Sandesha2Constants.SequenceProperties.OUT_SEQUENCE_ID);
		if (sequenceIDBean==null)
			throw new SandeshaException ("SequenceIdBean is not set");
		
		String sequenceID = sequenceIDBean.getValue();
		return sequenceID;
	}

	public static void sendAckRequest (String toEPR, String sequenceKey, ServiceClient serviceClient, ConfigurationContext configurationContext) throws AxisFault {
				
		Options options = serviceClient.getOptions();
		String rmSpecVersion = (String) options.getProperty(Sandesha2ClientAPI.RM_SPEC_VERSION);
		if (rmSpecVersion==null)
			rmSpecVersion = Sandesha2Constants.SPEC_VERSIONS.WSRM;
		
		if (Sandesha2Constants.SPEC_VERSIONS.WSRM.equals(rmSpecVersion)) {
			throw new SandeshaException ("Empty AckRequest messages can only be sent with the WSRX spec");
		}
		
		String internalSequenceID = SandeshaUtil.getInternalSequenceID(toEPR,sequenceKey);
		
		SequenceReport sequenceReport = Sandesha2ClientAPI.getOutgoingSequenceReport(internalSequenceID,configurationContext);
		if (sequenceReport==null)
			throw new SandeshaException ("Cannot generate the sequence report for the given internalSequenceID");
		if (sequenceReport.getSequenceStatus()!=SequenceReport.SEQUENCE_STATUS_ESTABLISHED)
			throw new SandeshaException ("Canot send the ackRequest message since it is not active");
		
		StorageManager storageManager = SandeshaUtil
				.getSandeshaStorageManager(configurationContext);

		SequencePropertyBeanMgr seqPropMgr = storageManager
			.getSequencePropretyBeanMgr();
		
		String outSequenceID = getSequenceID(toEPR,sequenceKey,serviceClient,configurationContext);
		
		String soapNamespaceURI = options.getSoapVersionURI();
		SOAPFactory factory = null;
		SOAPEnvelope dummyEnvelope = null;
		if (SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(soapNamespaceURI)) {
			factory = new SOAP11Factory ();
			dummyEnvelope = factory.getDefaultEnvelope();
		}else  {
			factory = new SOAP12Factory ();
			dummyEnvelope = factory.getDefaultEnvelope();
		}
		
		String rmNamespaceValue = SpecSpecificConstants.getRMNamespaceValue(rmSpecVersion);
		
		AckRequested ackRequested = new AckRequested (factory,rmNamespaceValue);
		Identifier identifier = new Identifier (factory,rmNamespaceValue);
		identifier.setIndentifer(outSequenceID);
		ackRequested.setIdentifier(identifier);
		
		
		ackRequested.toSOAPEnvelope(dummyEnvelope);
		
		OMElement ackRequestedHeaderBlock = dummyEnvelope.getHeader().getFirstChildWithName(new QName (rmNamespaceValue,Sandesha2Constants.WSRM_COMMON.ACK_REQUESTED));
		
		String oldAction = options.getAction();
//		String oldSOAPAction = options.getSoapAction();
		options.setAction(SpecSpecificConstants.getAckRequestAction(rmSpecVersion));
//		options.setAction(SpecSpecificConstants.getAckRequestSOAPAction(rmSpecVersion));
		
		System.out.println("Snding ack Rek");
		serviceClient.addHeader(ackRequestedHeaderBlock);
		
		serviceClient.fireAndForget(null);	
	    serviceClient.removeHeaders();
		options.setAction(oldAction);
//		options.setSoapAction(oldSOAPAction);
		
	}
		
	public static void closeSequence (String toEPR, String sequenceKey, ServiceClient serviceClient,ConfigurationContext configurationContext) throws AxisFault {

		String internalSequenceID = SandeshaUtil.getInternalSequenceID(toEPR,sequenceKey);
				
		SequenceReport sequenceReport = Sandesha2ClientAPI.getOutgoingSequenceReport(internalSequenceID,configurationContext);
		if (sequenceReport==null)
			throw new SandeshaException ("Cannot generate the sequence report for the given internalSequenceID");
		if (sequenceReport.getSequenceStatus()!=SequenceReport.SEQUENCE_STATUS_ESTABLISHED)
			throw new SandeshaException ("Canot close the sequence since it is not active");
		
		String sequenceID = getSequenceID(toEPR,sequenceKey,serviceClient,configurationContext);
		if (sequenceID==null)
			throw new SandeshaException ("Cannot find the sequenceID");
		
		Options options = serviceClient.getOptions();
		
		String rmSpecVersion = (String) options.getProperty(Sandesha2ClientAPI.RM_SPEC_VERSION);

		if (rmSpecVersion==null) 
			rmSpecVersion = SpecSpecificConstants.getDefaultSpecVersion ();
		
		if (!SpecSpecificConstants.isSequenceClosingAllowed (rmSpecVersion))
			throw new SandeshaException ("This rm version does not allow sequence closing");
		
		StorageManager storageManager = SandeshaUtil
				.getSandeshaStorageManager(configurationContext);

		SequencePropertyBeanMgr seqPropMgr = storageManager
			.getSequencePropretyBeanMgr();
		
		
		String oldAction = options.getAction();
		
		options.setAction(SpecSpecificConstants.getCloseSequenceAction (rmSpecVersion));		
		
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
		
		CloseSequence closeSequence = new CloseSequence (factory,rmNamespaceValue);
		Identifier identifier = new Identifier (factory,rmNamespaceValue);
		identifier.setIndentifer(sequenceID);
		closeSequence.setIdentifier(identifier);
		
		closeSequence.toSOAPEnvelope(dummyEnvelope);
		serviceClient.fireAndForget(dummyEnvelope.getBody().getFirstChildWithName(new QName (rmNamespaceValue,Sandesha2Constants.WSRM_COMMON.CLOSE_SEQUENCE)));

		options.setAction(oldAction);
	}
}
