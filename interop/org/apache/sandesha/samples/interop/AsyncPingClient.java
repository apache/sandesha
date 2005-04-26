/*
* Copyright 1999-2004 The Apache Software Foundation.
*
* Licensed under the Apache License, Version 2.0 (the "License"); you may not
* use this file except in compliance with the License. You may obtain a copy of
* the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
* WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
* License for the specific language governing permissions and limitations under
* the License.
*
*/

  package org.apache.sandesha.samples.interop;

  import org.apache.axis.client.Call;
  import org.apache.axis.client.Service;
  import org.apache.axis.encoding.XMLType;
  import org.apache.sandesha.Constants;
  import org.apache.sandesha.RMInitiator;
  import org.apache.sandesha.RMTransport;

  import javax.xml.namespace.QName;
  import javax.xml.rpc.ParameterMode;

  public class AsyncPingClient {
  private static String defaultServerPort="8070";
  private static String defaultClientPort="9070";
  private static String targetURL = "http://127.0.0.1:"+defaultServerPort+"/axis/services/RMInteropService?wsdl";

  public static void main(String[] args) {
      System.out.println("Client started...... Asynchronous ");
      try {

          RMInitiator.initClient(false);

          Service service = new Service();
          Call call = (Call) service.createCall();

          call.setProperty(Constants.ClientProperties.SYNC, new Boolean(false));
          call.setProperty(Constants.ClientProperties.ACTION, "sandesha:ping");

          //These Three are additional
          call.setProperty("from","http://127.0.0.1:"+defaultClientPort+"/axis/services/RMService");
          //<was:ReplyTo> needs to be set by the client, only if the response to a particular
          //invocation needs to be sent to a some other endpoint other than client endpoint.
          //call.setProperty("replyTo","http://127.0.0.1:"+defaultClientPort+"/axis/services/RMService");
          //<wsrm:AcksTo> is used, if the user needs the CreateSequence response or the faults related to
          //that to be sent to a specific endpoint.
          call.setProperty("acksTo","http://127.0.0.1:"+defaultClientPort+"/axis/services/RMService");
          //http://schemas.xmlsoap.org/ws/2003/03/addressing/role/anonymous

          call.setTargetEndpointAddress(targetURL);
          call.setOperationName(new QName("RMInteropService", "ping"));
          call.setTransport(new RMTransport(targetURL, ""));

          call.addParameter("arg1", XMLType.XSD_STRING, ParameterMode.IN);

          //First Message
          call.setProperty(Constants.ClientProperties.MSG_NUMBER, new Long(1));
          call.invoke(new Object[]{"Ping Message Number One"});

          //Second Message
          call.setProperty(Constants.ClientProperties.MSG_NUMBER, new Long(2));
          call.invoke(new Object[]{"Ping Message Number Two"});

          //Third Message
          call.setProperty(Constants.ClientProperties.MSG_NUMBER, new Long(3));
          call.setProperty(Constants.ClientProperties.LAST_MESSAGE, new Boolean(true)); //For last message.
          call.invoke(new Object[]{"Ping Message Number Three"});

          RMInitiator.stopClient();

      } catch (Exception e) {
          //System.err.println(e.toString());
          e.printStackTrace();
      }
  }
  }
