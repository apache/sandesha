/*
 * Created on May 3, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.apache.sandesha.samples.interop;

import java.util.HashMap;
import java.util.Map;

/**
 * @author SNimalan
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class EchoStringService {
	private static Map sequences=new HashMap(); 
	public String echoString(String text,String sequence){
		
		if(sequences.get(sequence)!=null){
			text=(String) sequences.get(sequence)+text;
			sequences.put(sequence,new String(text));
		}else{
			sequences.put(sequence,(new String(text)));
			
		}
		return text;
	}
}
