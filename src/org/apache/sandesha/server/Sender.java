/*
 * Copyright  1999-2004 The Apache Software Foundation.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.apache.sandesha.server;

import javax.xml.rpc.ServiceException;

import org.apache.axis.AxisFault;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.sandesha.Constants;
import org.apache.sandesha.IStorageManager;
import org.apache.sandesha.RMMessageContext;

/**
 * @author JEkanayake
 *
 */
public class Sender implements Runnable {
	private IStorageManager storageManager;

	public Sender() {
		storageManager = new ServerStorageManager();
	}

	public void run() {

		while (true) {
			long startTime = System.currentTimeMillis();
			boolean hasMessages = true;
			do {
				RMMessageContext rmMessageContext = storageManager.getNextMessageToSend();
				if (rmMessageContext == null)
					hasMessages = false;
				else {
					//Send the message.

					switch (rmMessageContext.getMessageType()) {
						case Constants.MSG_TYPE_CREATE_SEQUENCE_REQUEST :
							{
								//Send the message.
								//may get the reply back to here.

							}
						case Constants.MSG_TYPE_CREATE_SEQUENCE_RESPONSE :
							{
								//Send creat seq message.
								//No response and we can just close the connection
								try {
									Service service = new Service();
									Call call = (Call) service.createCall();
									call.setTargetEndpointAddress(rmMessageContext.getOutGoingAddress());
									call.setRequestMessage(
										rmMessageContext.getMsgContext().getResponseMessage());
									call.invoke();
								} catch (ServiceException e1) {
									System.out.println("(!)(!)(!)Cannot send the create sequence response.");
									e1.printStackTrace();
								} catch (AxisFault e) {
									e.printStackTrace();
								}
							}
						case Constants.MSG_TYPE_TERMINATE_SEQUENCE :
							{

							}
						case Constants.MSG_TYPE_ACKNOWLEDGEMENT :
							{
								try {
									Service service = new Service();
									Call call = (Call) service.createCall();
									call.setTargetEndpointAddress(rmMessageContext.getOutGoingAddress());
									call.setRequestMessage(
										rmMessageContext.getMsgContext().getResponseMessage());
									call.invoke();
								} catch (ServiceException e1) {
									System.out.println("(!)(!)(!)Cannot send the Ack message.");
									e1.printStackTrace();
								} catch (AxisFault e) {
									e.printStackTrace();
								}
							}
						case Constants.MSG_TYPE_SERVICE_RESPONSE :
							{
							}
					}

				}

			} while (hasMessages);

			long timeGap = System.currentTimeMillis() - startTime;
			if ((timeGap - Constants.SENDER_SLEEP_TIME) <= 0) {
				try {
					System.out.println("Sender thread is sleeping .............................");
					Thread.sleep(Constants.SENDER_SLEEP_TIME - timeGap);
				} catch (Exception ex) {
					ex.printStackTrace();
				}

			}
		}
	}

}
