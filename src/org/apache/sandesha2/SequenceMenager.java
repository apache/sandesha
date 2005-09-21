/*
 * Created on Sep 5, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.apache.sandesha2;

import java.util.ArrayList;

import javax.naming.Context;

import org.apache.axis2.context.AbstractContext;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.sandesha2.storage.AbstractBeanMgrFactory;
import org.apache.sandesha2.storage.beanmanagers.NextMsgBeanMgr;
import org.apache.sandesha2.storage.beanmanagers.SequencePropertyBeanMgr;
import org.apache.sandesha2.storage.beans.NextMsgBean;
import org.apache.sandesha2.storage.beans.SequencePropertyBean;

/**
 * @author Chamikara
 * @author Sanka
 * @author Jaliya 
 */
public class SequenceMenager {

	public static void setUpNewSequence (String sequenceId,ConfigurationContext context) {
//		SequencePropertyBean seqPropBean = new SequencePropertyBean (sequenceId,Constants.SEQ_PROPERTY_RECEIVED_MESSAGES,"");
//		SequencePropertyBeanMgr beanMgr = new SequencePropertyBeanMgr (Constants.DEFAULT_STORAGE_TYPE);
//		beanMgr.create(seqPropBean);
	
		SequencePropertyBean seqPropBean = new SequencePropertyBean (sequenceId,Constants.SEQ_PROPERTY_RECEIVED_MESSAGES,"");
		SequencePropertyBeanMgr seqPropMgr = AbstractBeanMgrFactory.getInstance(context).getSequencePropretyBeanMgr();
		seqPropMgr.insert(seqPropBean);
		
		NextMsgBeanMgr nextMsgMgr = AbstractBeanMgrFactory.getInstance(context).getNextMsgBeanMgr();
		nextMsgMgr.insert(new NextMsgBean (sequenceId,1)); // 1 will be the next message to invoke
														   //this will apply for only in-order invocations.	
	}
	
	public void removeSequence (String sequence) {
				
	}
}
