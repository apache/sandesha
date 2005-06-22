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
import java.io.IOException;

/**
 * SandeshaContext will keep track of different Call objects that the user may use inside
 * a single client instance. SandeshaContext provides the user with an API to initialize and
 * end sequences. With the "endSequence(Call call) method the user is provide with the option
 * of accepting a RMReport which contains the overall status of the message transfer.
 */
public class SandeshaContext {

    private static final Log log = LogFactory.getLog(SandeshaContext.class.getName());

    private static boolean rmInvokerStarted = false;
    private static boolean cleintSenderStarted = false;
    private static boolean serverSenderStarted = false;
    private static boolean listenerStarted = false;
    private static SimpleAxisServer sas = null;
    private static Sender cleintSender;
    private static Sender serverSender;
    private static boolean insideServer;


    private HashMap callMap = new HashMap();
    private static HashMap seqMap = new HashMap();

    private long key;

    private String toURL;
    private String sourceURL;
    private String replyToURL;

    public String getReplyToURL() {
        return replyToURL;
    }

    public void setReplyToURL(String replyToURL) {
        this.replyToURL = replyToURL;
    }

    private String faultToURL;
    private String fromURL;
    private String acksToURL;
    private boolean sendOffer;
    // private boolean lastMessage;
    private long messageNumber;

    private RMReport report;

    private boolean sync;

    public boolean isSync() {
        return sync;
    }

    public void setSync(boolean sync) {
        this.sync = sync;
    }

    public long getMessageNumber() {
        return messageNumber;
    }

    public void setMessageNumber(long messageNumber) {
        this.messageNumber = messageNumber;
    }

    public boolean isSendOffer() {
        return sendOffer;
    }

    public void setSendOffer(boolean sendOffer) {
        this.sendOffer = sendOffer;
    }

    public String getAcksToURL() {
        return acksToURL;
    }

    public void setAcksToURL(String acksToURL) {
        this.acksToURL = acksToURL;
    }

    public String getFromURL() {
        return fromURL;
    }

    public void setFromURL(String fromURL) {
        this.fromURL = fromURL;
    }

    public String getFaultURL() {
        return faultToURL;
    }

    public void setFaultToURL(String faultURL) {
        this.faultToURL = faultURL;
    }

    public String getSourceURL() {
        return sourceURL;
    }

    public void setSourceURL(String sourceURL) {
        this.sourceURL = sourceURL;
    }

    public String getToURL() {
        return toURL;
    }

    public void setToURL(String toURL) {
        this.toURL = toURL;
    }

    public SandeshaContext() throws AxisFault {
        messageNumber = 0;
        key = System.currentTimeMillis();
        SandeshaContext.insideServer = false;
        init(true);
        startListener();
        seqMap.put(new Long(key), this);
        report = new RMReport();
    }

    public SandeshaContext(int sync) throws AxisFault {
        this.sync = true;
        messageNumber = 0;
        key = System.currentTimeMillis();
        SandeshaContext.insideServer = false;
        init(true);
        seqMap.put(new Long(key), this);
        report = new RMReport();
    }

    public SandeshaContext(boolean insideServer) throws AxisFault {
        messageNumber = 0;
        key = System.currentTimeMillis();
        SandeshaContext.insideServer = insideServer;
        init(true);
        seqMap.put(new Long(key), this);
        report = new RMReport();
    }

    public SandeshaContext(boolean insideServer, int sync) throws AxisFault {
        this.sync = true;
        messageNumber = 0;
        key = System.currentTimeMillis();
        SandeshaContext.insideServer = insideServer;
        init(true);
        seqMap.put(new Long(key), this);
        report = new RMReport();
    }

    public void initCall(Call call, String targetUrl, String action, short MEP) throws AxisFault {
        if (toURL != null)
            call.setProperty(Constants.ClientProperties.TO, toURL);
        if (sourceURL != null)
            call.setProperty(Constants.ClientProperties.SOURCE_URL, sourceURL);
        if (faultToURL != null)
            call.setProperty(Constants.ClientProperties.FAULT_TO, faultToURL);
        if (fromURL != null)
            call.setProperty(Constants.ClientProperties.FROM, fromURL);
        if (replyToURL != null)
            call.setProperty(Constants.ClientProperties.REPLY_TO, replyToURL);
        if (acksToURL != null)
            call.setProperty(Constants.ClientProperties.ACKS_TO, acksToURL);

        call.setProperty(Constants.ClientProperties.SEND_OFFER, Boolean.valueOf(sendOffer));
        call.setProperty(Constants.ClientProperties.SYNC, Boolean.valueOf(sync));
        call.setProperty("context", this);

        String key = initialize(call, targetUrl, action, MEP);
        callMap.put(key, call);
    }

    public HashMap getCallMap() {
        return callMap;
    }

    public void setCallMap(HashMap callMap) {
        this.callMap = callMap;
    }

    public static IStorageManager init(boolean client) throws AxisFault {
        if (client) {
            IStorageManager storageManager = new ClientStorageManager();
            if (!cleintSenderStarted) {
                log.info(Constants.InfomationMessage.SENDER_STARTED);
                cleintSender = new Sender(storageManager);
                SimpleChain reqChain = null;
                SimpleChain resChain = null;
                try {
                    reqChain = getRequestChain();
                    resChain = getResponseChain();
                } catch (Exception e) {
                    throw new AxisFault(e.getMessage());
                }
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
                log.info(Constants.InfomationMessage.SENDER_STARTED);
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

    private void validateProperties(Call call, String targetUrl, String action, short MEP)
            throws AxisFault {
        if (action == null)
            throw new AxisFault("Please sepeicfy Action");
        if (targetUrl == null)
            throw new AxisFault("TargetUrl cannot be null");
        if (call == null)
            throw new AxisFault("Call cannot be null");
        if (!(MEP == Constants.ClientProperties.IN_ONLY || MEP == Constants.ClientProperties.IN_OUT))
            throw new AxisFault("Invalid MEP");
    }

    public RMReport endSequence() throws AxisFault {

        IStorageManager storageManager = new ClientStorageManager();
        long startingTime = System.currentTimeMillis();
        long inactivityTimeOut = PolicyLoader.getInstance().getInactivityTimeout();

        Iterator ite = callMap.keySet().iterator();

        while (ite.hasNext()) {
            String key = (String) ite.next();
            Call tempCall = (Call) callMap.get(key);
            String seqId = (String) tempCall.getProperty(Constants.ClientProperties.CALL_KEY);
            while (!storageManager.isSequenceComplete(seqId)) {
                try {
                    log.info(Constants.InfomationMessage.WAITING_TO_STOP_CLIENT);
                    Thread.sleep(Constants.CLIENT_WAIT_PERIOD_FOR_COMPLETE);
                    if ((System.currentTimeMillis() - startingTime) >= inactivityTimeOut) {
                        stopClientByForce();
                        this.report.setError("Inactivity Time Out Reached. Sequence not complete");
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    log.error(e);
                }
            }
        }

        if (this.report.getError() == null) {
            this.report.setAllAcked(true);
        }

        seqMap.remove(new Long(key));
        if (seqMap.isEmpty()) {
            if (listenerStarted) {
                sas.stop();
                listenerStarted = false;
            }
            cleintSender.stop();
            cleintSenderStarted = false;
            storageManager.clearStorage();
        }

        return this.report;

    }


    public void stopClientByForce() throws AxisFault {
        if (listenerStarted) {
            sas.stop();
            listenerStarted = false;
        }
        cleintSender.stop();
        cleintSenderStarted = false;
        throw new AxisFault("Inactivity Timeout Reached, No Response from the Server");
    }

    private String initialize(Call call, String targetUrl, String action, short MEP)
            throws AxisFault {
        validateProperties(call, targetUrl, action, MEP);
        String keyOfCall = this.key + action;
        call.setTargetEndpointAddress(targetUrl);
        call.setProperty(Constants.ClientProperties.ACTION, action);
        call.setTransport(new RMTransport(targetUrl, ""));
        call.setProperty(Constants.ClientProperties.MEP, new Short(MEP));
        call.setProperty(Constants.ClientProperties.CALL_KEY, keyOfCall);
        call.setProperty(Constants.ClientProperties.REPORT, this.report);

        if (!insideServer) {
            InetAddress addr = null;
            try {
                addr = InetAddress.getLocalHost();
            } catch (UnknownHostException e) {
                log.error(e);
            }

            String sourceURL = null;
            try {
                sourceURL = Constants.HTTP + Constants.COLON + Constants.SLASH +
                        Constants.SLASH + addr.getHostAddress() + Constants.COLON +
                        PropertyLoader.getClientSideListenerPort() + Constants.URL_RM_SERVICE;
            } catch (Exception e) {
                throw new AxisFault(e.getMessage());
            }

            call.setProperty(Constants.ClientProperties.SOURCE_URL, sourceURL);
        }
        return keyOfCall;
    }


    private static void startListener() {
        if (!insideServer) {
            if (!listenerStarted) {
                listenerStarted = true;
                try {
                    log.info(Constants.InfomationMessage.CLIENT_LISTENER_STARTED);
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
                    sas.setServerSocket(new ServerSocket(PropertyLoader.getClientSideListenerPort()));

                    Thread serverThread = new Thread(sas);
                    serverThread.start();

                } catch (Exception e) {
                    log.error(e);
                }
            }
        }

    }

    private static SimpleChain getHandlerChain(List arr) {
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


    private static SimpleChain getRequestChain() throws Exception {
        ArrayList arr = PropertyLoader.getRequestHandlerNames();
        return getHandlerChain(arr);
    }


    private static SimpleChain getResponseChain() throws Exception {

        ArrayList arr = PropertyLoader.getResponseHandlerNames();
        return getHandlerChain(arr);
    }

    private static SimpleChain getListenerRequestChain() throws Exception {

        ArrayList arr = PropertyLoader.getListenerRequestHandlerNames();
        return getHandlerChain(arr);
    }

    private static SimpleChain getListenerResponseChain() throws Exception {

        ArrayList arr = PropertyLoader.getListenerResponseHandlerNames();
        return getHandlerChain(arr);
    }


    public void setLastMessage(Call call) {
        call.setProperty(Constants.ClientProperties.LAST_MESSAGE, Boolean.valueOf(true));
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
}
