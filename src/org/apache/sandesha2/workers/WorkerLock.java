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

package org.apache.sandesha2.workers;

import java.util.ArrayList;

public class WorkerLock {

	public ArrayList workList = null;
	
	public WorkerLock () {
		workList = new ArrayList ();
	}
	
	public synchronized void addWork (String work) {
		workList.add(work);
	}
	
	public synchronized void removeWork (String work) {
		workList.remove(work);
	}
	
	public synchronized boolean isWorkPresent (String work) {
		return workList.contains(work);
	}

}
