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
package org.apache.sandesha2.util;

import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.StringTokenizer;

import org.apache.axis2.context.MessageContext;
import org.apache.axis2.om.impl.MIMEOutputUtils;
import org.apache.sandesha2.wsrm.AcknowledgementRange;

/**
 * @author chamikara
 * @author sanka
 */

public class SandeshaUtil {

	private static Hashtable storedMsgContexts = new Hashtable ();
	
	public static String getUUID () {
		String newSequenceID = "uuid:" + MIMEOutputUtils.getRandomStringOf18Characters();
		return newSequenceID;
	}
	
	public static AcknowledgementRange[] getAckRangeArray (String msgNoStr) {
		String[] msgNoStrs = msgNoStr.split(",");
		long[] msgNos = getLongArr (msgNoStrs);
		
		long[] sortedMsgNos = sort (msgNos);
		
		int length = sortedMsgNos.length;
		if (length==0)
			return null;
		//for (int i=0;i<length;i++) 
		//	System.out.println (sortedMsgNos[i]);
		
		ArrayList ackRanges = new ArrayList();
		// upper = 0;
		long lower = sortedMsgNos[0];
		//long max = sortedMsgNos[sortedMsgNos.length];
		long temp = sortedMsgNos[0];
		
		for (long i=1;i<length;i++) {
			int intI = (int) i;
			if ((sortedMsgNos[intI]==(temp+1)) || (sortedMsgNos[intI]==(temp))) {
				temp = sortedMsgNos[intI];
				continue;
			}
			
			
			AcknowledgementRange ackRange = new AcknowledgementRange ();
			ackRange.setLowerValue(lower);
			ackRange.setUpperValue(temp);
			ackRanges.add(ackRange);
			
			lower = sortedMsgNos[intI];
			temp = sortedMsgNos[intI];
			
		}
		
		AcknowledgementRange ackRange = new AcknowledgementRange ();
		ackRange.setLowerValue(lower);
		ackRange.setUpperValue(temp);
		ackRanges.add(ackRange);
		
		Object[] objs = ackRanges.toArray();
		int l = objs.length;
		AcknowledgementRange[] ackRangeArr = new AcknowledgementRange [l];
		for (int i=0;i<l;i++) 
			ackRangeArr[i] = (AcknowledgementRange) objs[i];
		
		return ackRangeArr;
	}
//	TODO remove int from folowing methods. (to make them truly Long :) )
	
	
	private static long[] sort(long[] input) {
		int length = input.length;
		
		long temp = 0;
		for (int i=0;i<length;i++) {
			temp = 0;
			for (int j=i;j<length;j++) {
				if (input[j] < input[i]) {
					//swap
					temp = input[i];
					input[i] = input[j];
					input[j] = temp;
				}
			}
		}
		
		return input;
	}
	
	
	
	private static long[] getLongArr(String[] strings) {
		int length = strings.length;
		long[] longs = new long[length];
		for (int i=0;i<length;i++) {
			longs[i] = Long.parseLong(strings[i]);
		}
		
		return longs;
	}
	
	
	
	public static String storeMessageContext (MessageContext ctx) throws Exception {
		if (ctx==null)
			throw new Exception ("Stored Msg Ctx is null");
		
		String key = getUUID();
		storedMsgContexts.put(key,ctx);
		return key;
	}
	
	public static MessageContext getStoredMessageContext (String key) {
		return (MessageContext) storedMsgContexts.get(key);
	}
	
//	public static void main (String[] args) {
//		String msgList = "13,2,6,4,4,1,999,12,3";
//		getAckRangeArray( msgList);
//		
//	}
}
