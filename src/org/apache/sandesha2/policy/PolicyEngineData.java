package org.apache.sandesha2.policy;

public class PolicyEngineData {
	
	private long acknowledgementInterval = -1;
	private boolean exponentialBackoff = false;
	private long inactivityTimeout = -1;
	private String inactivityTimeoutMeassure = null;
	private boolean invokeInOrder = true;
	private String messageTypesToDrop = null;
	private long retransmissionInterval =  -1;
	private String permanentStorageMgr = null;
	private String inmemoryStorageManager = null;	
	private String storageManager = null;
	private int maximumRetransmissionCount; 

	public boolean isExponentialBackoff() {
		return exponentialBackoff;
	}

	public void setExponentialBackoff(boolean exponentialBackoff) {
		this.exponentialBackoff = exponentialBackoff;
	}

	public long getInactivityTimeout() {
		return inactivityTimeout;
	}

	public void setInactivityTimeout(long inactivityTimeout) {
		this.inactivityTimeout = inactivityTimeout;
	}

	public String getInactivityTimeoutMeassure() {
		return inactivityTimeoutMeassure;
	}

	public void setInactivityTimeoutMeassure(String inactivityTimeoutMeassure) {
		this.inactivityTimeoutMeassure = inactivityTimeoutMeassure;
	}

	public boolean isInvokeInOrder() {
		return invokeInOrder;
	}

	public void setInvokeInOrder(boolean invokeInOrder) {
		this.invokeInOrder = invokeInOrder;
	}

	public String getMessageTypesToDrop() {
		return messageTypesToDrop;
	}

	public void setMessageTypesToDrop(String messageTypesToDrop) {
		this.messageTypesToDrop = messageTypesToDrop;
	}

	public long getRetransmissionInterval() {
		return retransmissionInterval;
	}

	public void setRetransmissionInterval(long retransmissionInterval) {
		this.retransmissionInterval = retransmissionInterval;
	}

//	public String getPermanentStorageManager() {
//		return permanentStorageMgr;
//	}
//
//	public void setPermanentStorageManager(String storageManager) {
//		this.permanentStorageMgr = storageManager;
//	}

	public void initializeWithDefaults() {
		
	}

	public PolicyEngineData copy() {
		PolicyEngineData ped = new PolicyEngineData();
		
		ped.setAcknowledgementInterval(this.getAcknowledgementInterval());
		ped.setExponentialBackoff(this.isExponentialBackoff());
		ped.setInactivityTimeout(this.getInactivityTimeout());
		ped.setInactivityTimeoutMeassure(this.getInactivityTimeoutMeassure());
		ped.setInvokeInOrder(this.isInvokeInOrder());
		ped.setMessageTypesToDrop(this.getMessageTypesToDrop());
		ped.setRetransmissionInterval(this.getRetransmissionInterval());
		//ped.setPermanentStorageManager(this.getPermanentStorageManager());
		ped.setStorageManager(this.getStorageManager());
		ped.setMaximumRetransmissionCount(this.getMaximumRetransmissionCount());
		
		return ped;
	}

	public void setAcknowledgementInterval(long acknowledgementInterval) {
		this.acknowledgementInterval = acknowledgementInterval;
	}
	
	public long getAcknowledgementInterval() {
		return acknowledgementInterval;
	}
	
	public void setStorageManager(String storageManager) {
		this.storageManager = storageManager;
	}
	
	public String getStorageManager() {
		return storageManager;
	}

	public int getMaximumRetransmissionCount() {
		return maximumRetransmissionCount;
	}

	public void setMaximumRetransmissionCount(int maximumRetransmissionCount) {
		this.maximumRetransmissionCount = maximumRetransmissionCount;
	}
}
