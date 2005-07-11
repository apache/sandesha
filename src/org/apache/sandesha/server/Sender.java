/*
 * Copyright  1999-2004 The Apache Software Foundation.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.apache.sandesha.server;

import org.apache.axis.SimpleChain;
import org.apache.axis.components.threadpool.ThreadPool;
import org.apache.sandesha.Constants;
import org.apache.sandesha.IStorageManager;
import org.apache.sandesha.storage.Callback;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * This is the sender for Sandesha for both the client and the server sides. Starting of the
 * Sender will be done either by the RMProvider or the SandeshaContext. The job of the sender is to
 * keep on monitoring the SandeshaQueue and send any messages that are scheduled to be sent.
 *
 * @author Chamikar Jayalath
 * @author Jaliya Ekanayake
 */
public class Sender {

    private ThreadPool tPool = new ThreadPool(Constants.SENDER_THREADS);
    private ArrayList threadList = new ArrayList();

    public void startSender() {
        running = true;
        for (int i = 0; i < Constants.SENDER_THREADS; i++) {
            SenderWorker senderWorker = new SenderWorker(this.storageManager);
            senderWorker.setRequestChain(this.getRequestChain());
            senderWorker.setResponseChain(this.getResponseChain());
            senderWorker.setRunning(true);
            SenderWorker.setCallback(callback);
            threadList.add(senderWorker);
            tPool.addWorker(senderWorker);
        }
    }

    public void stop() {
        Iterator ite = threadList.iterator();
        while (ite.hasNext()) {
            SenderWorker sWorker = (SenderWorker) ite.next();
            sWorker.setRunning(false);
        }

        tPool.safeShutdown();
        running = false;
    }

    public static Callback callback;

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    private boolean running;
    private IStorageManager storageManager;


    public static synchronized Callback getCallback() {
        return callback;
    }

    public static synchronized void setCallback(Callback cb) {
        callback = cb;
    }

    private SimpleChain requestChain = null;
    private SimpleChain responseChain = null;

    public SimpleChain getRequestChain() {
        return requestChain;
    }

    public void setRequestChain(SimpleChain requestChain) {
        this.requestChain = requestChain;
    }

    public SimpleChain getResponseChain() {
        return responseChain;
    }

    public void setResponseChain(SimpleChain responseChanin) {
        this.responseChain = responseChanin;
    }

    public Sender() {
        storageManager = new ServerStorageManager();
    }

    public Sender(IStorageManager storageManager) {
        this.storageManager = storageManager;
    }
}