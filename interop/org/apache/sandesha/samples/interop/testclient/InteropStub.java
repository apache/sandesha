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
import org.apache.axis.message.addressing.util.AddressingUtils;
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
	
	
    public void runPing(InteropBean bean){
		
        
        String target = bean.getTarget();
		String from = bean.getFrom();
		String replyTo = bean.getReplyto();
		String acksTo = bean.getAcksTo();
		String acks = bean.getAcks();
		String terminate = bean.getTerminate();
		String operation = bean.getOperation();
		int messages = bean.getNoOfMsgs();
			
		System.out.println(target);
		System.out.println(from);
		System.out.println(replyTo);
		System.out.println(acksTo);
		System.out.println(acks);
		System.out.println(terminate);
		System.out.println(operation);
		System.out.println(messages);
		
		
		if(replyTo!=null && replyTo.equalsIgnoreCase("anonymous"))
		    replyTo = AddressingUtils.getAnonymousRoleURI();
		
		if(from!=null && from.equalsIgnoreCase("anonymous"))
		    from = AddressingUtils.getAnonymousRoleURI();
		
		if(acksTo!=null && acksTo.equalsIgnoreCase("anonymous"))
		    acksTo = AddressingUtils.getAnonymousRoleURI();
		
		
		try {
		    boolean sync = false;
			
		    if(acksTo.equals(AddressingUtils.getAnonymousRoleURI())){
			    sync = true;
			}

			System.out.println("********Running ping 2**********");
			
			RMInitiator.initClient(sync);

			Service service = new Service();
			Call call = (Call) service.createCall();

			System.out.println("Ping sync:" + sync);
			call.setProperty(Constants.ClientProperties.SYNC, new Boolean(sync));
			
			call.setProperty(Constants.ClientProperties.ACTION, "sandesha:ping");
            
			
			//these three properties are optional
			call.setProperty(Constants.ClientProperties.ACKS_TO,acksTo);
			
			if(from!=null && from!="")
			    call.setProperty(Constants.ClientProperties.FROM,from);
			
			if(replyTo!=null && replyTo!="")
			    call.setProperty(Constants.ClientProperties.REPLY_TO,replyTo);
			
			call.setTargetEndpointAddress(target);
			call.setOperationName(new QName("RMInteropService", operation));
			call.setTransport(new RMTransport(target, ""));

			call.addParameter("arg1", XMLType.XSD_STRING, ParameterMode.IN);
            
			for(int i=1;i<=messages;i++){
				call.setProperty(Constants.ClientProperties.MSG_NUMBER, new Long((i)));
				if(i==messages){
					call.setProperty(Constants.ClientProperties.LAST_MESSAGE, new Boolean(true));
				}
				String msg = "Ping Message Number " + i;
				call.invoke(new Object[]{msg});
	
			}

			RMInitiator.stopClient();	

			} catch (Exception e) {
				e.printStackTrace();
			}	
    }
    
    public void runEcho(InteropBean bean){	
        
        String target = bean.getTarget();
		String from = bean.getFrom();
		String replyTo = bean.getReplyto();
		String acksTo = bean.getAcksTo();
		String acks = bean.getAcks();
		String terminate = bean.getTerminate();
		String operation = bean.getOperation();
		int messages = bean.getNoOfMsgs();
			
		System.out.println(target);
		System.out.println(from);
		System.out.println(replyTo);
		System.out.println(acksTo);
		System.out.println(acks);
		System.out.println(terminate);
		System.out.println(operation);
		System.out.println(messages);
		
		
		if(replyTo!=null && replyTo.equalsIgnoreCase("anonymous"))
		    replyTo = AddressingUtils.getAnonymousRoleURI();
		
		if(from!=null && from.equalsIgnoreCase("anonymous"))
		    from = AddressingUtils.getAnonymousRoleURI();
		
		if(acksTo!=null && acksTo.equalsIgnoreCase("anonymous"))
		    acksTo = AddressingUtils.getAnonymousRoleURI();
		
		
		try {
		    boolean sync = false;
		    
			System.out.println("********Running echo 2**********");
			
			RMInitiator.initClient(sync);

			Service service = new Service();
			Call call = (Call) service.createCall();

			System.out.println("Echo sync:" + sync);
			call.setProperty(Constants.ClientProperties.SYNC, new Boolean(sync));
			
			call.setProperty(Constants.ClientProperties.ACTION, "urn:wsrm:echoString");
            
			
			//these three properties are optional. If not set they will be set depending on sync.
			call.setProperty(Constants.ClientProperties.ACKS_TO,acksTo);
			
			if(from!=null && from!="")
			    call.setProperty(Constants.ClientProperties.FROM,from);
			
			if(replyTo!=null && replyTo!="")
			    call.setProperty(Constants.ClientProperties.REPLY_TO,replyTo);
			
			System.out.println("reply to is :" + replyTo);
			call.setTargetEndpointAddress(target);
			call.setOperationName(new QName("RMInteropService", operation));
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
    
   /* public void runPingSync(InteropBean bean){
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
            call.setProperty(Constants.ClientProperties.ACKS_TO,
                    Constants.WSA.NS_ADDRESSING_ANONYMOUS);
			//These two are additional
			//call.setProperty("from","http://schemas.xmlsoap.org/ws/2003/03/addressing/role/anonymous");
			//call.setProperty("replyTo","http://10.10.0.4:8080/axis/services/MyService");
			//http://schemas.xmlsoap.org/ws/2003/03/addressing/role/anonymous

			call.setProperty(Constants.ClientProperties.FROM,AddressingUtils.getAnonymousRoleURI());
			System.out.println("ANON IS |:" + AddressingUtils.getAnonymousRoleURI());
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
	            call.setProperty(Constants.ClientProperties.FROM,AddressingUtils.getAnonymousRoleURI());
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
	}*/
	
}
