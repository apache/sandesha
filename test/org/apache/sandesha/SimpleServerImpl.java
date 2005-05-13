package org.apache.sandesha;

import org.apache.axis.transport.http.SimpleAxisServer;
import org.apache.sandesha.util.PropertyLoader;

import java.net.ServerSocket;

public class SimpleServerImpl {

    public static void main(String[] args) {

        try {
            SimpleAxisServer sas = new SimpleAxisServer();
            sas.setServerSocket(new ServerSocket(PropertyLoader.getSimpleAxisServerPort()));
            Thread serverThread = new Thread(sas);
            serverThread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

