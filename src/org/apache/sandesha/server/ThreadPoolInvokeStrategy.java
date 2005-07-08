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

import java.util.Collections;
import java.util.Map;

import org.apache.axis.components.logger.LogFactory;
import org.apache.axis.components.threadpool.ThreadPool;
import org.apache.commons.logging.Log;
import org.apache.log4j.spi.LoggerFactory;
import org.apache.sandesha.Constants;

/**
 * Use the Axis thread pool for handling web service invokes.  Implementations 
 * of the <code>RMInvokerWork</code> are wrapped in a <code>Runnable</code>
 * and passed into the pool.
 *
 * @author Patrick Collins
 */
public class ThreadPoolInvokeStrategy implements InvokeStrategy {
   
   private static final Log log = LogFactory.getLog(ThreadPoolInvokeStrategy.class.getName());

   private Map params = Collections.EMPTY_MAP;
   private ThreadPool tPool;
   
   /**
    * Creates the axis thread pool.
    * @see org.apache.sandesha.server.InvokeStrategy#start()
    */
   public void start() {
      final int numberOfThreads = getThreadPoolSize();

      tPool = new ThreadPool(numberOfThreads);
      for (int i = 0; i < numberOfThreads; i++) {
         RMRunnableInvoker rmWorker = new RMRunnableInvoker( new RMInvokerWork() );
          tPool.addWorker(rmWorker);
      }
   }
   
   /**
    * @see org.apache.sandesha.server.InvokeStrategy#stop()
    */
   public void stop() {
      tPool.shutdown();
   }

   /**
    * Determine the size of the thread pool.  Defaults to value defined
    * in <code>Constants.INVOKER_THREADS</code> if none was explicitly set
    * via config.
    */
   protected int getThreadPoolSize() {
      int threadSize = Constants.INVOKER_THREADS;
      String value = (String) getParams().get( Constants.THREAD_POOL_SIZE );

      if( value != null && value.length() > 0 ) {
         try {
            threadSize = Integer.parseInt(value);
         } catch( NumberFormatException nfe ) {
            // eat it
         }
      }
      return threadSize;
   }
   
   /**
    * @see org.apache.sandesha.server.InvokeStrategy#addParams(java.util.Map)
    */
   public void addParams(Map aParams) {
      params = aParams;
   }
   
   protected Map getParams() {
      return params;
   }
   
   /**
    * A <code>Runnable</code> wrapper for embedding <code>RMInvokerWork</code>
    * objects in their own threads. 
    */
   protected class RMRunnableInvoker implements Runnable
   {
      private RMInvokerWork rmInvokerWork;
      
       public RMRunnableInvoker( RMInvokerWork aRMInvokerWork ) {
          rmInvokerWork = aRMInvokerWork;
       }

       public void run() {
           while (true) {
               try {
                   Thread.sleep(Constants.RMINVOKER_SLEEP_TIME);
                   getRMInvokerWorker().executeInvoke();
               } catch( InterruptedException ex ) {
                  log.error( ex );
               } catch( Exception e ) {
                  e.printStackTrace();
                  log.error( e );
               }
           }
       }
       
      /**
       * @return Returns the rmInvokerWork.
       */
      protected RMInvokerWork getRMInvokerWorker() {
         return rmInvokerWork;
      }
   }
}
