package org.apache.sandesha2.policy;

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.sandesha2.policy.processors.AcknowledgementIntervalProcessor;
import org.apache.sandesha2.policy.processors.ExponentialBackoffProcessor;
import org.apache.sandesha2.policy.processors.InactivityTimeoutMeasureProcessor;
import org.apache.sandesha2.policy.processors.InactivityTimeoutProcessor;
import org.apache.sandesha2.policy.processors.InvokeInOrderProcessor;
import org.apache.sandesha2.policy.processors.MaximumRetransmissionCountProcessor;
import org.apache.sandesha2.policy.processors.MessageTypesToDropProcessor;
import org.apache.sandesha2.policy.processors.RetransmissionItervalProcessor;
import org.apache.sandesha2.policy.processors.StorageManagersProcessor;
import org.apache.ws.policy.AndCompositeAssertion;
import org.apache.ws.policy.Assertion;
import org.apache.ws.policy.Policy;
import org.apache.ws.policy.PrimitiveAssertion;
import org.apache.ws.policy.XorCompositeAssertion;
import org.apache.ws.policy.util.PolicyFactory;
import org.apache.ws.policy.util.PolicyReader;

public class RMPolicyProcessor {

	Log logger = LogFactory.getLog(this.getClass().getName());

	PolicyReader prdr = null;

	RMPolicyToken topLevel = new RMPolicyToken("_TopLevel_",
			RMPolicyToken.COMPLEX_TOKEN, null);

	RMProcessorContext rmProcessorContext = null;

	public boolean setup() throws NoSuchMethodException {
		logger.debug("RMPolicyProcessor:setUp");

		prdr = PolicyFactory.getPolicyReader(PolicyFactory.OM_POLICY_READER);
		/*
		 * Initialize the top level security policy token.
		 */
		RMPolicyToken rpt = null;

		RetransmissionItervalProcessor rip = new RetransmissionItervalProcessor();
		rpt = RMPolicy.retransmissionIterval.copy();
		rpt.setProcessTokenMethod(rip);
		topLevel.setChildToken(rpt);

		AcknowledgementIntervalProcessor aip = new AcknowledgementIntervalProcessor();
		rpt = RMPolicy.acknowledgementInterval.copy();
		rpt.setProcessTokenMethod(aip);
		topLevel.setChildToken(rpt);

		MaximumRetransmissionCountProcessor mrip = new MaximumRetransmissionCountProcessor();
		rpt = RMPolicy.maximumRetransmissionCount.copy();
		rpt.setProcessTokenMethod(mrip);
		topLevel.setChildToken(rpt);
		
		ExponentialBackoffProcessor ebp = new ExponentialBackoffProcessor();
		rpt = RMPolicy.exponentialBackoff.copy();
		rpt.setProcessTokenMethod(ebp);
		topLevel.setChildToken(rpt);

		InactivityTimeoutMeasureProcessor itmp = new InactivityTimeoutMeasureProcessor();
		rpt = RMPolicy.inactiveTimeoutMeasure.copy();
		rpt.setProcessTokenMethod(itmp);
		topLevel.setChildToken(rpt);

		InactivityTimeoutProcessor itp = new InactivityTimeoutProcessor();
		rpt = RMPolicy.inactiveTimeout.copy();
		rpt.setProcessTokenMethod(itp);
		topLevel.setChildToken(rpt);

		InvokeInOrderProcessor iiop = new InvokeInOrderProcessor();
		rpt = RMPolicy.invokeInOrder.copy();
		rpt.setProcessTokenMethod(iiop);
		topLevel.setChildToken(rpt);

		MessageTypesToDropProcessor mttdp = new MessageTypesToDropProcessor();
		rpt = RMPolicy.messageTypeToDrop.copy();
		rpt.setProcessTokenMethod(mttdp);
		topLevel.setChildToken(rpt);

		StorageManagersProcessor smp = new StorageManagersProcessor();
		rpt = RMPolicy.storageManagers.copy();
		rpt.setProcessTokenMethod(smp);
		topLevel.setChildToken(rpt);

		/*
		 * Now get the initial PolicyEngineData, initialize it and put it onto
		 * the PED stack.
		 */
		PolicyEngineData ped = new PolicyEngineData();
		ped.initializeWithDefaults();

		/*
		 * Now get a context and push the top level token onto the token stack.
		 * The top level token is a special token that acts as anchor to start
		 * parsing.
		 */
		rmProcessorContext = new RMProcessorContext();
		rmProcessorContext.pushRMToken(topLevel);
		rmProcessorContext.pushPolicyEngineData(ped);

		return true;
	}

	/**
	 * This method takes a normalized policy object, processes it and returns
	 * true if all assertion can be fulfilled.
	 * 
	 * Each policy must be nromalized accordig to the WS Policy framework
	 * specification. Therefore a policy has one child (wsp:ExactlyOne) that is
	 * a XorCompositeAssertion. This child may contain one or more other terms
	 * (alternatives). To match the policy one of these terms (alternatives)
	 * must match. If none of the contained terms match this policy cannot be
	 * enforced.
	 * 
	 * @param policy
	 *            The policy to process
	 * @return True if this policy can be enforced by the policy enforcement
	 *         implmentation
	 */
	public boolean processPolicy(Policy policy) {
		logger.debug("RMPolicyProcessor:processPolicy");

		if (!policy.isNormalized()) {
			policy = (Policy) policy.normalize();
		}

		XorCompositeAssertion xor = (XorCompositeAssertion) policy.getTerms()
				.get(0);
		List listOfPolicyAlternatives = xor.getTerms();

		boolean success = false;
		int numberOfAlternatives = listOfPolicyAlternatives.size();

		for (int i = 0; !success && i < numberOfAlternatives; i++) {
			AndCompositeAssertion aPolicyAlternative = (AndCompositeAssertion) listOfPolicyAlternatives
					.get(i);

			List listOfAssertions = aPolicyAlternative.getTerms();

			Iterator iterator = listOfAssertions.iterator();
			/*
			 * Loop over all assertions in this alternative. If all assertions
			 * can be fulfilled then we choose this alternative and signal a
			 * success.
			 */
			boolean all = true;
			while (all && iterator.hasNext()) {
				Assertion assertion = (Assertion) iterator.next();

				/*
				 * At this point we expect PrimitiveAssertions only.
				 */
				if (!(assertion instanceof PrimitiveAssertion)) {
					logger.debug("Got a unexpected assertion type: "
							+ assertion.getClass().getName());
					continue;
				}
				/*
				 * We need to pick only the primitive assertions which contain a
				 * WSRM policy assertion. For that we'll check the namespace of
				 * the primitive assertion
				 */
				PrimitiveAssertion pa = (PrimitiveAssertion) assertion;
				if (!(pa.getName().getNamespaceURI()
						.equals("http://ws.apache.org/sandesha2/policy"))) {
					logger.debug("Got a unexpected assertion: "
							+ pa.getName().getLocalPart());
					continue;
				}
				all = processPrimitiveAssertion((PrimitiveAssertion) assertion);
			}
			/*
			 * copy the status of assertion processing. If all is true then this
			 * alternative is "success"ful
			 */
			success = all;
		}
		return success;
	}

	boolean processPrimitiveAssertion(PrimitiveAssertion pa) {
		logger.debug("RMPolicyManager:processPrimitiveAssertion");

		boolean commit = true;

		commit = startPolicyTransaction(pa);

		List terms = pa.getTerms();
		if (commit && terms.size() > 0) {
			for (int i = 0; commit && i < terms.size(); i++) {
				Assertion assertion = (Assertion) pa.getTerms().get(i);
				if (assertion instanceof Policy) {
					commit = processPolicy((Policy) assertion);
				} else if (assertion instanceof PrimitiveAssertion) {
					commit = processPrimitiveAssertion((PrimitiveAssertion) assertion);
				}
			}
		}
		if (commit) {
			commitPolicyTransaction(pa);
		} else {
			abortPolicyTransaction(pa);
		}
		return commit;
	}

	public boolean startPolicyTransaction(PrimitiveAssertion pa) {
		logger.debug("RMPolicyProcessor:startPolicyTransaction");

		String tokenName = pa.getName().getLocalPart();

		RMPolicyToken rmpt = null;

		/*
		 * Get the current rm policy token from the context and check if the
		 * current token supports/contains this assertion as token. If yes set
		 * this token as current token (push onto stack), set the assertion into
		 * context and call the processing method for this token.
		 */
		RMPolicyToken currentToken = rmProcessorContext
				.readCurrentSecurityToken();
		if (currentToken == null) {
			logger.error("Internal error on token stack - No current token");
			System.exit(1);
		}
		rmpt = currentToken.getChildToken(tokenName);
		rmProcessorContext.pushRMToken(rmpt);
		rmProcessorContext.setAssertion(pa);
		rmProcessorContext.setAction(RMProcessorContext.START);

		/*
		 * Get the current state of the PolicyEngineData, make a copy of it and
		 * push the copy onto the PED stack. The token method works on this
		 * copy, adding its data.
		 */
		PolicyEngineData ped = rmProcessorContext.readCurrentPolicyEngineData();
		ped = ped.copy();
		rmProcessorContext.pushPolicyEngineData(ped);
		if (rmpt == null) {
			logger
					.debug("RM token: '" + tokenName
							+ "' unknown in context of '"
							+ currentToken.getTokenName());
			return false;
		}
		boolean ret = false;

		try {
			ret = rmpt.invokeProcessTokenMethod(rmProcessorContext);
		} catch (Exception ex) {
			logger.error("Exception occured when invoking processTokenMethod",
					ex);
		} finally {
			rmProcessorContext.setAction(RMProcessorContext.NONE);
		}
		return ret;
	}

	public void abortPolicyTransaction(PrimitiveAssertion pa) {
		logger.debug("RMPolicyProcessor:abortPolicyTransaction");

		RMPolicyToken currentToken = rmProcessorContext
				.readCurrentSecurityToken();
		if (currentToken == null) {
			logger.debug("Abort transaction because of unknown token: '"
					+ pa.getName().getLocalPart() + "'");

			rmProcessorContext.popRMToken();
			return;
		}

		rmProcessorContext.setAssertion(pa);
		rmProcessorContext.setAction(RMProcessorContext.ABORT);
		try {
			currentToken.invokeProcessTokenMethod(rmProcessorContext);

		} catch (Exception ex) {
			logger.error("Exception occured when invoking processTokenMethod:",
					ex);

		} finally {
			rmProcessorContext.setAction(RMProcessorContext.NONE);
			rmProcessorContext.popRMToken();
			rmProcessorContext.popPolicyEngineData();

		}
	}

	public void commitPolicyTransaction(PrimitiveAssertion pa) {
		logger.debug("RMPolicyProcessor:commitPolicyTransaction");
		
		RMPolicyToken currentToken = rmProcessorContext
				.readCurrentSecurityToken();
		if (currentToken == null) {
			logger.error("Internal error on token stack - Commiting an unknown token: "
							+ pa.getName().getLocalPart() + "'");
			System.exit(1);
		}
		rmProcessorContext.setAssertion(pa);
		rmProcessorContext.setAction(RMProcessorContext.COMMIT);
		try {
			currentToken.invokeProcessTokenMethod(rmProcessorContext);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			rmProcessorContext.setAction(RMProcessorContext.NONE);
			rmProcessorContext.popRMToken();
			rmProcessorContext.commitPolicyEngineData();
		}
	}

	public RMProcessorContext getContext() {
		return rmProcessorContext;
	}

	public void setContext(RMProcessorContext rmProcessorContext) {
		this.rmProcessorContext = rmProcessorContext;
	}
}
