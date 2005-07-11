package org.apache.sandesha.interop.testclient;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.PrintWriter;


/**
 * @author root
 *         <p/>
 *         To change the template for this generated type comment go to
 *         Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class ResponseWriter {

    private PrintWriter writer;
    private static final Log log = LogFactory.getLog(PrintWriter.class.getName());

    public ResponseWriter(PrintWriter writer) {
        this.writer = writer;
    }

    public synchronized boolean write(String s) {
        try {
            writer.println(s);
            flush();
            return true;
        } catch (Exception e) {
            log.error("Exception: In method 'write' of 'ResponseWriter");
            return false;
        }
    }

    public synchronized void flush() {
        writer.flush();
    }
}
