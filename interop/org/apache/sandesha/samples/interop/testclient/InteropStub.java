/*
 * Created on Apr 15, 2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.apache.sandesha.samples.interop.testclient;



import javax.xml.namespace.QName;
import javax.xml.rpc.ParameterMode;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.encoding.XMLType;
import org.apache.axis.utils.XMLUtils;
import org.apache.sandesha.Constants;
import org.apache.sandesha.RMInitiator;
import org.apache.sandesha.RMTransport;



/**
 * @author root
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class InteropStub {
	
	public void runPingSync(InteropBean bean){
		String target = bean.getTarget();
		String from = bean.getFrom();
		String replyTo = bean.getReplyto();
		String acks = bean.getAcks();
		String terminate = bean.getTerminate();
		String operation = bean.getOperation();
		int messages = bean.getNoOfMsgs();
			
		//String targetURL = "http://127.0.0.1:"+"8070"+"/axis/services/RMInteropService?wsdl";

		try {

			System.out.println("********Running ping sync");
			RMInitiator.initClient(true);

			Service service = new Service();
			Call call = (Call) service.createCall();

			call.setProperty(Constants.ClientProperties.SYNC, new Boolean(true));
			call.setProperty(Constants.ClientProperties.ACTION, "sandesha:ping");

			//These two are additional
			//call.setProperty("from","http://schemas.xmlsoap.org/ws/2003/03/addressing/role/anonymous");
			//call.setProperty("replyTo","http://10.10.0.4:8080/axis/services/MyService");
			//http://schemas.xmlsoap.org/ws/2003/03/addressing/role/anonymous

			call.setProperty(Constants.ClientProperties.FROM,org.apache.axis.message.addressing.Constants.NS_URI_ANONYMOUS);
			
			call.setTargetEndpointAddress(target);
			call.setOperationName(new QName("RMInteropService", operation));
			call.setTransport(new RMTransport(target, ""));

			call.addParameter("arg1", XMLType.XSD_STRING, ParameterMode.IN);

			for(int i=1;i<=messages;i++){
				call.setProperty(Constants.ClientProperties.MSG_NUMBER, new Long((i)));
				String msg = "Ping Message Number " + i;
				call.invoke(new Object[]{msg});
				
				if(i==messages){
					call.setProperty(Constants.ClientProperties.LAST_MESSAGE, new Boolean(true));
				}
			}
			//First Message
			/*call.setProperty(Constants.ClientProperties.MSG_NUMBER, new Long(1));
			call.invoke(new Object[]{"Ping Message Number One"});

			//Second Message
			call.setProperty(Constants.ClientProperties.MSG_NUMBER, new Long(2));
			call.invoke(new Object[]{"Ping Message Number Two"});

			//Third Message
			call.setProperty(Constants.ClientProperties.MSG_NUMBER, new Long(3));
			call.setProperty(Constants.ClientProperties.LAST_MESSAGE, new Boolean(true)); //For last message.
			call.invoke(new Object[]{"Ping Message Number Three"});*/

			RMInitiator.stopClient();

			} catch (Exception e) {
				//System.err.println(e.toString());
				e.printStackTrace();
			}	
			
	}
	
	public void runPingAsync(InteropBean bean){
	      try {

			String target = bean.getTarget();
			String from = bean.getFrom();
			String replyTo = bean.getReplyto();
			String acks = bean.getAcks();
			String terminate = bean.getTerminate();
			String operation = bean.getOperation();
			int messages = bean.getNoOfMsgs();
			
			
	          RMInitiator.initClient(false);

	          Service service = new Service();
	          Call call = (Call) service.createCall();

	          call.setProperty(Constants.ClientProperties.SYNC, new Boolean(false));
	          call.setProperty(Constants.ClientProperties.ACTION, "sandesha:ping");

	          //These two are additional
	          call.setProperty("from","http://127.0.0.1:" + "9070" + "/axis/services/RMService");
	          //call.setProperty("replyTo","http://127.0.0.1:"+defaultClientPort+"/axis/services/RMService");
	          //http://schemas.xmlsoap.org/ws/2003/03/addressing/role/anonymous

	          call.setTargetEndpointAddress(target);
	          call.setOperationName(new QName("RMInteropService", "ping"));
	          call.setTransport(new RMTransport(target, ""));

	          call.addParameter("arg1", XMLType.XSD_STRING, ParameterMode.IN);
	          	
				for(int i=1;i<=messages;i++){
					call.setProperty(Constants.ClientProperties.MSG_NUMBER, new Long((i)));
					String msg = "Ping Message Number " + i;
					
					if(i==messages){
						call.setProperty(Constants.ClientProperties.LAST_MESSAGE, new Boolean(true));
					}
					
					call.invoke(new Object[]{msg});
				}

	          RMInitiator.stopClient();

	      } catch (Exception e) {
	          //System.err.println(e.toString());
	          e.printStackTrace();
	      }	    
	}
	
	public void runEchoStringSync(InteropBean bean){
	       try {
	           
			String target = bean.getTarget();
			String from = bean.getFrom();
			String replyTo = bean.getReplyto();
			String acks = bean.getAcks();
			String terminate = bean.getTerminate();
			String operation = bean.getOperation();
			int messages = bean.getNoOfMsgs();
		        
		        //A separate listner will be started if the value of the input parameter for the mehthod
	            // initClient is "false". If the service is of type request/response the parameter value shoule be "false"
	            RMInitiator.initClient(false);

	            //UUIDGen uuidGen = UUIDGenFactory.getUUIDGen(); //Can use this for continuous testing.
	            //String str = uuidGen.nextUUID();


	            Service service = new Service();
	            Call call = (Call) service.createCall();

	            //Action is required.
	            call.setProperty(Constants.ClientProperties.SYNC, new Boolean(false));
	            call.setProperty(Constants.ClientProperties.ACTION, "sandesha:echo");

	            //These two are additional, We need them since we need to monitor the messages using TCPMonitor.
	            call.setProperty(Constants.ClientProperties.FROM,org.apache.axis.message.addressing.Constants.NS_URI_ANONYMOUS);
	            call.setProperty(Constants.ClientProperties.REPLY_TO,"http://127.0.0.1:" + "9070" + "/axis/services/RMService");
				
				
	            call.setTargetEndpointAddress(target);
	            call.setOperationName(new QName("RMInteropService", "echoString"));
	            call.setTransport(new RMTransport(target, ""));

	            call.addParameter("arg1", XMLType.XSD_STRING, ParameterMode.IN);
	            call.addParameter("arg2", XMLType.XSD_STRING, ParameterMode.IN);
	            call.setReturnType(org.apache.axis.encoding.XMLType.XSD_STRING);

				for(int i=1;i<=messages;i++){
					call.setProperty(Constants.ClientProperties.MSG_NUMBER, new Long((i)));
					String msg = "ECHO " + i;
					

					if(i==messages){
						call.setProperty(Constants.ClientProperties.LAST_MESSAGE, new Boolean(true));
					}
					
					String ret = (String) call.invoke(new Object[]{msg,"abcdef"});
					System.out.println("Got response from server " + ret);
				}

	            RMInitiator.stopClient();
	        } catch (Exception e) {
	            e.printStackTrace();
	        }		
	}	
	
	public void runEchoStringAsync(InteropBean bean){
        try {
            
            System.out.println("********* Running ECHO STRING Async");
            
            System.out.println("e11111");
			String target = bean.getTarget();
			String from = bean.getFrom();
			String replyTo = bean.getReplyto();
			String acks = bean.getAcks();
			String terminate = bean.getTerminate();
			String operation = bean.getOperation();
			int messages = bean.getNoOfMsgs();
	        
            //A separate listner will be started if the value of the input parameter for the mehthod
            // initClient is "false". If the service is of type request/response the parameter value shoule be "false"
            RMInitiator.initClient(false);

            //UUIDGen uuidGen = UUIDGenFactory.getUUIDGen(); //Can use this for continuous testing.
            //String str = uuidGen.nextUUID();


            Service service = new Service();
            Call call = (Call) service.createCall();
            System.out.println("e22222");
            //To obtain the
            call.setProperty(Constants.ClientProperties.SYNC, new Boolean(false));
            call.setProperty(Constants.ClientProperties.ACTION, "sandesha:echo");

            //These two are additional
            //call.setProperty("from","http://schemas.xmlsoap.org/ws/2003/03/addressing/role/anonymous");
            call.setProperty(Constants.ClientProperties.FROM,"http://127.0.0.1:" + "9070" + "/axis/services/RMService");
            call.setProperty(Constants.ClientProperties.REPLY_TO,"http://127.0.0.1:" + "9070" + "/axis/services/RMService");

            call.setTargetEndpointAddress(target);
            call.setOperationName(new QName("RMInteropService", "echoString"));
            call.setTransport(new RMTransport(target, ""));

            call.addParameter("arg1", XMLType.XSD_STRING, ParameterMode.IN);
            call.addParameter("arg2", XMLType.XSD_STRING, ParameterMode.IN);
            call.setReturnType(org.apache.axis.encoding.XMLType.XSD_STRING);
            System.out.println("e3333");
			for(int i=1;i<=messages;i++){
				call.setProperty(Constants.ClientProperties.MSG_NUMBER, new Long((i)));
				String msg = "ECHO " + i;
				

				if(i==messages){
					call.setProperty(Constants.ClientProperties.LAST_MESSAGE, new Boolean(true));
				}
				
				String ret = (String) call.invoke(new Object[]{msg,"abcdef"});
				System.out.println("Got response from server " + ret);
				System.out.println("e44444");
			}
			System.out.println("e55555");
            RMInitiator.stopClient();
            System.out.println("e66666");
        } catch (Exception e) {
            e.printStackTrace();
        }		
	}
	
}
