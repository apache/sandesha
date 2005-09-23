package org.apache.sandesha2.storage.beans;

import java.io.Serializable;

public class CreateSeqBean implements Serializable {
	private String tempSequenceId;

	private String CreateSeqMsgId;

	private String SequenceId;

	/**
	 * @return Returns the createSeqMsgId.
	 */
	public String getCreateSeqMsgId() {
		return CreateSeqMsgId;
	}

	/**
	 * @param createSeqMsgId
	 *            The createSeqMsgId to set.
	 */
	public void setCreateSeqMsgId(String createSeqMsgId) {
		CreateSeqMsgId = createSeqMsgId;
	}

	/**
	 * @return Returns the sequenceId.
	 */
	public String getSequenceId() {
		return SequenceId;
	}

	/**
	 * @param sequenceId
	 *            The sequenceId to set.
	 */
	public void setSequenceId(String sequenceId) {
		SequenceId = sequenceId;
	}

	public String getTempSequenceId() {
		return tempSequenceId;
	}

	public void setTempSequenceId(String tempSequenceId) {
		this.tempSequenceId = tempSequenceId;
	}

}