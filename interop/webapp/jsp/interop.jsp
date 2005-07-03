<%@ page import="org.apache.sandesha.server.*,org.apache.sandesha.interop.testclient.*,org.apache.sandesha.client.ClientStorageManager,org.apache.sandesha.ws.rm.providers.RMProvider,javax.servlet.jsp.*,
                 java.io.Writer,
                 java.io.PrintWriter,
                 org.apache.sandesha.IStorageManager,
                 org.apache.sandesha.Constants,
                 org.apache.axis.SimpleChain,
                 java.util.Properties,
                 java.io.InputStream,
                 java.io.IOException,
                 org.apache.sandesha.ws.rm.providers.RMClientProvider"%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<jsp:useBean id="interopBean" scope="request" class="org.apache.sandesha.interop.testclient.InteropBean" />
<jsp:setProperty name="interopBean" property="*" />

<%
out.println("<html>");
out.println("<head>");

    /////////////////////Load Properties///////////////////////////
    String ip=null;
    String port=null;
    String warName=null;
    String defaultTarget=null;
    String defaultAsyncEndPoint=null;
       Properties properties = new Properties();
        try {

            ClassLoader cl = this.getClass().getClassLoader();
            InputStream is = cl.getResourceAsStream("sandesha-interop.properties");
            properties.load(is);
            ip=properties.getProperty("IP");
            port=properties.getProperty("PORT");
            warName=properties.getProperty("WAR_NAME");
            defaultTarget="http://"+ip+":"+port+"/"+warName+"/services/RMInteropService";
            defaultAsyncEndPoint="http://"+ip+":"+port+"/"+warName+"/services/RMService";


        } catch (IOException e) {
            e.printStackTrace();
        }
      ResponseWriter writer = new ResponseWriter (response.getWriter());

    ///////////////////////////////////////////////////////////////

%>

<title>Welcome to Apache Sandesha Innterop Test</title>
<script>

		function changeSelect(itm,val){
			txtItem = itm;

			if(val.value=="none"){
				document.getElementById(txtItem).value = "";
				//document.getElementById(txtItem).disabled = true;
			}else if(val.value=="sync"){
				document.getElementById(txtItem).value = "anonymous";
				//document.getElementById(txtItem).disabled = true;
			}else if(val.value=="async"){
				document.getElementById(txtItem).value = "<%=defaultAsyncEndPoint%>";
                		//document.getElementById(txtItem).disabled = false;
			}
		}

		function setOperation (itm){
		//document.getElementById("txtfrom").value = itm.value;
			if(itm.value=="ping" || itm.value=="Ping"){
            document.getElementById("replyto1").disabled=false;
				//document.getElementById('replytoTR').style.display = 'none';
				//document.getElementById('offerTR').style.display = 'none';
				document.getElementById("replyto").value = "";
			}else if(itm.value=="echoString" || itm.value=="EchoString"){
				//document.getElementById('replytoTR').style.display = '';
				//document.getElementById('offerTR').style.display = '';
                document.getElementById("replyto1").disabled=true;
				document.getElementById("replyto").value = "<%=defaultAsyncEndPoint%>";
			}
		}


    </script>
<script language="JavaScript" type="text/JavaScript">
<!--
function MM_reloadPage(init) {  //reloads the window if Nav4 resized
  if (init==true) with (navigator) {if ((appName=="Netscape")&&(parseInt(appVersion)==4)) {
    document.MM_pgW=innerWidth; document.MM_pgH=innerHeight; onresize=MM_reloadPage; }}
  else if (innerWidth!=document.MM_pgW || innerHeight!=document.MM_pgH) location.reload();
}
MM_reloadPage(true);
//-->
</script>
</head>
<body>
<h1>Apache Sandesha Interop Testing</h1>

<form method="post" name="InteropTesting" action="interop.jsp">

<table width='100%'>
	<tr id='server' style="display:''">
		<td>
		<table width="100%">
		<tr><td><font size="+1">Server Endpoint</font></td><td><font size="+1"><%=defaultTarget%></font></td></tr>
		<tr><td colspan="2" >
	     <a href="<%=defaultTarget%>?wsdl"><h3><u>WSDL</h3></u></a>
		</td></tr>
        <tr><td colspan="2" >
	     <a href="http://ws.apache.org/sandesha/interopguide.html"><h4><u>Interop Guide</h4></u></a>
     		<br /><hr/>
		</td></tr>
		</table>
		</td>
	</tr>

	<tr >
      <td>

      <table id='client' width='100%' style="display:''" >
          <tr>
            <td colspan='5'><h3><u>Test Client</u></h3></td>
          </tr>
          <tr>
            <td>Target</td>
            <td colspan="4"><input type='text' size='80' name='target'  value="<%=defaultTarget%>"/></td>
          </tr>
          <tr>
            <td width='20%'>Operation</td>
            <td colspan="4"><select name='operation' onchange="setOperation(this)" >
                <option value="Ping">Ping</option>
                <option value="echoString">echoString</option>
              </select></td>
          </tr>
          <tr id="fromTR">
            <td width='20%' >wsrm:AcksTo</td>
            <td >
                <table width="100%">
                        <tr>
                                <td> <select name='acksTo1' onchange="changeSelect('acksTo',this)">
                                        <option value="sync">Synchronous</option>
                                        <option value="async">Asynchronous</option>
                                        </select>
                                </td>
                                <td>
                                        <input type='text' size='80' name='acksTo'  id='acksTo' value='anonymous' />
                                </td>
                        </tr>
                </table>
           </td>
           </tr>
          <tr id="fromTR">
            <td width='20%' >wsa:From</td>
            <td >
                <table width="100%">
                        <tr>
                                <td> <select name='from1' onchange="changeSelect('from',this)" >
<%--                                        <option value="none">none</option>--%>
                                        <option value="sync">Synchronous</option>
                                        <option value="async">Asynchronous</option>
                                        </select>
                                </td>
                                <td>
                                        <input type='text' size='80' name='from'  id='from' value='anonymous'/>
                                </td>
                        </tr>
                </table>
           </td>
        </tr>
          <tr id="replytoTR" >
            <td width='20%' >wsa:ReplyTo</td>
            <td >
                <table width="100%">
                        <tr>
                                <td>
                                <select name='replyto1' onchange="changeSelect('replyto',this)">
                      				<option value="none">none</option>
                      				<option value="sync">Synchronous</option>
                      				<option value="async">Asynchronous</option>
                    			</select>
                                </td>
                                <td>
                                        <input type='text' size='80' name='replyto'  id='replyto' />
                                </td>
                        </tr>
                </table>
           </td>
        </tr>

           <tr id="faulttoTR">
            <td width='20%' >wsa:FaultTo</td>
            <td >
                <table width="100%">
                        <tr>
                                <td>
                                <select name='faultto1' onchange="changeSelect('faultto',this)">
                      				<option value="none">none</option>
                      				<option value="sync">Synchronous</option>
                      				<option value="async">Asynchronous</option>
                    			</select>
                                </td>
                                <td>
                                        <input type='text' size='80' name='faultto'  id='faultto' />
                                </td>
                        </tr>
                </table>
           </td>
        </tr>

        <tr id="offerTR" >
            <td width='20%' >Send Offer</td>
            <td >
                <table width="100%">
                        <tr>
                                <td>
                                <select name='offer'>
                      				<option value="no">no</option>
                      				<option value="yes">yes</option>
                    			</select>
                                </td>
                        </tr>
                </table>
           </td>
        </tr>


        <tr>
            <td colspan='1'>No. of Messages</td>
            <td colspan ='10' ><select name='noOfMsgs'>
                <option value="1">1</option>
                <option value="2">2</option>
                <option value="3">3</option>
                <option value="4">4</option>
                <option value="5">5</option>
                <option value="6">6</option>
                <option value="7">7</option>
                <option value="8">8</option>
                <option value="9">9</option>
                <option value="10">10</option>
              </select></td>
          </tr>


        </table></td>
	</tr>
    <tr></tr>
</table>

  <input name="submit" type='submit' value='Run test' />
</form>

<hr />
<% out.flush();%>
<span>
<%
   	runTest(interopBean,writer,defaultAsyncEndPoint);
    writer.flush();
%>
</span>


</body>
</html>



<%!

public void runTest(InteropBean bean,ResponseWriter writer,String defaultAsyncEndPoint) throws Exception {

//    System.out.println("AcksTo "+bean.getAcksTo());
//    System.out.println("FaultTo "+bean.getFaultto());
//    System.out.println("From "+bean.getFrom());
//    System.out.println("No of Msf "+bean.getNoOfMsgs());
//    System.out.println("Opertation "+bean.getOperation());
//    System.out.println("ReplyTo"+bean.getReplyto());
//    System.out.println("Source "+bean.getSourceURL());
//    System.out.println("Target "+bean.getTarget());
//    System.out.println("Offer "+bean.getOffer());


	String to = null;
	if(bean!=null){
		to = bean.getTarget();
        bean.setSourceURL(defaultAsyncEndPoint);
	}

	if(to!=null) {
          	writer.write(" <span><br /><h3> Starting Test ....... <br /></h3> ");
			writer.flush();


			InteropCallback callback = new InteropCallback (writer);

            ClientStorageManager csm = new ClientStorageManager ();

            csm.setCallback(callback);
			RMProvider.setCallback(callback);
            RMClientProvider.setCallback(callback);
			Sender.setCallback(callback);

            //InteropStub stub= InteropStub.getInstance();
            InteropStub stub= InteropStub.getInstance();
            stub.setCallback(callback);

        	if(bean.getOperation().equalsIgnoreCase("ping")){
		       stub.runPing(bean);
            }else if(bean.getOperation().equalsIgnoreCase("echoString") ){
   		      stub.runEcho(bean);
		    }

            writer.write ("  <br /> <br /><h3>Test Finished... </h3>");
			writer.write ("<hr /><br /></span>");

			csm.removeCallback();
			RMProvider.removeCallback();
		}

    }
%>