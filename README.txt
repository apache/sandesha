======================================================
Apache Sandesha-1.0 build 28th July, 2005)

http://ws.apache.org/ws-sandesha
------------------------------------------------------

___________________
Documentation
===================

Documentation can be found in the docs/ directory included in the
binary distribution and in xdocs/ directory in the source distribution.

___________________
Installation
===================

The binary distribution contains a Sandesha-1.0.jar file. To configure Sandesha
in the server side simply follow the steps given bellow.

Step1: Copy  Sandesha-1.0.jar to the webapps/axis/WEB-INF/lib directory. (Assume that
       the user has already configured axis in a servlet container)
Step2: Copy addressing-SNAPSHOT.jar that can be found in the lib directory of the binary
       distribution of Sandesha to the same location.

If you need to test with samples then copy the Sandesha-samples.jar that can be found in the 
samples directory to the webapps/axis/WEB-INF/lib directory as well.

To verify the installation, go to http://localhost:8080/axis/ and
click on the "List" link. Then if you can see the RMSampleService deployed (this is only
if the Sandesha-samples.jar is also copied to the webapps/axis/WEB-INF/lib) then
you have successfully configured Sandesha on the server side.

________________________________________________
Enabling WS-ReliableMessaging for a Web Service
================================================

To enable WS-RM for a Web Service, the user has to add two handlers and a provider to the
server-config.wsdd of axis. Following xml lines shows the required changes
to enable WS-RM for a Web Service named MyService to its deployment descriptor

--------------------------------------------------------------------------------------------
<deployment xmlns="http://xml.apache.org/axis/wsdd/"
xmlns:java="http://xml.apache.org/axis/wsdd/providers/java">
 <service name="MyService" provider="Handler">
<requestFlow>
<handler type="java:org.apache.sandesha.ws.rm.handlers.RMServerRequestHandler"></handler>
<handler type="java:org.apache.axis.message.addressing.handler.AddressingHandler"></handler>
</requestFlow>
<parameter name="handlerClass" value="org.apache.sandesha.ws.rm.providers.RMProvider"/>
<parameter name="className" value="test.MyService"/>
<parameter name="allowedMethods" value="*"/>
<parameter name="scope" value="request"/>
</service>
</deployment>
----------------------------------------------------------------------------------------------


To build the Sandesha-1.0.jar file using the source distribution use the
following command:
    $ maven

The jar file will be created in the target/ directory.

_______________________________________________________
Enabling WS-ReliableMessaging for a Web Service Client
=======================================================

To enable WS-RM for a Web Service client the user has to add few code lines to the existing client
code. (Currently Sandesha can only use with the DII based clients, for clients that use generated
stubs, then the user has to manually add these lines to the stubs)

Following code fragment shows these additional lines that are required. A more detailed explanation
can be found in the User Guide.

----------------------------------------------------------------------------------------------
Service service = new Service();
	Call call = (Call) service.createCall();
	SandeshaContext ctx = new SandeshaContext();
	ctx.initCall(call, targetUrl,
	"urn:wsrm:echoString",Constants.ClientProperties.IN_OUT);
	call.setOperationName(new QName("http://tempuri.org/", "echoString"));
	call.addParameter("Text", XMLType.XSD_STRING, ParameterMode.IN);
	call.addParameter("Seq", XMLType.XSD_STRING, ParameterMode.IN);
	call.setReturnType(org.apache.axis.encoding.XMLType.XSD_STRING);

	ctx.setLastMessage(call);
	String ret = (String) call.invoke(new Object[]{"Sandesha Echo 1", "abcdef"});
	System.out.println("The Response for First Messsage is :" + ret);

	ctx.endSequence();
	} catch (Exception e) { e.printStackTrace();
	}
----------------------------------------------------------------------------------------------

___________________
Support
===================

Any problem with this release can be reported to sandesha the mailing list
If you are sending email to the mailing list make sure to add the [Sandesha] prefix to the subject.

Mailing list subscription:
    sandesha-dev-subscribe@ws.apache.org


Thank you for your support on Sandesha!

Apache Sandesha Team.
