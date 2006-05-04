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
	
	private boolean acknowledgementIntervalSet = false;
	private boolean exponentialBackoffSet = false;
	private boolean inactivityTimeoutSet = false;
	private boolean inactivityTimeoutMeassureSet = false;
	private boolean invokeInOrderSet = false;
	private boolean messageTypesToDropSet = false;
	private boolean retransmissionIntervalSet = false;
	private boolean permanentStorageMgrSet = false;
	private boolean inmemoryStorageManagerSet = false;	
	private boolean storageManagerSet = false;
	private boolean maximumRetransmissionCountSet = false;

	public boolean isExponentialBackoff() {
		return exponentialBackoff;
	}

	public void setExponentialBackoff(boolean exponentialBackoff) {
		this.exponentialBackoff = exponentialBackoff;
		setExponentialBackoffSet(true);
	}

	public long getInactivityTimeout() {
		return inactivityTimeout;
	}

	public void setInactivityTimeout(long inactivityTimeout) {
		this.inactivityTimeout = inactivityTimeout;
		setInactivityTimeoutSet(true);
	}

	public String getInactivityTimeoutMeassure() {
		return inactivityTimeoutMeassure;
	}

	public void setInactivityTimeoutMeassure(String inactivityTimeoutMeassure) {
		this.inactivityTimeoutMeassure = inactivityTimeoutMeassure;
		setInactivityTimeoutMeassureSet(true);
	}

	public boolean isInvokeInOrder() {
		return invokeInOrder;
	}

	public void setInvokeInOrder(boolean invokeInOrder) {
		this.invokeInOrder = invokeInOrder;
		setInvokeInOrderSet (true);
	}

	public String getMessageTypesToDrop() {
		return messageTypesToDrop;
	}

	public void setMessageTypesToDrop(String messageTypesToDrop) {
		this.messageTypesToDrop = messageTypesToDrop;
		setMessageTypesToDropSet(true);
	}

	public long getRetransmissionInterval() {
		return retransmissionInterval;
	}

	public void setRetransmissionInterval(long retransmissionInterval) {
		this.retransmissionInterval = retransmissionInterval;
		setRetransmissionIntervalSet(true);
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
		
		if (isAcknowledgementIntervalSet())
			ped.setAcknowledgementInterval(this.getAcknowledgementInterval());
		
		if (isExponentialBackoffSet())
			ped.setExponentialBackoff(this.isExponentialBackoff());
		
		if (isInactivityTimeoutSet())
			ped.setInactivityTimeout(this.getInactivityTimeout());
		
		if (isInactivityTimeoutMeassureSet())
			ped.setInactivityTimeoutMeassure(this.getInactivityTimeoutMeassure());
		
		if (isInvokeInOrderSet())
		    ped.setInvokeInOrder(this.isInvokeInOrder());
		
		if (isMessageTypesToDropSet())
			ped.setMessageTypesToDrop(this.getMessageTypesToDrop());
		
		if (isRetransmissionIntervalSet())
			ped.setRetransmissionInterval(this.getRetransmissionInterval());
		
		//ped.setPermanentStorageManager(this.getPermanentStorageManager());
		
		if (isStorageManagerSet())
			ped.setStorageManager(this.getStorageManager());
		
		if (isMaximumRetransmissionCountSet())
			ped.setMaximumRetransmissionCount(this.getMaximumRetransmissionCount());
		
		return ped;
	}

	public void setAcknowledgementInterval(long acknowledgementInterval) {
		this.acknowledgementInterval = acknowledgementInterval;
		setAcknowledgementIntervalSet(true);
	}
	
	public long getAcknowledgementInterval() {
		return acknowledgementInterval;
	}
	
	public void setStorageManager(String storageManager) {
		this.storageManager = storageManager;
		setStorageManagerSet(true);
	}
	
	public String getStorageManager() {
		return storageManager;
	}

	public int getMaximumRetransmissionCount() {
		return maximumRetransmissionCount;
	}

	public void setMaximumRetransmissionCount(int maximumRetransmissionCount) {
		this.maximumRetransmissionCount = maximumRetransmissionCount;
		setMaximumRetransmissionCountSet(true);
	}

	public boolean isAcknowledgementIntervalSet() {
		return acknowledgementIntervalSet;
	}

	public boolean isExponentialBackoffSet() {
		return exponentialBackoffSet;
	}

	public boolean isInactivityTimeoutMeassureSet() {
		return inactivityTimeoutMeassureSet;
	}

	public boolean isInactivityTimeoutSet() {
		return inactivityTimeoutSet;
	}

	public String getInmemoryStorageManager() {
		return inmemoryStorageManager;
	}

	public boolean isInmemoryStorageManagerSet() {
		return inmemoryStorageManagerSet;
	}

	public boolean isInvokeInOrderSet() {
		return invokeInOrderSet;
	}

	public boolean isMaximumRetransmissionCountSet() {
		return maximumRetransmissionCountSet;
	}

	public boolean isMessageTypesToDropSet() {
		return messageTypesToDropSet;
	}

	public String getPermanentStorageMgr() {
		return permanentStorageMgr;
	}

	public boolean isPermanentStorageMgrSet() {
		return permanentStorageMgrSet;
	}

	public boolean isRetransmissionIntervalSet() {
		return retransmissionIntervalSet;
	}

	public boolean isStorageManagerSet() {
		return storageManagerSet;
	}

	private void setAcknowledgementIntervalSet(boolean acknowledgementIntervalSet) {
		this.acknowledgementIntervalSet = acknowledgementIntervalSet;
	}

	private void setExponentialBackoffSet(boolean exponentialBackoffSet) {
		this.exponentialBackoffSet = exponentialBackoffSet;
	}

	private void setInactivityTimeoutMeassureSet(boolean inactivityTimeoutMeassureSet) {
		this.inactivityTimeoutMeassureSet = inactivityTimeoutMeassureSet;
	}

	private void setInactivityTimeoutSet(boolean inactivityTimeoutSet) {
		this.inactivityTimeoutSet = inactivityTimeoutSet;
	}

	public void setInmemoryStorageManager(String inmemoryStorageManager) {
		this.inmemoryStorageManager = inmemoryStorageManager;
		setInmemoryStorageManagerSet(true);
	}

	private void setInmemoryStorageManagerSet(boolean inmemoryStorageManagerSet) {
		this.inmemoryStorageManagerSet = inmemoryStorageManagerSet;
	}

	private void setInvokeInOrderSet(boolean invokeInOrderSet) {
		this.invokeInOrderSet = invokeInOrderSet;
	}

	public void setMaximumRetransmissionCountSet(boolean maximumRetransmissionCountSet) {
		this.maximumRetransmissionCountSet = maximumRetransmissionCountSet;
	}

	private void setMessageTypesToDropSet(boolean messageTypesToDropSet) {
		this.messageTypesToDropSet = messageTypesToDropSet;
	}

	public void setPermanentStorageMgr(String permanentStorageMgr) {
		this.permanentStorageMgr = permanentStorageMgr;
		setPermanentStorageMgrSet(true);
	}

	private void setPermanentStorageMgrSet(boolean permanentStorageMgrSet) {
		this.permanentStorageMgrSet = permanentStorageMgrSet;
	}

	private void setRetransmissionIntervalSet(boolean retransmissionIntervalSet) {
		this.retransmissionIntervalSet = retransmissionIntervalSet;
	}

	private void setStorageManagerSet(boolean storageManagerSet) {
		this.storageManagerSet = storageManagerSet;
	}
}
