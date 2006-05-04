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
import java.io.InputStream;

import junit.framework.TestCase;

import org.apache.sandesha2.util.PropertyManager;
import org.apache.sandesha2.util.SandeshaPropertyBean;

/**
 * @author Chamikara Jayalath <chamikaramj@gmail.com>
 */

public class PorpertyLoaderTest extends TestCase {
	
	SandeshaPropertyBean propertyBean = null;
	
	public void setUp () {
		String fileName = "./test-resources/sandesha2.properties";
		File file= new File (fileName);
		if (!file.exists()) {
			fail("'test-resources/sandesha2.properties' not found");
		}
		
		try {
			InputStream in = new FileInputStream (file);
			propertyBean = PropertyManager.loadPropertiesFromPropertyFile(in);
			in.close();
		} catch (Exception e) {
			fail (e.getMessage());
		}
		
	}
	
	public void testRetransmissionInterval () {
		long value = propertyBean.getRetransmissionInterval();
		assertEquals(value,20000);
	}
	
	public void testExponentialBackOff () {
		boolean value = propertyBean.isExponentialBackoff();
		assertEquals(value,false);
	}
	
	public void testAcknowledgementInterval () {
		long value = propertyBean.getAcknowledgementInaterval();
		assertEquals(value,8000);
	}
	
	public void testInactivityTImeout () {
		long value = propertyBean.getInactiveTimeoutInterval();
		assertEquals(value,(60*60*3*1000));
	}
	
	public void testStorageManager () {
		String storageMgr = propertyBean.getStorageManagerClass();
		assertEquals(storageMgr,"org.apache.sandesha2.storage.inmemory.InMemoryStorageManager1");
	}
}
