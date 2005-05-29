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

import org.apache.axis.components.threadpool.ThreadPool;
import org.apache.sandesha.Constants;

/**
 * <p/>
 * This class will act as the service dispatcher for Sandesha. RMInvoker uses several RMInvokerWorkers
 * to dispatch the acutal request to the service. Number of RMInvokerWorkers is decided using the
 * constant value RMINVOKER_THREADS in the Constants class.
 */
public class RMInvoker {
    private static boolean invokerStarted = false;

    public void startInvoker() {
        if (!invokerStarted) {
            System.out.println(Constants.InfomationMessage.RMINVOKER_STARTED);
            ThreadPool tPool = new ThreadPool(Constants.INVOKER_THREADS);
            for (int i = 0; i < Constants.INVOKER_THREADS; i++) {
                RMInvokerWorker rmWorker = new RMInvokerWorker();
                tPool.addWorker(rmWorker);
            }
        }
    }


}