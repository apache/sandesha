package org.apache.sandesha;

import org.apache.axis.AxisFault;
import org.apache.axis.Handler;
import org.apache.axis.SimpleChain;
import org.apache.axis.client.Call;
import org.apache.axis.components.logger.LogFactory;
import org.apache.axis.configuration.SimpleProvider;
import org.apache.axis.description.JavaServiceDesc;
import org.apache.axis.handlers.soap.SOAPService;
import org.apache.axis.transport.http.SimpleAxisServer;
import org.apache.commons.logging.Log;
import org.apache.sandesha.client.ClientStorageManager;
import org.apache.sandesha.server.RMInvoker;
import org.apache.sandesha.server.Sender;
import org.apache.sandesha.server.ServerStorageManager;
import org.apache.sandesha.util.PolicyLoader;
import org.apache.sandesha.util.PropertyLoader;
import org.apache.sandesha.ws.rm.providers.RMProvider;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * SandeshaContext will keep track of different Call objects that the user may use inside
 * a single client instance. SandeshaContext provides the user with an API to initialize and
 * end sequences. With the "endSequence(Call call) method the user is provide with the option
 * of accepting a RMReport which contains the overall status of the message transfer.
 */
public class SandeshaContext {

    private static boolean rmInvokerStarted = false;
    private static boolean cleintSenderStarted = false;
    private static boolean serverSenderStarted = false;
    private static boolean listenerStarted = false;
    private static SimpleAxisServer sas = null;
    private static Sender cleintSender;
    private static Sender serverSender;
    private static boolean insideServer;

    private static HashMap callMap = new HashMap();
    private static int activeSequenes = 0;

     public SandeshaContext(){
            SandeshaContext.insideServer = false;
     }

    public SandeshaContext(boolean insideServer) {
        SandeshaContext.insideServer = insideServer;
    }


    public static HashMap getCallMap() {
        return callMap;
    }

    public static void setCallMap(HashMap callMap) {
        SandeshaContext.callMap = callMap;
    }

    private static final Log log = LogFactory.getLog(SandeshaContext.class.getName());

    public static IStorageManager init(boolean client) {
        if (client) {
            IStorageManager storageManager = new ClientStorageManager();
            if (!cleintSenderStarted) {
                System.out.println(Constants.InfomationMessage.SENDER_STARTED);
                cleintSender = new Sender(storageManager);
                SimpleChain reqChain = getRequestChain();
                SimpleChain resChain = getResponseChain();
                if (reqChain != null)
                    cleintSender.setRequestChain(reqChain);
                if (resChain != null)
                    cleintSender.setResponseChain(resChain);
                cleintSender.startSender();
                cleintSenderStarted = true;
            }
            return storageManager;
        } else {
            if (!serverSenderStarted) {
                System.out.println(Constants.InfomationMessage.SENDER_STARTED);
                serverSender = new Sender();
                serverSender.startSender();
                serverSenderStarted = true;
            }
            if (!rmInvokerStarted) {
                RMInvoker rmInvoker = new RMInvoker();
                rmInvoker.startInvoker();
                rmInvokerStarted = true;
            }
            return new ServerStorageManager();
        }
    }


    private static void validateProperties(Call call, String targetUrl, String action, short MEP)
            throws AxisFault {
        if (action == null)
            throw new AxisFault("Please sepeicfy Action");
        if (targetUrl == null)
            throw new AxisFault("TargetUrl cannot be null");
        if (call == null)
            throw new AxisFault("Call cannot be null");
        if (!(MEP == Constants.ClientProperties.IN_ONLY ||
                MEP == Constants.ClientProperties.IN_OUT))
            throw new AxisFault("Invalid MEP");
    }


    public void addNewSequeceContext(Call call, String targetUrl, String action, short MEP)
            throws AxisFault {
        String key = initialize(call, targetUrl, action, MEP);
        init(true);
        startListener();
        callMap.put(key, call);
        activeSequenes++;
    }

    public void addNewSequeceContext(Call call, String targetUrl, String action, short MEP,
                                     boolean sync) throws AxisFault {
        String key = initialize(call, targetUrl, action, MEP);
        setSynchronous(call);
        init(true);
        if (!sync) {
            startListener();
        }
        callMap.put(key, call);
        activeSequenes++;
    }

    public void endAllSequence(Call call) throws AxisFault {

        IStorageManager storageManager = new ClientStorageManager();
        long startingTime = System.currentTimeMillis();
        long inactivityTimeOut = PolicyLoader.getInstance().getInactivityTimeout();
        while (!storageManager.isAllSequenceComplete()) {
            try {
                System.out.println(Constants.InfomationMessage.WAITING_TO_STOP_CLIENT);
                Thread.sleep(Constants.CLIENT_WAIT_PERIOD_FOR_COMPLETE);
                if ((System.currentTimeMillis() - startingTime) >= inactivityTimeOut) {
                    stopClientByForce();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                log.error(e);
            }
        }

        endAllSequences();

    }

    public RMReport endSequence(Call call) throws AxisFault {

        IStorageManager storageManager = new ClientStorageManager();
        long startingTime = System.currentTimeMillis();
        long inactivityTimeOut = PolicyLoader.getInstance().getInactivityTimeout();
        String seqId = (String) call.getProperty(Constants.ClientProperties.CALL_KEY);
        while (!storageManager.isSequenceComplete(seqId)) {
            try {
                System.out.println(Constants.InfomationMessage.WAITING_TO_STOP_CLIENT);
                Thread.sleep(Constants.CLIENT_WAIT_PERIOD_FOR_COMPLETE);
                if ((System.currentTimeMillis() - startingTime) >= inactivityTimeOut) {
                    stopClientByForce();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                log.error(e);
            }
        }

        RMReport rmReport = (RMReport) call.getProperty(Constants.ClientProperties.REPORT);
        rmReport.setAllAcked(true);

        if (activeSequenes == 1) {
            if (listenerStarted) {
                sas.stop();
                listenerStarted = false;
            }
            cleintSender.stop();
            cleintSenderStarted = false;
            storageManager.clearStorage();
            activeSequenes--;
            return rmReport;
        }
        activeSequenes--;
        return rmReport;

    }


    public void endAllSequences() throws AxisFault {
        IStorageManager storageManager = new ClientStorageManager();
        if (activeSequenes >= 1) {
            stopClientByForce();
        } else {
            if (listenerStarted) {
                sas.stop();
                listenerStarted = false;
            }
            cleintSender.stop();
            cleintSenderStarted = false;
            storageManager.clearStorage();
            activeSequenes--;
        }

    }


    public static void stopClientByForce() throws AxisFault {
        if (listenerStarted) {
            sas.stop();

            //FOR JSP
            listenerStarted = false;
            //END JSP
            listenerStarted = false;
        }
        cleintSender.stop();

        //FOR JSP
        cleintSenderStarted = false;
        //END JSP
        throw new AxisFault("Inactivity Timeout Reached, No Response from the Server");
    }

    private static String initialize(Call call, String targetUrl, String action, short MEP)
            throws AxisFault {
        validateProperties(call, targetUrl, action, MEP);
        String keyOfCall = System.currentTimeMillis() + action;
        call.setTargetEndpointAddress(targetUrl);
        call.setProperty(Constants.ClientProperties.ACTION, action);
        call.setTransport(new RMTransport(targetUrl, ""));
        call.setProperty(Constants.ClientProperties.MEP, new Short(MEP));
        call.setProperty(Constants.ClientProperties.CALL_KEY, keyOfCall);
        call.setProperty(Constants.ClientProperties.REPORT, new RMReport());

        if (!insideServer) {
            InetAddress addr = null;
            try {
                addr = InetAddress.getLocalHost();
            } catch (UnknownHostException e) {
                log.error(e);
            }

            String sourceURL = Constants.HTTP + Constants.COLON + Constants.SLASH +
                    Constants.SLASH +
                    addr.getHostAddress() + Constants.COLON +
                    PropertyLoader.getClientSideListenerPort() + Constants.URL_RM_SERVICE;
            call.setProperty(Constants.ClientProperties.SOURCE_URL, sourceURL);
        }
        return keyOfCall;
    }


    private static void startListener() {
        if (!insideServer) {
            if (!listenerStarted) {
                listenerStarted = true;
                try {
                    System.out.println(Constants.InfomationMessage.CLIENT_LISTENER_STARTED);
                    sas = new SimpleAxisServer();

                    SimpleProvider sp = new SimpleProvider();
                    sas.setMyConfig(sp);

                    SimpleChain reqHandlers = getListenerRequestChain();
                    SimpleChain resHandlers = getListenerResponseChain();

                    RMProvider rmp = new RMProvider();
                    rmp.setClient(true);
                    SOAPService rmService = new SOAPService(reqHandlers, rmp, resHandlers);

                    JavaServiceDesc desc = new JavaServiceDesc();
                    rmService.setOption(Constants.ClientProperties.CLASS_NAME,
                            Constants.ClientProperties.RMSERVICE_CLASS);
                    rmService.setOption(Constants.ClientProperties.ALLOWED_METHODS,
                            Constants.ASTERISK);

                    desc.setName(Constants.ClientProperties.RMSERVICE);
                    rmService.setServiceDescription(desc);
                    sp.deployService(Constants.ClientProperties.RMSERVICE, rmService);
                    sas.setServerSocket(
                            new ServerSocket(PropertyLoader.getClientSideListenerPort()));

                    Thread serverThread = new Thread(sas);
                    serverThread.start();

                } catch (Exception e) {
                    log.error(e);
                }
            }
        }

    }


    public static SimpleChain getHandlerChain(List arr) {
        SimpleChain reqHandlers = new SimpleChain();
        Iterator it = arr.iterator();
        boolean hasReqHandlers = false;
        try {
            while (it.hasNext()) {
                hasReqHandlers = true;
                String strClass = (String) it.next();
                Class c = Class.forName(strClass);
                Handler h = (Handler) c.newInstance();
                reqHandlers.addHandler(h);
            }
        } catch (Exception e) {
            log.error(e);
            return null;
        }
        if (hasReqHandlers)
            return reqHandlers;
        else
            return null;
    }


    private static SimpleChain getRequestChain() {
        ArrayList arr = PropertyLoader.getRequestHandlerNames();
        return getHandlerChain(arr);
    }


    private static SimpleChain getResponseChain() {

        ArrayList arr = PropertyLoader.getResponseHandlerNames();
        return getHandlerChain(arr);
    }

    private static SimpleChain getListenerRequestChain() {

        ArrayList arr = PropertyLoader.getListenerRequestHandlerNames();
        return getHandlerChain(arr);
    }

    private static SimpleChain getListenerResponseChain() {

        ArrayList arr = PropertyLoader.getListenerResponseHandlerNames();
        return getHandlerChain(arr);
    }


    public String getToUrl(Call call) {
        return (String) call.getProperty(Constants.ClientProperties.TO);
    }

    public void setToUrl(Call call, String toUrl) {
        call.setProperty(Constants.ClientProperties.TO, toUrl);
    }

    public String getSourceUrl(Call call) {
        return (String) call.getProperty(Constants.ClientProperties.SOURCE_URL);
    }

    public void setSourceUrl(Call call, String toUrl) {
        call.setProperty(Constants.ClientProperties.SOURCE_URL, toUrl);
    }

    public String getFaultTo(Call call) {
        return (String) call.getProperty(Constants.ClientProperties.FAULT_TO);
    }

    public void setFaultToUrl(Call call, String faultTo) {
        call.setProperty(Constants.ClientProperties.FAULT_TO, faultTo);
    }

    public String getFromUrl(Call call) {
        return (String) call.getProperty(Constants.ClientProperties.FROM);
    }

    public void setFromUrl(Call call, String fromUrl) {
        call.setProperty(Constants.ClientProperties.FROM, fromUrl);
    }

    public String getReplyToUrl(Call call) {
        return (String) call.getProperty(Constants.ClientProperties.REPLY_TO);
    }

    public void setReplyToUrl(Call call, String replyToUrl) {
        call.setProperty(Constants.ClientProperties.REPLY_TO, replyToUrl);
    }

    public String getAcksToUrl(Call call) {
        return (String) call.getProperty(Constants.ClientProperties.ACKS_TO);
    }

    public void setAcksToUrl(Call call, String acksToUrl) {
        call.setProperty(Constants.ClientProperties.ACKS_TO, acksToUrl);
    }

    public boolean isSendOffer(Call call) {
        return ((Boolean) call.getProperty(Constants.ClientProperties.SEND_OFFER)).booleanValue();
    }

    public void setSendOffer(Call call) {
        call.setProperty(Constants.ClientProperties.SEND_OFFER, new Boolean(true));
    }

    public void setLastMessage(Call call) {
        call.setProperty(Constants.ClientProperties.LAST_MESSAGE, new Boolean(true));
    }

    public boolean isLastMessage(Call call) {
        return ((Boolean) call.getProperty(Constants.ClientProperties.LAST_MESSAGE)).booleanValue();
    }

    public long getMessageNumber(Call call) {
        return ((Long) call.getProperty(Constants.ClientProperties.MSG_NUMBER)).longValue();
    }

    public void setMessageNumber(Call call, long msgNumber) {
        call.setProperty(Constants.ClientProperties.MSG_NUMBER, new Long(msgNumber));
    }

    public void setSynchronous(Call call) {
        call.setProperty(Constants.ClientProperties.SYNC, new Boolean(true));
    }

    public boolean getSynchronous(Call call) {
        return ((Boolean) call.getProperty(Constants.ClientProperties.SYNC)).booleanValue();
    }

}
