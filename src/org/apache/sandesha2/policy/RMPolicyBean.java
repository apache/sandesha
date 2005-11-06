/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.sandesha2.policy;

/**
 * @author Sanka Samaranayake (sanka@apache.org)
 */

public class RMPolicyBean {
    private long inactiveTimeoutInterval = -1l;
    private long acknowledgementInterval = -1l;
    private long retransmissionInterval = -1l;
    private boolean exponentialBackoff = false;
    
    
    public RMPolicyBean() {
        
    }
    
    public long getInactiveTimeoutInterval() {
        return inactiveTimeoutInterval;
    }
    
    public long getAcknowledgementInaterval() {
        return acknowledgementInterval;
    }
    
    public long getRetransmissionInterval() {
        return retransmissionInterval;
    }
    
    public boolean getExponentialBackoff() {
        return exponentialBackoff;
    }
    
    public void setExponentialBackoff(boolean exponentialBackoff) {
        this.exponentialBackoff = exponentialBackoff;        
    }
    
    public void setRetransmissionInterval(long retransmissionInterval) {
        this.retransmissionInterval = retransmissionInterval;
    }
    
    public void setInactiveTimeoutInterval(long inactiveTimeoutInterval) {
        this.inactiveTimeoutInterval = inactiveTimeoutInterval;
    }
    
    public void setAcknowledgementInterval(long acknowledgementInterval) {
        this.acknowledgementInterval = acknowledgementInterval;
    }
    
    
}
