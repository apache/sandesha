/*
 * Created on Apr 15, 2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.apache.sandesha.storage;

import org.apache.axis.AxisFault;

import java.util.ArrayList;

/**
 * @author root
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class CallbackData {
	
	private int messageType;
	private String action;
	private long messageNumber;
	private String messageId;
	private String sequenceId;

    public AxisFault getFault() {
        return fault;
    }

    public void setFault(AxisFault fault) {
        this.fault = fault;
    }

	private long ackStart;
	private long ackEnd;
	private ArrayList ackNack;
    private AxisFault fault;



    /**
     * @return Returns the action.
     */
    public String getAction() {
        return action;
    }
    /**
     * @param action The action to set.
     */
    public void setAction(String action) {
        this.action = action;
    }
	public CallbackData () {
		ackNack = new ArrayList ();
	}
	
	private void addToNacks(long nack){
		ackNack.add(new Long(nack));
	}
	
	private ArrayList getNacks(){
		return ackNack;
	}
	
	/**
	 * @return
	 */
	public long getAckEnd() {
		return ackEnd;
	}

	/**
	 * @return
	 */
	public long getAckStart() {
		return ackStart;
	}

	/**
	 * @return
	 */
	public String getMessageId() {
		return messageId;
	}

	/**
	 * @return
	 */
	public long getMessageNumber() {
		return messageNumber;
	}

	/**
	 * @return
	 */
	public int getMessageType() {
		return messageType;
	}

	/**
	 * @return
	 */
	public String getSequenceId() {
		return sequenceId;
	}

	/**
	 * @param l
	 */
	public void setAckEnd(long l) {
		ackEnd = l;
	}

	/**
	 * @param l
	 */
	public void setAckStart(long l) {
		ackStart = l;
	}

	/**
	 * @param string
	 */
	public void setMessageId(String string) {
		messageId = string;
	}

	/**
	 * @param l
	 */
	public void setMessageNumber(long l) {
		messageNumber = l;
	}

	/**
	 * @param i
	 */
	public void setMessageType(int i) {
		messageType = i;
	}

	/**
	 * @param string
	 */
	public void setSequenceId(String string) {
		sequenceId = string;
	}

}
