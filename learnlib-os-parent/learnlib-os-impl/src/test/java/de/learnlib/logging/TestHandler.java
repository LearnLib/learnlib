/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.learnlib.logging;

import java.util.logging.Filter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 *
 * @author falkhowar
 */
public class TestHandler extends Handler {
    
  
    private LogRecord lastMessage = null;

    public TestHandler(Filter f) {
        this.setFilter(f);
    }
    
    @Override
    public void publish(LogRecord record) {
        if (this.isLoggable(record)) {
            this.lastMessage = record;
        }
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() throws SecurityException {
    }

    /**
     * @return the lastMessage
     */
    public LogRecord getLastMessage() {
        return lastMessage;
    }
    
    
    
}
