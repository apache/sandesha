/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Axis" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
package org.apache.sandesha.client;

import org.apache.sandesha.RMSequence;
import org.apache.sandesha.ws.rm.SequenceAcknowledgement;
import org.apache.sandesha.ws.utility.Identifier;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 
 * Amila Navarathna<br>
 * Jaliya Ekanayaka<br>
 * Sudar Nimalan<br>
 * (Apache Sandesha Project)
 *
 */
public class ClientMessageController {
	private static ClientMessageController instance;

	private Map sequenceMap;
	private SequenceAcknowledgement seqAck;

	private ClientMessageController() {
		sequenceMap = new HashMap();
	}
	public static ClientMessageController getInstance() {
		System.out.println("MessageController::getInstance");
		if (instance == null) {
			instance = new ClientMessageController();
		}
		return instance;
	}

	/**
	 * stores a sequence object in the map. Each of these sequence objects 
	 * consists of one or more message objects.
	 * The sequences are stored as the sequenceIdentifier as a key
	 * 
	 * @param sequence
	 * 
	 * TODO:
	 */
	public void storeSequence(RMSequence sequence) {
		//System.out.println("----------------storeSequence::"+sequence.getSequenceIdetifer());
		sequenceMap.put(sequence.getSequenceIdetifer().toString(), sequence);
	}

	/**
	 * 
	 * returns a RMSequence if a sequence for the identifier exists.
	 * else return a null value
	 * <b>developer must handle the null value returned</b>
	 * @param identifier
	 * @return
	 * 
	 * TODO:
	 */
	public RMSequence retrieveIfSequenceExists(Identifier identifier) {
		if (sequenceMap.get(identifier.toString()) != null){
			return ((RMSequence) sequenceMap.get(identifier.toString()));
			}
		
		else
			return null;
	}

	/**
	 * @return
	 * 
	 * TODO:
	 */
	public SequenceAcknowledgement getSeqAck() {
		return seqAck;
	}

	/**
	 * @param acknowledgement
	 * 
	 * TODO:
	 */
	public void setSeqAck(SequenceAcknowledgement acknowledgement) {
		seqAck = acknowledgement;
	}
	

}
