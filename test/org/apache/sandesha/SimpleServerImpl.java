package org.apache.sandesha.test;

import java.io.File;
import java.net.ServerSocket;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.namespace.QName;

import org.apache.axis.deployment.wsdd.WSDDDeployment;
import org.apache.axis.deployment.wsdd.WSDDDocument;
import org.apache.axis.transport.http.SimpleAxisServer;
import org.apache.axis.handlers.soap.SOAPService;
import org.apache.axis.EngineConfiguration;
import org.apache.axis.AxisEngine;
import org.apache.axis.description.JavaServiceDesc;
import org.apache.axis.server.AxisServer;
import org.apache.sandesha.ws.rm.providers.RMProvider;
import org.apache.sandesha.util.PropertyLoader;
import org.w3c.dom.Document;

/**
 * Created by IntelliJ IDEA. User: Jaliya Date: Sep 13, 2004 Time: 2:31:47 PM To
 * change this template use Options | File Templates.
 */
public class SimpleServerImpl {

    public static void main(String[] args) {

        try {
            SimpleAxisServer sas = new SimpleAxisServer();
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            DocumentBuilder db = dbf.newDocumentBuilder();

            //Need to change the path

            Document doc = db.parse(new File("config/server-config.wsdd"));
            //Document doc = db.parse(new File("D:\\Projects\\Sandesha\\Version1\\classes\\server-config.wsdd"));
            //"C:/SandeshaPrj/SandeshaIMPL/PrjWorkspace/bin/server-config.wsdd"));
            WSDDDocument wsdddoc = new WSDDDocument(doc);
            WSDDDeployment wsdddep = wsdddoc.getDeployment();
            sas.setMyConfig(wsdddep);
            sas.getMyConfig().configureEngine(new AxisServer());

            sas.setServerSocket(new ServerSocket(PropertyLoader.getSimpleAxisServerPort()));
            Thread serverThread = new Thread(sas);
            serverThread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

