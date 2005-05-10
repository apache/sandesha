package org.apache.sandesha.samples.interop.testclient;

import java.io.Serializable;


public class InteropBean implements Serializable {
	
	private String target;
	private String operation;
	private String from;
	private String replyto;
	private int noOfMsgs;
	private String terminate;
	private String acks;
	
	
	

	/**
	 * @return
	 */
	public String getAcks() {
		return acks;
	}

	/**
	 * @return
	 */
	public String getFrom() {
		return from;
	}

	/**
	 * @return
	 */
	public int getNoOfMsgs() {
		return noOfMsgs;
	}

	/**
	 * @return
	 */
	public String getOperation() {
		return operation;
	}

	/**
	 * @return
	 */
	public String getReplyto() {
		return replyto;
	}

	/**
	 * @return
	 */
	public String getTarget() {
		return target;
	}

	/**
	 * @return
	 */
	public String getTerminate() {
		return terminate;
	}

	/**
	 * @param string
	 */
	public void setAcks(String string) {
		acks = string;
	}

	/**
	 * @param string
	 */
	public void setFrom(String string) {
		from = string;
	}

	/**
	 * @param i
	 */
	public void setNoOfMsgs(int i) {
		noOfMsgs = i;
	}

	/**
	 * @param string
	 */
	public void setOperation(String string) {
		operation = string;
	}

	/**
	 * @param string
	 */
	public void setReplyto(String string) {
		replyto = string;
	}

	/**
	 * @param string
	 */
	public void setTarget(String string) {
		target = string;
	}

	/**
	 * @param string
	 */
	public void setTerminate(String string) {
		terminate = string;
	}

}
