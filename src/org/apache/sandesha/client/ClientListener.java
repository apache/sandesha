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

package org.apache.sandesha.client;


import org.apache.axis.SimpleChain;
import org.apache.axis.configuration.SimpleProvider;
import org.apache.axis.description.JavaServiceDesc;
import org.apache.axis.handlers.soap.SOAPService;
import org.apache.axis.transport.http.SimpleAxisServer;
import org.apache.sandesha.Constants;
import org.apache.sandesha.util.PropertyLoader;
import org.apache.sandesha.ws.rm.providers.RMProvider;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;


/**
 * This is the client side listener for Apache Sandesha.
 * <p/>
 * This will start with the RMService deployed to accept asynchronous responses or
 * <p/>
 * RM related messages such as Acknowledgements etc..
 *
 * @author Jaliya Ekanayake
 * @author Patrick Collins
 */

public class ClientListener {


    public static final int NULL_PORT = -1;
    private SimpleAxisServer sas;
    private int listenerPort = NULL_PORT;
    private boolean started;

    public ClientListener(int aPort) {
        listenerPort = aPort;
    }


    public synchronized void start() throws IOException {

        if (!isStarted()) {
            initSimpleAxisServer();
            configureClientService();
            configureServerSocket();
            startSimpleAxisServer();
        }
    }


    public void stop() {
        getSas().stop();
    }


    protected void initSimpleAxisServer() {
        setSas(new SimpleAxisServer());
        getSas().setMyConfig(new SimpleProvider());
    }


    protected void configureClientService() {
        SimpleChain reqHandlers = getListenerRequestChain();
        SimpleChain resHandlers = getListenerResponseChain();
        RMProvider rmp = new RMProvider();
        rmp.setClient(true);
        SOAPService rmService = new SOAPService(reqHandlers, rmp, resHandlers);
        JavaServiceDesc desc = new JavaServiceDesc();
        rmService.setOption(Constants.ClientProperties.CLASS_NAME, Constants.ClientProperties.RMSERVICE_CLASS);
        rmService.setOption(Constants.ClientProperties.ALLOWED_METHODS, Constants.ASTERISK);
        desc.setName(Constants.ClientProperties.RMSERVICE);
        rmService.setServiceDescription(desc);
        ((SimpleProvider) getSas().getMyConfig()).deployService(Constants.ClientProperties.RMSERVICE, rmService);
    }

    protected void configureServerSocket() throws IOException {
        ServerSocket socket = new ServerSocket(getListenerPort());
        getSas().setServerSocket(socket);
    }

    protected void startSimpleAxisServer() {
        Thread serverThread = new Thread(getSas());
        serverThread.start();
    }

    protected SimpleChain getListenerRequestChain() {
        ArrayList arr = PropertyLoader.getListenerRequestHandlerNames();
        return ClientHandlerUtil.getHandlerChain(arr);
    }

    protected SimpleChain getListenerResponseChain() {
        ArrayList arr = PropertyLoader.getListenerResponseHandlerNames();
        return ClientHandlerUtil.getHandlerChain(arr);
    }

    protected SimpleAxisServer getSas() {
        return sas;
    }

    protected void setSas(SimpleAxisServer aSas) {
        sas = aSas;
    }

    protected int getListenerPort() {
        if (listenerPort == NULL_PORT) {
            listenerPort = PropertyLoader.getClientSideListenerPort();
        }

        return listenerPort;
    }


    protected boolean isStarted() {
        return started;
    }
}

