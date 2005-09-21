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

package org.apache.sandesha2;

import org.apache.axis2.AxisFault;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.modules.Module;

/**
 * @author Chamikara
 * @author Sanka
 * @author Jaliya 
 */

public class SandeshaModule implements Module{

	private InOrderInvoker inorderInvoker = new InOrderInvoker ();
	
    // initialize the module
    public void init(AxisConfiguration axisSystem) throws AxisFault {
       
    	//Start invoker thread
    	//axisSystem.get
    	//inorderInvoker.start();
    	
    }

    // shutdown the module
    public void shutdown(AxisConfiguration axisSystem) throws AxisFault {
    	//inorderInvoker.stopWork();
    }
}
