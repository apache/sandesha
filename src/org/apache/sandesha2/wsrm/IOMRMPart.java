/*
 * Created on Sep 21, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.apache.sandesha2.wsrm;

import org.apache.axis2.soap.SOAPEnvelope;

/**
 * @author chamikara
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public interface IOMRMPart extends IOMRMElement {
	public void toSOAPEnvelope (SOAPEnvelope envelope);
}
