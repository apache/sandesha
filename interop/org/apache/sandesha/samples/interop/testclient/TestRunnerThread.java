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
		
		InteropStub stub = InteropStub.getInstance();
		String operation = bean.getOperation();
		
		
		if(operation.equalsIgnoreCase("ping")){

		    stub.runPing(bean);
			
		}else if(operation.equalsIgnoreCase("echoString") ){
		    
		    stub.runEcho(bean);
		}
		
	}
	
	
}
