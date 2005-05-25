
package org.apache.sandesha.samples.interop.testclient;

import java.io.PrintWriter;


/**
 * @author root
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class ResponseWriter {
	
	private PrintWriter writer;
	
	public ResponseWriter(PrintWriter writer){
			this.writer = writer;
	}
	
	public synchronized boolean write(String s){ 
		try{
			writer.println(s);
			flush();
			return true;
		}catch(Exception e){
			System.out.println("Exception: In method 'write' of 'ResponseWriter'");
			return false;
		}
	}
	
	public synchronized void flush(){
		writer.flush();
	}
}
