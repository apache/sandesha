package org.apache.sandesha2.policy;


public class RMPolicy {

	public static final RMPolicyToken retransmissionIterval = new RMPolicyToken(
			"RetransmissionInterval", RMPolicyToken.SIMPLE_TOKEN,
			new String[] { "Milliseconds" });

	public static final RMPolicyToken acknowledgementInterval = new RMPolicyToken(
			"AcknowledgementInterval", RMPolicyToken.SIMPLE_TOKEN,
			new String[] {});
	
	public static final RMPolicyToken maximumRetransmissionCount = new RMPolicyToken(
			"MaximumRetransmissionCount", RMPolicyToken.SIMPLE_TOKEN,
			new String[] {});

	public static final RMPolicyToken exponentialBackoff = new RMPolicyToken(
			"ExponentialBackoff", RMPolicyToken.SIMPLE_TOKEN, new String[] {});

	public static final RMPolicyToken inactiveTimeout = new RMPolicyToken(
			"InactivityTimeout", RMPolicyToken.SIMPLE_TOKEN, new String[] {});

	public static final RMPolicyToken inactiveTimeoutMeasure = new RMPolicyToken(
			"InactivityTimeoutMeasure", RMPolicyToken.SIMPLE_TOKEN,
			new String[] {});

	public static final RMPolicyToken invokeInOrder = new RMPolicyToken(
			"InvokeInOrder", RMPolicyToken.SIMPLE_TOKEN, new String[] {});
	
	public static final RMPolicyToken messageTypeToDrop = new RMPolicyToken( 
			"MessageTypesToDrop", RMPolicyToken.SIMPLE_TOKEN, new String[] {});
	
	public static final RMPolicyToken storageManagers = new RMPolicyToken(
			"StorageManagers", RMPolicyToken.COMPLEX_TOKEN, new String[] {});
	
	public static final RMPolicyToken permenentStorageManager = new RMPolicyToken(
			"PermanentStorageManager", RMPolicyToken.SIMPLE_TOKEN, new String[]{});
	
	public static final RMPolicyToken storageManager = new RMPolicyToken(
			"StorageManager", RMPolicyToken.SIMPLE_TOKEN, new String[]{});
	
	public static final String includeNever = "http://schemas.xmlsoap.org/ws/2005/07/securitypolicy/IncludeToken/Never";

	public static final String includeOnce = "http://schemas.xmlsoap.org/ws/2005/07/securitypolicy/IncludeToken/Once";

	public static final String includeAlways = "http://schemas.xmlsoap.org/ws/2005/07/securitypolicy/IncludeToken/Always";

}
