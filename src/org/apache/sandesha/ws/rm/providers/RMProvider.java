/*
 * Created on Apr 23, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.apache.sandesha.ws.rm.providers;

import org.apache.axis.AxisEngine;
import org.apache.axis.AxisFault;
import org.apache.axis.Constants;
import org.apache.axis.Message;
import org.apache.axis.MessageContext;
import org.apache.axis.client.Call;
import org.apache.axis.description.OperationDesc;
import org.apache.axis.description.ParameterDesc;
import org.apache.axis.description.ServiceDesc;
import org.apache.axis.enum.Style;
import org.apache.axis.handlers.soap.SOAPService;
import org.apache.axis.message.RPCElement;
import org.apache.axis.message.RPCHeaderParam;
import org.apache.axis.message.RPCParam;
import org.apache.axis.message.SOAPBodyElement;
import org.apache.axis.message.SOAPEnvelope;
import org.apache.axis.message.addressing.Address;
import org.apache.axis.message.addressing.AddressingHeaders;
import org.apache.axis.message.addressing.From;
import org.apache.axis.soap.SOAPConstants;
import org.apache.axis.utils.JavaUtils;
import org.apache.axis.utils.Messages;
import org.xml.sax.SAXException;

import javax.xml.namespace.QName;
import javax.xml.rpc.holders.Holder;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;
import org.apache.axis.providers.java.RPCProvider;


import org.apache.sandesha.RMMessage;
import org.apache.sandesha.RMMessageContext;
import org.apache.sandesha.RMMessageController;
import org.apache.sandesha.RMSequence;

import org.apache.sandesha.client.ClientMessageController;
import org.apache.sandesha.server.ServerMessageController;
import org.apache.sandesha.test.RMDisplayer;
import org.apache.sandesha.ws.rm.RMHeaders;
import org.apache.sandesha.ws.rm.Sequence;
import org.apache.sandesha.ws.rm.SequenceAcknowledgement;
import org.apache.sandesha.ws.utility.Identifier;

/**
 * @author h
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class RMProvider extends RPCProvider {
	public void processMessage(
		MessageContext msgContext,
		SOAPEnvelope reqEnv,
		SOAPEnvelope resEnv,
		Object obj)
		throws Exception {
		if (log.isDebugEnabled()) {
			log.debug("Enter: RPCProvider.processMessage()");
		}

		//////////////To test whether the addressing headers are accessible at this point
		System.out.println("This is from the RMProvider"); 
		System.out.println(
			"-----------------------------------------------------------");
			System.out.println(obj);
		System.out.println(
			"-----------------------------------------------------------");
		System.out.println(
			msgContext.getRequestMessage().getSOAPPartAsString());
		System.out.println(
			"-----------------------------------------------------------");
		
		//System.out.println(msgContext.getResponseMessage());

		ServerMessageController serverMessageController =
			ServerMessageController.getInstance();
		ClientMessageController clientMessageController =
			ClientMessageController.getInstance();

		RMMessage message = new RMMessage();

		/////////////
		//code will be in the real implemention

		RMHeaders rmHeaders =(RMHeaders) msgContext.getProperty(org.apache.sandesha.ws.rm.Constants.ENV_RM_REQUEST_HEADERS);
		System.out.println(rmHeaders);
		AddressingHeaders addressingHeaders =(AddressingHeaders) msgContext.getProperty(org.apache.axis.message.addressing.Constants.ENV_ADDRESSING_REQUEST_HEADERS);
		System.out.println(addressingHeaders);
		

		if(true){
			
		}//Address fromAddress = addressingHeaders.getFrom().getAddress();
		String anonymous =new String(org.apache.sandesha.Constants.ANONYMOUS_URI);
		boolean asynchronous = true;

		message.setAddressingHeaders(addressingHeaders);
		message.setRMHeaders(rmHeaders);
		message.setRequestMessage(msgContext.getRequestMessage());
		msgContext.setEncodingStyle(msgContext.getEncodingStyle());
		//rmHeaders=null;

		//			check for wsrm headers pass to super
		if (rmHeaders == null) {
			System.out.println("rmHeaders==null");
			System.out.println("Calling to super");
			super.processMessage(msgContext, reqEnv, resEnv, obj);
		} else {
		
			//has some wsrm header 
			System.out.println("rmHeaders!=null");
			message.setRMHeaders(rmHeaders);
			message.setAddressingHeaders(addressingHeaders);
			

			if (rmHeaders.getCreateSequence() != null) {
				//Address fromAddress= addressingHeaders.getFrom().getAddress();
				//String anonymous=new String("http://schemas.xmlsoap.org/ws/2003/03/addressing/role/anonymous");
				if (asynchronous) {

				}
			}
			if (rmHeaders.getCreateSequenceResponse() != null) {
				if (asynchronous) {

				}
			}
			if (rmHeaders.getAckRequest() != null) {
				if (asynchronous) {
					System.out.println("rmHeaders.getAckRequest() != null");
					SequenceAcknowledgement seqAck=serverMessageController.getAcknowledgement(rmHeaders.getAckRequest().getIdentifier());
					SOAPEnvelope ackResEnv=new SOAPEnvelope();
					RMHeaders ackResRMHeaders=new RMHeaders();
					ackResRMHeaders.setSequenceAcknowledgement(seqAck);
					ackResRMHeaders.toSoapEnvelop(ackResEnv);
					Message ackResMsg=new Message(ackResEnv);
					Call call=new Call(addressingHeaders.getReplyTo().getAddress().toString());
					call.setRequestMessage(ackResMsg);
					try{
						call.invoke();
						}catch(Exception e){
							
						}
					
					
				}
			}
			if (rmHeaders.getSequenceAcknowledgement() != null) {
				System.out.println("rmHeaders.getSequenceAcknowledgement() != null");
				Identifier seqAckID=rmHeaders.getSequenceAcknowledgement().getIdentifier();
				RMSequence clientSeq=clientMessageController.retrieveIfSequenceExists(seqAckID);
				RMSequence serverSeq=serverMessageController.retrieveIfSequenceExists(seqAckID);
				if(clientSeq!=null){
					System.out.println("clientSeq!=null");
					clientSeq.setSequenceAcknowledgement(rmHeaders.getSequenceAcknowledgement());
				}
				if(serverSeq!=null){
					System.out.println("serverSeq!=null");
					serverSeq.setSequenceAcknowledgement(rmHeaders.getSequenceAcknowledgement());
				}
				
			}
			if (rmHeaders.getSequence() != null) {
				System.out.println("rmHeaders.getSequence()!=null");
				 message.setIdentifier(rmHeaders.getSequence().getIdentifier());
				if (msgContext.getOperation() != null) {
					System.out.println("msgContext.getOperation() != null");
					message.setOperation(msgContext.getOperation());
					message.setServiceDesc(msgContext.getService().getServiceDescription());
					message.setServiceObject(obj);
					serverMessageController.insertMessage(message);
					
				}else{
					System.out.println(msgContext.getOperation() == null);
					RMSequence responsedSeq=clientMessageController.retrieveIfSequenceExists(rmHeaders.getSequence().getIdentifier());
					if(responsedSeq!=null){
						RMMessage resMsg=responsedSeq.retrieveMessage(new Long(message.getMessageNumber()));
						resMsg.setResponseMessage(message.getRequestMessage());
					}
					responsedSeq.setResponceMessage(message);
					
					
				}
			}

		}
		System.out.println(msgContext.getResponseMessage());
		System.out.println("RMProvider finished");

	}

	/**
	 * This method encapsulates the method invocation.             
	 * @param msgContext MessageContext
	 * @param method the target method.
	 * @param obj the target object
	 * @param argValues the method arguments
	 */
	protected Object invokeMethod(
		MessageContext msgContext,
		Method method,
		Object obj,
		Object[] argValues)
		throws Exception {
		return (method.invoke(obj, argValues));
	}

	/**
	 * Throw an AxisFault if the requested method is not allowed.
	 * @param msgContext MessageContext
	 * @param allowedMethods list of allowed methods
	 * @param methodName name of target method
	 */
	protected void checkMethodName(
		MessageContext msgContext,
		String allowedMethods,
		String methodName)
		throws Exception {
		// Our version doesn't need to do anything, though inherited
		// ones might.
	}

}
