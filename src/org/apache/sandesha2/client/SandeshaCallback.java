/*
 * Created on Oct 11, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.apache.sandesha2.client;

import org.apache.axis2.clientapi.AsyncResult;
import org.apache.axis2.clientapi.Callback;


public class SandeshaCallback extends Callback {

	
	public void onComplete(AsyncResult result) {
		System.out.println("OnComplete of SandeshaCallback was called");
	}
	public void reportError(Exception e) {
		System.out.println("reportError of SandeshaCallback was called");
	}
	
}
