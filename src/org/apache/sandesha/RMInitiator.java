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
package org.apache.sandesha;

import org.apache.axis.Handler;
import org.apache.axis.SimpleChain;
import org.apache.axis.configuration.SimpleProvider;
import org.apache.axis.description.JavaServiceDesc;
import org.apache.axis.handlers.soap.SOAPService;
import org.apache.axis.message.addressing.handler.AddressingHandler;
import org.apache.axis.transport.http.SimpleAxisServer;
import org.apache.sandesha.client.ClientStorageManager;
import org.apache.sandesha.server.RMInvoker;
import org.apache.sandesha.server.Sender;
import org.apache.sandesha.server.ServerStorageManager;
import org.apache.sandesha.ws.rm.handlers.RMServerRequestHandler;
import org.apache.sandesha.ws.rm.providers.RMProvider;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * @author Jaliya
 * @auther Chamikara This class will act as the initiator for the Sender and the
 * SimpleAxisServer,' In the client side, it will start the Sender and
 * retruns ClientStorageManager. In the server side, it will start both
 * the Sender and the RMInvoker and returns ServerStorageManager.
 */

public class RMInitiator {
    private static boolean rmInvokerStarted = false;
    private static boolean senderStarted = false;
    private static boolean listenerStarted = false;
    private static SimpleAxisServer sas = null;

    public static IStorageManager init(boolean client) {
        if (client) {
            IStorageManager storageManager = new ClientStorageManager();
            if (!senderStarted) {
                System.out.println("INFO: Sender Thread started .....\n");
                Sender sender = new Sender(storageManager);
                Thread thSender = new Thread(sender);
                thSender.setDaemon(false);
                senderStarted = true;
                thSender.start();
            }
            return storageManager;
        } else {
            if (!senderStarted) {
                System.out.println("INFO: Sender Thread started .....\n");
                Sender sender = new Sender();
                Thread thSender = new Thread(sender);
                thSender.setDaemon(false);
                senderStarted = true;
                thSender.start();
            }
            if (!rmInvokerStarted) {
                System.out.println("INFO: RMInvoker thread started ....\n");
                RMInvoker rmInvoker = new RMInvoker();
                Thread thInvoker = new Thread(rmInvoker);
                thInvoker.setDaemon(true);
                rmInvokerStarted = true;
                thInvoker.start();
            }
            return new ServerStorageManager();
        }
    }

    public static void initClient(boolean sync) {
        init(true);
        if (!sync && !listenerStarted) {
            listenerStarted = true;
            startListener();
        }
    }

    private static void startListener() {
        sas = new SimpleAxisServer();
        try {
            SimpleProvider sp = new SimpleProvider();
            sas.setMyConfig(sp);

            SimpleChain shc = new SimpleChain();
            //We need these two handlers in the request path to the client.
            //Actually the response messages coming asynchronously should
            //come through the SimpleAxisServer instance.
            //We need to load the response handlers specified by the users
            // in addtion to the the above two.
            Handler addrHanlder = new AddressingHandler();
            Handler rmHandler = new RMServerRequestHandler();

            shc.addHandler(addrHanlder);
            shc.addHandler(rmHandler);

            //Need to use the RMProvider at the client side to handle the
            //Asynchronous responses.
            RMProvider rmProvider = new RMProvider();
            //This is the switch used to inform the RMProvider about the
            // side that it operates.
            rmProvider.setClient(true);

            SOAPService rmService = new SOAPService(shc, rmProvider, null);

            JavaServiceDesc desc = new JavaServiceDesc();
            rmService.setOption("className", "org.apache.sandesha.client.RMService");
            rmService.setOption("allowedMethods", "*");

            //Add Handlers ; Addressing and ws-rm before the service.
            desc.setName("RMService");
            rmService.setServiceDescription(desc);

            //deploy the service to server
            sp.deployService("RMService", rmService);
            //finally start the server
            //Start the simple axis server in port 8090
            sas.setServerSocket(new ServerSocket(Constants.SOURCE_LISTEN_PORT));

            Thread serverThread = new Thread(sas);
            //serverThread.setDaemon(true);
            serverThread.start();
        } catch (IOException ex) {
            ex.printStackTrace();
        }


    }
}