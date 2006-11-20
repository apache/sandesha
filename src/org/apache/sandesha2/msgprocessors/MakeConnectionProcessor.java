package org.apache.sandesha2.msgprocessors;

import java.util.Collection;
import java.util.Iterator;
import java.util.Random;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.sandesha2.RMMsgContext;
import org.apache.sandesha2.Sandesha2Constants;
import org.apache.sandesha2.SandeshaException;
import org.apache.sandesha2.i18n.SandeshaMessageHelper;
import org.apache.sandesha2.i18n.SandeshaMessageKeys;
import org.apache.sandesha2.storage.StorageManager;
import org.apache.sandesha2.storage.beanmanagers.SenderBeanMgr;
import org.apache.sandesha2.storage.beans.SenderBean;
import org.apache.sandesha2.util.MsgInitializer;
import org.apache.sandesha2.util.SandeshaUtil;
import org.apache.sandesha2.workers.SenderWorker;
import org.apache.sandesha2.wsrm.Address;
import org.apache.sandesha2.wsrm.Identifier;
import org.apache.sandesha2.wsrm.MakeConnection;
import org.apache.sandesha2.wsrm.MessagePending;

/**
 * This class is responsible for processing MakeConnection request messages that come to the system.
 * MakeConnection is only supported by WSRM 1.1
 * Here a client can ask for reply messages using a polling mechanism, so even clients without real
 * endpoints can ask for reliable response messages.
 */
public class MakeConnectionProcessor implements MsgProcessor {

	/**
	 * Prosesses incoming MakeConnection request messages.
	 * A message is selected by the set of SenderBeans that are waiting to be sent.
	 * This is processed using a SenderWorker. 
	 */
	public boolean processInMessage(RMMsgContext rmMsgCtx) throws AxisFault {
		
		MakeConnection makeConnection = (MakeConnection) rmMsgCtx.getMessagePart(Sandesha2Constants.MessageParts.MAKE_CONNECTION);
		Address address = makeConnection.getAddress();
		Identifier identifier = makeConnection.getIdentifier();
		
		ConfigurationContext configurationContext = rmMsgCtx.getConfigurationContext();
		StorageManager storageManager = SandeshaUtil.getSandeshaStorageManager(configurationContext,configurationContext.getAxisConfiguration());
		
		SenderBeanMgr senderBeanMgr = storageManager.getSenderBeanMgr();
		
		//selecting the set of SenderBeans that suit the given criteria.
		SenderBean findSenderBean = new SenderBean ();
		findSenderBean.setSend(true);
		
		if (address!=null)
			findSenderBean.setWsrmAnonURI(address.getAddress());
		
		if (identifier!=null)
			findSenderBean.setSequenceID(identifier.getIdentifier());
		
		//finding the beans that go with the criteria of the passed SenderBean
		
		//beans with reSend=true
		findSenderBean.setReSend(true);
		Collection collection = senderBeanMgr.find(findSenderBean);
		
		//beans with reSend=false
		findSenderBean.setReSend (false);
		Collection collection2 = senderBeanMgr.find(findSenderBean);
		
		//all possible beans
		collection.addAll(collection2);
		
		//selecting a bean to send RANDOMLY. TODO- Should use a better mechanism.
		int size = collection.size();
		int itemToPick=-1;
		
		boolean pending = false;
		if (size>0) {
			Random random = new Random ();
			itemToPick = random.nextInt(size);
		}

		if (size>1)
			pending = true;  //there are more than one message to be delivered using the makeConnection.
							 //So the MessagePending header should have value true;
		
		Iterator it = collection.iterator();
		
		SenderBean senderBean = null;
		for (int item=0;item<size;item++) {
			
		    senderBean = (SenderBean) it.next();
			if (item==itemToPick)
				break;
		}

		if (senderBean==null) 
			return false;
			
		TransportOutDescription transportOut = rmMsgCtx.getMessageContext().getTransportOut();
		if (transportOut==null) {
			String message = SandeshaMessageHelper.getMessage(
					SandeshaMessageKeys.cantSendMakeConnectionNoTransportOut);
			throw new SandeshaException (message);
		}
			
		String messageStorageKey = senderBean.getMessageContextRefKey();
		MessageContext returnMessage = storageManager.retrieveMessageContext(messageStorageKey,configurationContext);
		RMMsgContext returnRMMsg = MsgInitializer.initializeMessage(returnMessage);
		
		
		addMessagePendingHeader (returnRMMsg,pending);
		
		setTransportProperties (returnMessage, rmMsgCtx);
		
		//setting that the response gets written written.
		//This will be used by transports. For e.g. CommonsHTTPTransportSender will send 200 OK, instead of 202.
		rmMsgCtx.getMessageContext().getOperationContext().setProperty(Constants.RESPONSE_WRITTEN , Constants.VALUE_TRUE);
		
		
		//running the MakeConnection through a SenderWorker.
		//This will allow Sandesha2 to consider both of following senarios equally.
		//	1. A message being sent by the Sender thread.
		//  2. A message being sent as a reply to an MakeConnection.
		SenderWorker worker = new SenderWorker (configurationContext,senderBean.getMessageID());
		worker.setTransportOut(rmMsgCtx.getMessageContext().getTransportOut());
		
		worker.run();
		return false;
	}
	
	private void addMessagePendingHeader (RMMsgContext returnMessage, boolean pending) throws SandeshaException {
		String rmNamespace = returnMessage.getRMNamespaceValue();
		MessagePending messagePending = new MessagePending (rmNamespace);
		messagePending.setPending(pending);
		
		messagePending.toSOAPEnvelope(returnMessage.getSOAPEnvelope());
		
	}

	public boolean processOutMessage(RMMsgContext rmMsgCtx) throws AxisFault {
		return false;
	}

	private void setTransportProperties (MessageContext returnMessage, RMMsgContext makeConnectionMessage) {
        returnMessage.setProperty(MessageContext.TRANSPORT_OUT,makeConnectionMessage.getProperty(MessageContext.TRANSPORT_OUT));
        returnMessage.setProperty(Constants.OUT_TRANSPORT_INFO,makeConnectionMessage.getProperty(Constants.OUT_TRANSPORT_INFO));
	}
}
