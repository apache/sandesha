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

package sandesha2.samples.interop;

import java.util.HashMap;
import java.util.Map;

import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMFactory;
import org.apache.axis2.om.OMNamespace;

public class RMInteropService {

	private static Map sequences = new HashMap();

	public OMElement echoString(OMElement in) {

		String responseText = null;
		if (in != null) {
			String tempText = in.getText();
			if (tempText == null || "".equals(tempText)) {
				OMElement firstChild = in.getFirstElement();
				if (firstChild != null)
					tempText = firstChild.getText();
			}

			if (tempText != null)
				responseText = tempText;
		}

		System.out.println("echoString got text:"
				+ ((null == responseText) ? "" : responseText));
		OMFactory fac = OMAbstractFactory.getOMFactory();
		OMNamespace omNs = fac.createOMNamespace("http://tempuri.org/",
				"echoString");
		OMElement method = fac.createOMElement("echoStringResponse", omNs);

		OMElement value = fac.createOMElement("text", omNs);

		if (responseText == null || "".equals(responseText))
			responseText = "echo response";

		value.setText(responseText);
		method.addChild(value);

		return method;
	}

	public void ping(OMElement in) {
		//Just accept the message and do some processing

		String text = null;
		if (in != null) {
			OMElement firstElement = in.getFirstElement();
			if (firstElement != null) {
				text = firstElement.getText();
			}
		}

		text = (text == null) ? "" : text;

		System.out.println("Ping got text:" + text);
	}

	public OMElement EchoString(OMElement in) {

		String responseText = null;
		if (in != null) {
			String tempText = in.getText();
			if (tempText == null || "".equals(tempText)) {
				OMElement firstChild = in.getFirstElement();
				if (firstChild != null)
					tempText = firstChild.getText();
			}

			if (tempText != null)
				responseText = tempText;
		}

		System.out.println("echoString got text:"
				+ ((null == responseText) ? "" : responseText));
		OMFactory fac = OMAbstractFactory.getOMFactory();
		OMNamespace omNs = fac.createOMNamespace("http://tempuri.org/",
				"echoString");
		OMElement method = fac.createOMElement("echoStringResponse", omNs);

		OMElement value = fac.createOMElement("text", omNs);

		if (responseText == null || "".equals(responseText))
			responseText = "echo response";

		value.setText(responseText);
		method.addChild(value);

		return method;
	}

	public void Ping(OMElement in) {
		//Just accept the message and do some processing

		String text = null;
		if (in != null) {
			OMElement firstElement = in.getFirstElement();
			if (firstElement != null) {
				text = firstElement.getText();
			}
		}

		text = (text == null) ? "" : text;

		System.out.println("Ping got text:" + text);
	}

}