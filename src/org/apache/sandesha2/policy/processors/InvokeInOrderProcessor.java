package org.apache.sandesha2.policy.processors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.sandesha2.policy.PolicyEngineData;
import org.apache.sandesha2.policy.RMPolicyToken;
import org.apache.sandesha2.policy.RMProcessorContext;


public class InvokeInOrderProcessor {
	private boolean initializedInvokeInOrder = false;
	private Log logger = LogFactory.getLog(this.getClass().getName());
	
	public void initializeInvokeInOrder(RMPolicyToken spt)
			throws NoSuchMethodException {
		logger.debug("InvokeInOrderProcessor:initializeInvokeInOrder");
	}

	public Object doInvokeInOrder(RMProcessorContext rmpc) {
		logger.debug("Processing "
				+ rmpc.readCurrentSecurityToken().getTokenName() + ": "
				+ RMProcessorContext.ACTION_NAMES[rmpc.getAction()]);

		RMPolicyToken rmpt = rmpc.readCurrentSecurityToken();
		switch (rmpc.getAction()) {

		case RMProcessorContext.START:
			if (!initializedInvokeInOrder) {
				try {
					initializeInvokeInOrder(rmpt);
					initializedInvokeInOrder = true;
				} catch (NoSuchMethodException e) {
					logger.error("Exception occured in initializeInvokeInOrder", e);
					return new Boolean(false);
				}
			}
			logger.debug(rmpt.getTokenName());
			
		case RMProcessorContext.COMMIT:
			
			PolicyEngineData ped = rmpc.readCurrentPolicyEngineData();
			String text = rmpc.getAssertion().getStrValue();
			ped.setInvokeInOrder(new Boolean(text).booleanValue());
			break;
		case RMProcessorContext.ABORT:
			break;
		}
		return new Boolean(true);
	}
}
