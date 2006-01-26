package org.apache.sandesha2.policy.processors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.sandesha2.policy.PolicyEngineData;
import org.apache.sandesha2.policy.RMPolicy;
import org.apache.sandesha2.policy.RMPolicyToken;
import org.apache.sandesha2.policy.RMProcessorContext;

public class StorageManagersProcessor {
	private boolean initializedStorageManager = false;

	private Log logger = LogFactory.getLog(this.getClass().getName());

	public void initializeStorageManager(RMPolicyToken rmpt)
			throws NoSuchMethodException {
		logger.debug("StorageManagersProcessor:initializeStorageManager");

		RMPolicyToken tmpRpt = RMPolicy.storageManager.copy();
		tmpRpt.setProcessTokenMethod(this);
		rmpt.setChildToken(tmpRpt);

		tmpRpt = RMPolicy.permenentStorageManager.copy();
		tmpRpt.setProcessTokenMethod(this);
		rmpt.setChildToken(tmpRpt);

	}

	public Object doStorageManagers(RMProcessorContext rmpc) {
		logger.debug("Processing "
				+ rmpc.readCurrentSecurityToken().getTokenName() + ": "
				+ RMProcessorContext.ACTION_NAMES[rmpc.getAction()]);

		RMPolicyToken rmpt = rmpc.readCurrentSecurityToken();
		switch (rmpc.getAction()) {

		case RMProcessorContext.START:
			if (!initializedStorageManager) {
				try {
					initializeStorageManager(rmpt);
					initializedStorageManager = true;
				} catch (NoSuchMethodException e) {
					logger.error(
							"Exception occured in initializeStorageManager", e);
					return new Boolean(false);
				}
			}
			logger.debug(rmpt.getTokenName());

		case RMProcessorContext.COMMIT:
			break;
		case RMProcessorContext.ABORT:
			break;
		}
		return new Boolean(true);
	}

	public Object doStorageManager(RMProcessorContext rmpc) {
		logger.debug("Processing "
				+ rmpc.readCurrentSecurityToken().getTokenName() + ": "
				+ RMProcessorContext.ACTION_NAMES[rmpc.getAction()]);

		PolicyEngineData ped = rmpc.readCurrentPolicyEngineData();
		String cls = rmpc.getAssertion().getStrValue();

		if (cls != null && !cls.trim().equals("")) {
			ped.setStorageManager(cls.trim());
		}

		return new Boolean(true);
	}

	public Object doPermanentStorageManager(RMProcessorContext spc) {
		logger.debug("Processing "
				+ spc.readCurrentSecurityToken().getTokenName() + ": "
				+ RMProcessorContext.ACTION_NAMES[spc.getAction()]);

		PolicyEngineData ped = spc.readCurrentPolicyEngineData();
		String cls = spc.getAssertion().getStrValue();

		if (cls != null && !cls.trim().equals("")) {
			ped.setStorageManager(cls.trim());
		}

		return new Boolean(true);
	}
}
