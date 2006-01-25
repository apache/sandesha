package org.apache.sandesha2.transport;

import javax.xml.namespace.QName;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.HandlerDescription;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.transport.TransportSender;
import org.apache.sandesha2.RMMsgContext;
import org.apache.sandesha2.Sandesha2Constants;
import org.apache.sandesha2.SandeshaException;
import org.apache.sandesha2.storage.StorageManager;
import org.apache.sandesha2.storage.Transaction;
import org.apache.sandesha2.storage.beanmanagers.SenderBeanMgr;
import org.apache.sandesha2.storage.beans.SenderBean;
import org.apache.sandesha2.util.MsgInitializer;
import org.apache.sandesha2.util.SandeshaUtil;

public class Sandesha2TransportSender implements TransportSender  {

	private String messageStoreKey = null;
	
	public void invoke(MessageContext msgContext) throws AxisFault {
		
		//setting the correct transport sender.
		TransportSender sender = (TransportSender) msgContext.getProperty(Sandesha2Constants.ORIGINAL_TRANSPORT_SENDER);
		
		if (sender==null)
			throw new SandeshaException ("Original transport sender is not present");
		
		msgContext.getTransportOut().setSender(sender);
		
		String key =  (String) msgContext.getProperty(Sandesha2Constants.MESSAGE_STORE_KEY);
		
		if (key==null)
			throw new SandeshaException ("Cant store message without the storage key");
		
		ConfigurationContext configurationContext = msgContext.getConfigurationContext();
		StorageManager storageManager = SandeshaUtil.getSandeshaStorageManager(configurationContext);
		
		Transaction messageStoreTransaction = storageManager.getTransaction();
		storageManager.storeMessageContext(key,msgContext);
		messageStoreTransaction.commit();
		
		Transaction transaction = storageManager.getTransaction();
		//setting send=true (if requestd) for the message.
		SenderBeanMgr senderBeanMgr = storageManager.getRetransmitterBeanMgr();

		RMMsgContext rmMsg = MsgInitializer.initializeMessage(msgContext);
		
		SenderBean senderBean = senderBeanMgr.retrieveFromMessageRefKey(key);

	 	String setSendToTrue = (String) msgContext.getProperty(Sandesha2Constants.SET_SEND_TO_TRUE);
		if (Sandesha2Constants.VALUE_TRUE.equals(setSendToTrue)) {

			senderBean.setSend(true);
			senderBeanMgr.update(senderBean);
		}
		
		transaction.commit();
		
		msgContext.setProperty(Sandesha2Constants.QUALIFIED_FOR_SENDING,Sandesha2Constants.VALUE_TRUE);
	}

	//Below methods are not used
	public void cleanUp(MessageContext msgContext) throws AxisFault {}

	public void init(ConfigurationContext confContext, TransportOutDescription transportOut) throws AxisFault {}

	public void cleanup() throws AxisFault {}

	public HandlerDescription getHandlerDesc() {return null;}

	public QName getName() { return null;}

	public Parameter getParameter(String name) {  return null; }

	public void init(HandlerDescription handlerdesc) {}

}
