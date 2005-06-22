/*
 * Copyright  1999-2004 The Apache Software Foundation.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.apache.sandesha;

/**
 * RMReport is generated at the end of the termination of each sequence.
 * @auther Chamikara Jayalath
 * @auther Jaliya Ekanyake
 */
public class RMReport {
	
	private  boolean allAcked;
	private int returns;
    private String error;

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public int getReturns() {
        return returns;
    }

    public void setReturns(int returns) {
        this.returns = returns;
    }

    public boolean isAllAcked() {
        return this.allAcked;
    }

    public int getNumberOfReturnMessages() {
        return this.returns;
    }
    
    public void setAllAcked(boolean acked){
    	this.allAcked = acked;
    }
    
    public void setNoOfReturmMessages(int n){
    	this.returns = n;
    }

    public void incrementReturnedMsgCount(){
       this.returns++;
    }

}
