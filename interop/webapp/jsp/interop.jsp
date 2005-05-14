<%@ page import="org.apache.sandesha.server.*,org.apache.sandesha.samples.interop.testclient.*,org.apache.sandesha.client.ClientStorageManager,org.apache.sandesha.ws.rm.providers.RMProvider,javax.servlet.jsp.*"%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<jsp:useBean id="interopBean" scope="request" class="org.apache.sandesha.samples.interop.testclient.InteropBean" />
<jsp:setProperty name="interopBean" property="*" />

<%
response.getWriter().println("<html>");
response.getWriter().println("<head>");
response.getWriter().flush();
%>

<title>Hi!! Welcome to Apache Sandesha interop test</title>
<script>
    
       function displayServer(){
              document.getElementById('server').style.display = '';
        }
        
        function hideServer(){
              document.getElementById('server').style.display = 'none';
        }

       function displayClient(){
              document.getElementById('client').style.display = '';
        }
        
        function hideClient(){
              document.getElementById('client').style.display = 'none';
        }
        
        function displayEchoText(){
              document.getElementById('echo').style.display = '';
        }
        
        function hideEchoText(){
              document.getElementById('echo').style.display = 'none';
        }
		
		function changeSelect(itm,val){
			txtItem = itm;
			
			if(val.value=="none"){
				document.getElementById(txtItem).value = "";
				//document.getElementById(txtItem).disabled = true;
			}else if(val.value=="sync"){
				document.getElementById(txtItem).value = "anonymous";
				//document.getElementById(txtItem).disabled = true;
			}else if(val.value=="async"){
				document.getElementById(txtItem).value = "http://127.0.0.1:9070/axis/services/RMService";
				//document.getElementById(txtItem).disabled = false;
			}
		}
		
		function setOperation (itm){
		//document.getElementById("txtfrom").value = itm.value;
			if(itm.value=="ping" || itm.value=="Ping"){
				document.getElementById('replytoTR').style.display = 'none';
				document.getElementById('offerTR').style.display = 'none';
			}else if(itm.value=="echoString" || itm.value=="EchoString"){
				document.getElementById('replytoTR').style.display = '';
				document.getElementById('offerTR').style.display = '';
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
<%
	
	String endPoint         = request.getParameter("endPoint");
	String method = request.getParameter("method");
	String run = request.getParameter("running");
	
    if(endPoint == null)
        endPoint = "server";
        
	String displayServerArea,displayClientArea,serverSelected,clientSelected;
	displayServerArea = "\'\'";
	displayClientArea = "none";
	serverSelected = "true";
	clientSelected = "false";
	
    if(endPoint.equals("client")){
    	displayServerArea = "none";
    	displayClientArea = "\'\'";	
        serverSelected = "false";
	    clientSelected = "true";
    } 
	
	/*out.println( "client variable sorc is " + endPoint);
	out.println( "client variable method is " + endPoint);
	out.println( "running is " + run); */

	
%>
<form method="post" name="InteropTesting" action="interop.jsp">

<table width='100%'>
	<tr id='server' style="display:''">
		<td>
		<table width="100%">
		<tr><td><h3><u>Server endpoint</u></h3></td></tr>
		<tr><td>
		http://sandeshaSL.org:8080/interoptest
		<br /><hr />
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
            <td colspan="4"><input type='text' size='80' name='target'  /></td>
          </tr>
          <tr> 
            <td width='20%'>Operation</td>
            <td colspan="4"><select name='operation' onchange="setOperation(this)">
                <option value="Ping">Ping</option>
                <option value="echoString">echoString</option>
              </select></td>
          </tr>
          <tr id="fromTR"> 
            <td width='20%' >Acks to</td>
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
            <td width='20%' >From</td>
            <td > 
                <table width="100%">
                        <tr> 
                                <td> <select name='from1' onchange="changeSelect('from',this)">
                                        <option value="none">none</option>
                                        <option value="sync">Synchronous</option>
                                        <option value="async">Asynchronous</option>
                                        </select> 
                                </td>
                                <td> 
                                        <input type='text' size='80' name='from'  id='from' /> 
                                </td>
                        </tr>
                </table>
           </td>
        </tr>
          <tr id="replytoTR" style="display:none"> 
            <td width='20%' >Reply to</td>
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
        
        <tr id="offerTR" style="display:none" > 
            <td width='20%' >Offer seq</td>
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
            <td width='20%' >Send messages</td>
            <td > 
                <table width="100%">
                        <tr> 
 							<td width="4%">acks</td>
           					 <td width="13%"><input type="checkbox" name="acks" value="checked" checked></td>
            				<td width="14%">Terminate seq</td>
           					 <td width="49%"><input type="checkbox" name="terminate" value="checked" checked></td>
                        </tr>
                </table>
           </td>
        </tr>
        <tr> 
            <td colspan='1'>no. of Msgs</td>
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
</table>

  <input name="submit" type='submit' value='Run test' />
</form>

<hr />

<%
	
	runTest(interopBean,response);
%>

</body>
</html>



<%!

public void runTest(InteropBean bean,HttpServletResponse res) throws Exception {
 
	String to = null;
	if(bean!=null){
		to = bean.getTarget();
	}

	if(to!=null) {
			//session.setParameter("runTest");
			ResponseWriter writer = new ResponseWriter (res.getWriter());
			 
			writer.write(" <br /> Starting test ....... <br /> ");
			writer.flush();

			String target = bean.getTarget();
			String from = bean.getFrom();
			String replyTo = bean.getReplyto();
			String acks = bean.getAcks();
			String terminate = bean.getTerminate();
			String operation = bean.getOperation();
			int messages = bean.getNoOfMsgs();

			//set the callbacks


			//run the service

	
			// ***********  code to run client test

			//create callback classe and register
			InteropCallback callback = new InteropCallback (writer);
			

			
			ClientStorageManager csm = new ClientStorageManager ();
			csm.setCallback(callback);
			RMProvider.setCallback(callback);
			Sender.setCallback(callback);

			
			//start the test
			TestRunnerThread runner = new TestRunnerThread ();
			//runner.setDaemon(true);
			//runner.setMethod (method);
			runner.setBean(bean);
			

			
			runner.start();
						
			while(!callback.isTestFinished()){

				Thread.sleep(100);			
			}	
			
	
			writer.write ("  <br /> <br />Test finished... ");	
			writer.write ("<hr /><br />");	
			writer.flush();
			
			csm.removeCallback();
			RMProvider.removeCallback();
		}
}
%> 