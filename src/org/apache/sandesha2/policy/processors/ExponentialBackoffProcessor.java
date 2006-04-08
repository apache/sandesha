package org.apache.sandesha2.policy.processors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.sandesha2.policy.PolicyEngineData;
import org.apache.sandesha2.policy.RMPolicyToken;
import org.apache.sandesha2.policy.RMProcessorContext;

public class ExponentialBackoffProcessor {

	private boolean initializedExponentialBackoff = false;

	private Log logger = LogFactory.getLog(this.getClass().getName());

	public void initializeExponentialBackoff(RMPolicyToken rmpt)
			throws NoSuchMethodException {

	}

	public Object doExponentialBackoff(RMProcessorContext rmpc) {

		RMPolicyToken rmpt = rmpc.readCurrentSecurityToken();
		switch (rmpc.getAction()) {

		case RMProcessorContext.START:
			if (!initializedExponentialBackoff) {
				try {
					initializeExponentialBackoff(rmpt);
					initializedExponentialBackoff = true;
				} catch (NoSuchMethodException e) {
					logger.error("Exception occured when invoking processTokenMethod", e);
					return new Boolean(false);
				}
			}
			logger.debug(rmpt.getTokenName());

		case RMProcessorContext.COMMIT:

			// ///////

			PolicyEngineData ped = rmpc.readCurrentPolicyEngineData();
			String text = rmpc.getAssertion().getStrValue();
			ped.setExponentialBackoff(new Boolean(text.trim()).booleanValue());

			// ///////

			break;
		case RMProcessorContext.ABORT:
			break;
		}
		return new Boolean(true);
	}
}
