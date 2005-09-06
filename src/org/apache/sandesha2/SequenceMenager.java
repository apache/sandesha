/*
 * Created on Sep 5, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.apache.sandesha2;

import java.util.ArrayList;

import org.apache.sandesha2.storage.beanmanagers.SequencePropertyBeanMgr;
import org.apache.sandesha2.storage.beans.SequencePropertyBean;

/**
 * @author Chamikara
 * @author Sanka
 * @author Jaliya 
 */
public class SequenceMenager {

	public static void setUpNewSequence (String sequenceId) {
		SequencePropertyBean seqPropBean = new SequencePropertyBean (sequenceId,Constants.SEQ_PROPERTY_RECEIVED_MESSAGES,"");
		SequencePropertyBeanMgr beanMgr = new SequencePropertyBeanMgr (Constants.DEFAULT_STORAGE_TYPE);
		beanMgr.create(seqPropBean);
	}
	
	public void removeSequence (String sequence) {
		
	}
}
