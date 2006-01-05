/*
 * Copyright 2004,2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.apache.sandesha2.client;

import java.util.ArrayList;

/**
 * @author Chamikara Jayalath <chamikaramj@gmail.com>
 */

public class SequenceReport {

	public static final byte SEQUENCE_STATUS_ACTIVE = 1;
	public static final byte SEQUENCE_STATUS_TERMINATED = 2;
	public static final byte SEQUENCE_STATUS_TIMEDOUT = 3;
	
	public static final byte SEQUENCE_DIRECTION_IN=1;
	public static final byte SEQUENCE_DIRECTION_OUT=2;
	
	private byte sequenceStatus = SEQUENCE_STATUS_ACTIVE;
	private byte sequenceDirection = SEQUENCE_DIRECTION_OUT;
	private String sequenceID = null;
	private ArrayList ackedMessages = null;
	private boolean sequenceEstablished = false;
	
	public SequenceReport () {
		ackedMessages = new ArrayList ();
	}
	
	public void setSequenceStatus (byte sequenceStatus) {
		if (sequenceStatus>=SEQUENCE_STATUS_ACTIVE && sequenceStatus<=SEQUENCE_STATUS_TIMEDOUT) {
			this.sequenceStatus = sequenceStatus;
		}
	}
	
	public void setSequenceDirection (byte sequenceDirection) {
		if (sequenceDirection>=SEQUENCE_DIRECTION_IN && sequenceDirection<=SEQUENCE_DIRECTION_OUT) {
			this.sequenceDirection = sequenceDirection;
		}
	}
	
	public byte getSequenceStatus () {
		return sequenceStatus;
	}
	
	public byte getSequenceDirection () {
		return sequenceDirection;
	}

	public String getSequenceID() {
		return sequenceID;
	}

	public void setSequenceID(String sequenceID) {
		this.sequenceID = sequenceID;
	}
	
	public ArrayList getAckedMessages () {
		return ackedMessages;
	}

	public boolean isSequenceEstablished() {
		return sequenceEstablished;
	}

	public void setSequenceEstablished(boolean sequenceEstablished) {
		this.sequenceEstablished = sequenceEstablished;
	}
	
	
	public void setAckedMessage (String ackedMessage) {
		ackedMessages.add(ackedMessage);
	}
	
	
}
