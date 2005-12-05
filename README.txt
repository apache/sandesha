==========================================================================
Apache Sandesha2 0.9 build (05 December 2005)

http://ws.apache.org/sandesha/sandesha2
---------------------------------------------------------------------------

Apache Sandesha2 is a WS-ReliableMessaging implementation on top of Apache 
Axis2. If you are looking for a WS-ReliableMessaging implementation for 
Apache Axis 1.x, please go for Sandesha 1.0 which is located at
http://svn.apache.org/repos/asf/webservices/sandesha/branches/sandesha_1_0/

----------------------------------------------------------------------------

Installation
============
Download and install Apache Axis2. (see http://ws.apache.org/axis2 for more
details).
Add and phase called RMPhase after the postDispatch phase to all four flows 
of the Axis2.xml.
Get the binary distribution of Sandesha2 and extract it. You will find the 
sandesha2-0.9.mar file inside that. This is the current Sandesha2 module 
file.
Put Sandesha2 module file to <Axis2_webapp>/WEB-INF/modules directory.
Put sandesha2.properties file to <Axis2_webapp>/WEB-INF/classes directory (this
can also be in any other place of your classpath).
 
Using Sandesha2 in the server side
===================================
Put a module reference for the Sandesha module in the services.xml files of the
services to which you hope to give the RM capability.
For e.g.
<service>
    <module ref="sandesha2-0.9" />
    ...........
    ...........
</service>

Using Sandeshsa2 in the client side
===================================

Engage Sandesha2-0.9 module to the call object or the MessageSender object before
you doing any invocation. Also add set the property "WSRMLastMessage" (given by
org.apache.sandesha2.ClientProperties.LAST_MESSAGE ) to "true", before doing the 
last invocation. 

Example code:
MessageSender sender = new MessageSender (AXIS2_CLIENT_REPO_PATH);
sender.engageModule(new QName ("Sandesha2-0.9"));
Options clientOptions = new Options ();
sender.setClientOptions(clientOptions);
clientOptions.setProperty(Options.COPY_PROPERTIES,new Boolean (true));
clientOptions.setTo(new EndpointReference(toEPR));
sender.send("ping",getPingOMBlock("ping1"));
sender.send("ping",getPingOMBlock("ping2"));
clientOptions.setProperty(Sandesha2ClientAPI.LAST_MESSAGE, "true");
sender.send("ping",getPingOMBlock("ping3"));

Please see Sandesha2 user guide for more advance details on configuring Sandesha2.


Documentation
=============
Documentation for Sandesha2 can be found in xdocs directory in the Sandesha2 
distribution.

Support
=======
Please post any problem you encounter to the sandesha developer list 
(sandesha-dev@ws.apache.org). Please remember to mart the subject with the [Sandesha2]
prefix. Your comments are highly appreciated and really needed to make this distribution
a successful one.

Apache Sandesha2 team.