package org.apache.sandesha.storage.queue;

/**
 * Created by IntelliJ IDEA.
 * User: jaliya
 * Date: Jul 2, 2005
 * Time: 2:04:01 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractSequence {

    protected String sequenceId;

    public String getSequenceId() {
        return sequenceId;
    }

    public void setSequenceId(String sequenceId) {
        this.sequenceId = sequenceId;
    }
}
