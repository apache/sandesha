/*
 * Created on Oct 9, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.apache.sandesha2;

import org.apache.axis2.AxisFault;
import org.apache.axis2.transport.http.SimpleHTTPServer;

public class SimpleSandesha2Server {

	public static void main(String[] args) throws AxisFault {
		System.out.println("Starting sandesha2 server...");
		SimpleHTTPServer server = new SimpleHTTPServer ("E:\\Program Files\\Apache Software Foundation\\Tomcat 5.0\\webapps\\axis2\\WEB-INF",8080);
		server.start();
		
	}
}
