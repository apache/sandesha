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
package org.apache.sandesha.client;

import java.util.Iterator;
import java.util.List;

import org.apache.axis.Handler;
import org.apache.axis.SimpleChain;
import org.apache.axis.components.logger.LogFactory;
import org.apache.commons.logging.Log;

/**
 * This class is used to get a handler chain from a given
 * array of handler names.
 *
 * @author Patrick Collins
 */
public class ClientHandlerUtil
{
   
   private static final Log log = LogFactory.getLog(ClientHandlerUtil.class.getName());
   
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

}
