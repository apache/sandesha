ReadMe.txt
--------------
Purpose: - To introduce step-by-step configuration of “Sandesha” implementation.

Pre-Conditions
------------------
1. Both client side and server side have to configure Tomcat and axis successfully
2. Client Application has to configure in different JVM.
3. Tomcat at the Client side has to start form different folder than Client folder.


    Note 1: - This file has 2 sections; section 1 will introduce how to configure the
	 	“Sandesha” in a general situation. The section 2 will introduce how to configure 
	 	the interop samples.

	Note 2: - Every section has two-configuration Client side and Server side


=======================================================================================
Section 1
----------
Sandesha Configuration
-----------------------------
1.1 Server Configuration
-------------------------------
Step 1. Copy all Sandesha classes to Tomat axis folder.
	Copy all org,apache.sandesha.* classes to TomcatHome/webapps/axis/WEB-INF/classes.

	Note: - Now Sandesha ready at server side.

Step 2. Deploy services with RMProvider, RMServerRequestHandler and AddressingHandler.
	If a service need to support for WS-RM, then the service has deploy with RMProvider, 
	RMServerRequestHandler and AddressingHandler.

	Note: - the sample wsdd file for deployment is available in
		interop->org->apache->sandesha->sample->interop folder or sees below.

1.2 Client Configuration
--------------------------------
Step 1. Copy all Sandesha classes to Tomat axis folder.
	Copy all org,apache.sandesha.* classes to TomcatHome/webapps/axis/WEB-INF/classes.

Step 2. Deploy RMClientService and RMClientReference.

	i. Deploy RMClientService. (no special configuration needed )
	
	ii. Deploy RMClientReference with RMServerRequestHandler, AddressingHandler and 
	  RMProvider.

	Note 1: - The wsdd for deployment of the two service is placed at 
			org->apache->sandesha->client.

	Note 2: - Now Sandesha ready at Client side

Stap 3:- Deploy the client application with RMClientRequestHandler and 	RMClientResponseHandler. 
	
	Note 1: - For RMClientRequestHandler need to set appropriate values for the following
	 	parameter in the WSDD file 
		1.Source URI: has to specify the IP address of the Client machine and port of 
		the Client side Tomcat.
		
		2.Reply To: has to specify the IP address of the Client machine that is going 
		to receive the reply from the server and port of the receiver machine Tomcat.
		
		3.Synchronized has to specify, whether the client expect a synchronize 
		message exchange or asynchronies message exchange.

		4.Action: has to specify the action of the request.

	Note: - the sample wsdd file for deployment is available in 
	interop->org->apache->sandesha->sample->interop folder or sees below.

========================================================================================

Section 2.
------------
Interop Sample Configuration
------------------------------------
Pre-Conditions: - 

1.The above steps for Sandesha configuration has to be finished up to Sandesha Ready stage
 at both client and server side. 
 
2.1 Server Side Configurations 
------------------------------------
Step 1:- Copy org.apache.sandesha.sample.interop.PingService.class and
 	org.apache.sandesha.sample.interop.EchoStringService.class to
 	TomcatHome->webapps->axis->WEB-INF->classes

Step 2:- Deploy the service with RMProvider, RMServerRequestHandler and AddressingHandler. 

	Note :- Sample WSDD is placed at interop->org->apache->sandesha->samples->interop

2.2 Client Side Configuration
-----------------------------------

	In this section we present the steps based on scenarios that has mention in the 
	WS-Reliable Messaging: Interop Workshop Scenarios Document.

Pre-Condition
-----------------
	Set the parameter in the Client Deploy WSDD as follows.
	
	1. Set the source URI and Reply To for your Client machine IP address and Tomcat
		port.
	2. Set Action as follows
	i. For Ping Service – wsrm:Ping
	ii. For Echo String Service – wsrm:EchoString
	
	Note: - To deploy the client the following java run time command will be useful.
	java org.apache.axis.utils.Admin ClientDeploy.wsdd       


Scenario 1.1
---------------

In this Scenario we have two options:
1.Asynchronous
2.Synchronous
 
1.Asynchronous

Step 1: Set the synchronous as false for ping service in the Client Deploy WSDD and deploy
 	the Client

Step 2: Run the Scenario_1_1_Client.

2.Synchronous

Step 1: Set the synchronous as true for ping service in the Client Deploy WSDD and deploy the
 	Client

Step 2: Run the Scenario_1_1_Client.

Scenario 1.2
---------------

Physically disconnect the Client and Server and do the step in Scenario 1.1
and while running connect the both.

Scenario 2.1
---------------

Follow the step in Scenario 1.1, but in step 2 of the every section run the Scenario_1_2_Client.

	Note :- From this point on word we are going to support for Asynchronous mode only
	 	for all Set the synchronous as false for ping service and Echo Service in the Client
	 	Deploy WSDD and deploy the Client


Scenario 2.2, Scenario 2.3
-----------------------------------------------
Run the Scenario_2_2_Client and Scenario_2_3_Client with above configuration.




	






