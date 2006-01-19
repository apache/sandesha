/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.sandesha2;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.sandesha2.util.PropertyManager;

import junit.framework.TestCase;

/**
 * @author Chamikara Jayalath <chamikaramj@gmail.com>
 */

public class PorpertyLoaderTest extends TestCase {
	
	PropertyManager manager = null;
	
	public void setUp () {
		manager = PropertyManager.getInstance();
		String fileName = "./test-resources/sandesha2.properties";
		File file= new File (fileName);
		if (!file.exists()) {
			fail("'test-resources/sandesha2.properties' not found");
		}
		
		try {
			InputStream in = new FileInputStream (file);
			manager.loadPropertiesFromPropertyFile(in);
			in.close();
		} catch (Exception e) {
			fail (e.getMessage());
		}
		
	}
	
	public void testRetransmissionInterval () {
		long value = manager.getRetransmissionInterval();
		assertEquals(value,20000);
	}
	
	public void testExponentialBackOff () {
		boolean value = manager.isExponentialBackoff();
		assertEquals(value,false);
	}
	
	public void testAcknowledgementInterval () {
		long value = manager.getAcknowledgementInterval();
		assertEquals(value,8000);
	}
	
	public void testInactivityTImeout () {
		long value = manager.getInactivityTimeout();
		assertEquals(value,(60*60*3*1000));
	}
	
	
	public void testStorageManager () {
		String storageMgr = manager.getStorageManagerClass();
		assertEquals(storageMgr,"org.apache.sandesha2.storage.inmemory.InMemoryStorageManager1");
	}
}
