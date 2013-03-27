/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.learnlib.tool.commands;

import java.util.Map;

/**
 *
 * @author falkhowar
 */
public interface Command {
    
    public String cmd();
    
    public String help();
    
    public String execute(String[] parameter, Map<String,Object> heap, String retval);
    
}
