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
package org.apache.sandesha.client;

import java.io.IOException;
import java.net.ServerSocket;

import org.apache.axis.AxisFault;
import org.apache.axis.Handler;
import org.apache.axis.MessageContext;
import org.apache.axis.SimpleChain;
import org.apache.axis.configuration.SimpleProvider;
import org.apache.axis.description.JavaServiceDesc;
import org.apache.axis.handlers.BasicHandler;
import org.apache.axis.handlers.soap.SOAPService;
import org.apache.axis.message.addressing.handler.AddressingHandler;
import org.apache.axis.transport.http.SimpleAxisServer;
import org.apache.sandesha.server.Sender;
import org.apache.sandesha.ws.rm.handlers.RMServerRequestHandler;

public class RMSender extends BasicHandler {

	/**
	 * Initialize the StorageManager Add the messsag to the queue and just
	 * return Create SimpleAxisServer
	 */

	private static boolean senderStarted = false;
	private static boolean serverStarted = false;

	private SimpleAxisServer sas = null;
	private Sender sender = null;

	public void invoke(MessageContext msgContext) throws AxisFault {
		//Check whether we have messages or not in the queue.
		//If yes, just add
		//If no, need to add a priority message.
		//return.

		//Start the sender
		//Start the SimpleAxisServer
		//Initiate the StorageManager
		//Insert the messae
		//Return null ; Later return for callback.

		if (!senderStarted) {
			sender = new Sender();
			Thread senderThread= new Thread(sender);
			senderThread.setDaemon(true);
			senderThread.start();
		}

		if (!serverStarted) {
			sas = new SimpleAxisServer();
			serverStarted = true;

			try {
				SimpleProvider sp = new SimpleProvider();
				sas.setMyConfig(sp);
				//SOAPService  myService = new SOAPService(new RPCProvider());

				Handler addrHanlder = new AddressingHandler();
				Handler rmHandler = new RMServerRequestHandler();

				SimpleChain shc = new SimpleChain();
				shc.addHandler(addrHanlder);
				shc.addHandler(rmHandler);

				SOAPService myService =
					new SOAPService(rmHandler, new org.apache.sandesha.ws.rm.providers.RMProvider(), null);
				//			customize the webservice
				JavaServiceDesc desc = new JavaServiceDesc();
				myService.setOption("className", "samples.userguide.example3.MyService");
				myService.setOption("allowedMethods", "*");

				//Add Handlers ; Addressing and ws-rm before the service.

				desc.setName("MyService");
				myService.setServiceDescription(desc);

				//			 deploy the service to server
				sp.deployService("MyService", myService);
				//			finally start the server
				sas.setServerSocket(new ServerSocket(8080));

				Thread serverThread = new Thread(sas);
				serverThread.setDaemon(true);
				serverThread.start();
			} catch (IOException ex) {
				ex.printStackTrace();
			}

		}
		
		msgContext.setResponseMessage(null);
		
		
	}

}

