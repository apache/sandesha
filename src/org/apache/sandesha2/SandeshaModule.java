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
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.modules.Module;
import org.apache.sandesha2.storage.StorageManager;
import org.apache.sandesha2.util.SandeshaUtil;

/**
 * The Module class of Sandesha2.
 * 
 * @author Chamikara Jayalath <chamikaramj@gmail.com>
 */

public class SandeshaModule implements Module {


	// initialize the module
	public void init(AxisConfiguration axisSystem) throws AxisFault {
		cleanStorage (axisSystem);
	}

	// shutdown the module
	public void shutdown(AxisConfiguration axisSystem) throws AxisFault {

	}
	
	private void cleanStorage (AxisConfiguration axisSystem) throws AxisFault {
		
		ConfigurationContext configurationContext = new ConfigurationContext (axisSystem);
		StorageManager storageManager = SandeshaUtil.getSandeshaStorageManager(configurationContext);
		
		storageManager.initStorage();
		
	}
	
}