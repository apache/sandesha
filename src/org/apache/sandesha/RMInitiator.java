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
import org.apache.axis.components.logger.LogFactory;
import org.apache.axis.configuration.SimpleProvider;
import org.apache.axis.description.JavaServiceDesc;
import org.apache.axis.handlers.soap.SOAPService;
import org.apache.axis.transport.http.SimpleAxisServer;
import org.apache.commons.logging.Log;
import org.apache.sandesha.client.ClientStorageManager;
import org.apache.sandesha.server.RMInvoker;
import org.apache.sandesha.server.Sender;
import org.apache.sandesha.server.ServerStorageManager;
import org.apache.sandesha.storage.dao.SandeshaQueueDAO;
import org.apache.sandesha.util.PropertyLoader;
import org.apache.sandesha.ws.rm.providers.RMProvider;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
    private static Thread thSender;
    private static Thread thInvoker;
    private static Sender sender;
    private static final Log log = LogFactory.getLog(RMInitiator.class.getName());

    public static IStorageManager init(boolean client) {
        if (client) {
            IStorageManager storageManager = new ClientStorageManager();
            if (!senderStarted) {
                System.out.println(Constants.InfomationMessage.SENDER_STARTED);
                sender = new Sender(storageManager);
                SimpleChain reqChain = getRequestChain();
                SimpleChain resChain = getResponseChain();
                if (reqChain != null)
                    sender.setRequestChain(reqChain);
                if (resChain != null)
                    sender.setResponseChain(resChain);

                thSender = new Thread(sender);
                thSender.setDaemon(false);
                senderStarted = true;
                thSender.start();
            }
            return storageManager;
        } else {
            if (!senderStarted) {
                System.out.println(Constants.InfomationMessage.SENDER_STARTED);
                Sender sender = new Sender();
                Thread thSender = new Thread(sender);
                thSender.setDaemon(false);
                senderStarted = true;
                thSender.start();
            }
            if (!rmInvokerStarted) {
                System.out.println(Constants.InfomationMessage.RMINVOKER_STARTED);
                RMInvoker rmInvoker = new RMInvoker();
                thInvoker = new Thread(rmInvoker);
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

    public static RMStatus stopClient() {
        //This should check whether we have received all the acks or reponses if any
        IStorageManager storageManager = new ClientStorageManager();
        storageManager.isAllSequenceComplete();

        while (!storageManager.isAllSequenceComplete()) {
            try {
                System.out.println(Constants.InfomationMessage.WAITING_TO_STOP_CLIENT);
                Thread.sleep(Constants.CLIENT_WAIT_PERIOD_FOR_COMPLETE);
            } catch (InterruptedException e) {
                log.error(e);
            }
        }

        if (listenerStarted)
            sas.stop();
        sender.setRunning(false);
        return new RMStatus();


    }

    private static void startListener() {

        try {
            System.out.println(Constants.InfomationMessage.CLIENT_LISTENER_STARTED);
            sas = new SimpleAxisServer();
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            DocumentBuilder db = dbf.newDocumentBuilder();

            SimpleProvider sp = new SimpleProvider();
            sas.setMyConfig(sp);

            SimpleChain reqHandlers = getListenerRequestChain();
            SimpleChain resHandlers = getListenerResponseChain();

            RMProvider rmp = new RMProvider();
            rmp.setClient(true);
            SOAPService rmService = new SOAPService(reqHandlers, rmp, resHandlers);

            JavaServiceDesc desc = new JavaServiceDesc();
            rmService.setOption(Constants.ClientProperties.CLASS_NAME, Constants.ClientProperties.RMSERVICE_CLASS);
            rmService.setOption(Constants.ClientProperties.ALLOWED_METHODS,Constants.ASTERISK);

            desc.setName(Constants.ClientProperties.RMSERVICE);
            rmService.setServiceDescription(desc);
            sp.deployService(Constants.ClientProperties.RMSERVICE, rmService);
            sas.setServerSocket(new ServerSocket(PropertyLoader.getClientSideListenerPort()));

            Thread serverThread = new Thread(sas);
            serverThread.start();

        } catch (Exception e) {
            log.error(e);
        }

    }


    public static SimpleChain getHandlerChain(List arr) {
        SimpleChain reqHandlers = new SimpleChain();
        Iterator it = arr.iterator();
        boolean hasReqHandlers = false;
        try {
            while (it.hasNext()) {
                hasReqHandlers = true;
                String strClass = (String) it.next();
                Class c = Class.forName(strClass);
                Handler h = (Handler) c.newInstance();
                reqHandlers.addHandler(h);
            }
        } catch (Exception e) {
            log.error(e);
            return null;
        }
        if (hasReqHandlers)
            return reqHandlers;
        else
            return null;
    }


    private static SimpleChain getRequestChain() {
        ArrayList arr = PropertyLoader.getRequestHandlerNames();
        return getHandlerChain(arr);
    }


    private static SimpleChain getResponseChain() {

        ArrayList arr = PropertyLoader.getResponseHandlerNames();
        return getHandlerChain(arr);
    }

    private static SimpleChain getListenerRequestChain() {

        ArrayList arr = PropertyLoader.getListenerRequestHandlerNames();
        return getHandlerChain(arr);
    }

    private static SimpleChain getListenerResponseChain() {

        ArrayList arr = PropertyLoader.getListenerResponseHandlerNames();
        return getHandlerChain(arr);
    }


}