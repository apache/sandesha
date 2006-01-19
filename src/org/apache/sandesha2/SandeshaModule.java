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

import javax.xml.namespace.QName;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisDescription;
import org.apache.axis2.description.ModuleDescription;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.modules.Module;
import org.apache.sandesha2.storage.StorageManager;
import org.apache.sandesha2.util.PropertyManager;
import org.apache.sandesha2.util.SandeshaPropertyBean;
import org.apache.sandesha2.util.SandeshaUtil;

/**
 * The Module class of Sandesha2.
 * 
 * @author Chamikara Jayalath <chamikaramj@gmail.com>
 */

public class SandeshaModule implements Module {


	// initialize the module
	public void init(ConfigurationContext configContext, ModuleDescription module) throws AxisFault {
		
		//continueUncompletedSequences (storageManager,configCtx);
		
		//loading properties to property manager.
		PropertyManager.getInstance().loadPropertiesFromModuleDesc(module);

	}
	
	
	public void engageNotify(AxisDescription axisDescription) throws AxisFault {
		
		// TODO add notify logic.
		
	}




	private void continueUncompletedSequences (StorageManager storageManager,ConfigurationContext configCtx) {
		//server side continues
		//SandeshaUtil.startInvokerIfStopped(configCtx);
		
		//server side re-injections
		
		//reinject everything that has been acked within the in-handler but have not been invoked.
		
		
		//client side continues
		//SandeshaUtil.startSenderIfStopped(configCtx);
		
		//client side re-injections
		
		
	}

	// shutdown the module
	public void shutdown(AxisConfiguration axisSystem) throws AxisFault {

	}
	
	//Removing data of uncontinuuable sequences so that the sandesha2 system will not be confused
	private void cleanStorage (StorageManager storageManager) throws AxisFault {
		
		storageManager.initStorage();
		
		//server side cleaning
		
		//cleaning NextMsgData
		//Cleaning InvokerData
		
		
		//client side cleaning
		
		//cleaning RetransmitterData
		//cleaning CreateSequenceData
		
		//cleaning sequence properties
		
	}
	
}