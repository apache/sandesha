/*
 * Created on Aug 26, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.apache.sandesha2;

import java.io.IOException;

import org.apache.axis2.transport.http.SimpleHTTPServer;

/**
 * @author 
 * 
 */
public class SimpleServerImpl {

    public static void main(String[] args) throws IOException {
        SimpleHTTPServer sas= new SimpleHTTPServer("E:\\Program Files\\Apache Software Foundation\\Tomcat 5.0\\webapps\\axis2\\WEB-INF",8080);
        sas.start();
        System.out.println ("Sandesha2 Simple Axis Server Started....");
    }
}
