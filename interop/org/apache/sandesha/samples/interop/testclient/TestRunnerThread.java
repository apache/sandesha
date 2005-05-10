/*
 * Created on Apr 15, 2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.apache.sandesha.samples.interop.testclient;

import javax.swing.JOptionPane;

/**
 * @author root
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class TestRunnerThread extends Thread {
	private int method = 0;
	private InteropBean bean;
	
	public void setMethod(int method){
		this.method = method;
	}
	
	public void setBean(InteropBean bean){
		this.bean = bean;
	}
	
	public void run(){
		
		InteropStub stub = new InteropStub ();
		String operation = bean.getOperation();
		
		
		if(operation.equalsIgnoreCase("ping")){
			//JOptionPane.showMessageDialog(null,"RUNNING PING");
			String from = bean.getFrom();
			if(from==null || from.equalsIgnoreCase("anonymous URI")){
				//JOptionPane.showMessageDialog(null,"RUNNING SYNC");
				stub.runPingSync(bean);
			}else {
				//JOptionPane.showMessageDialog(null,"RUNNING ASYNC");
				stub.runPingAsync(bean);
			}
			
		}else if(operation.equalsIgnoreCase("echoString") ){
			String from = bean.getFrom();
			if(from==null || from.equalsIgnoreCase("anonymous URI")){
				stub.runEchoStringSync(bean);
			}else {
				stub.runEchoStringAsync(bean);
			}
			
		}
		
	}
	
	
}
