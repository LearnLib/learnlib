/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.learnlib.tool.commands;

import de.learnlib.tool.discovery.ComponentDirectory;
import java.util.Map;

/**
 *
 * @author falkhowar
 */
public class New implements Command {

    private ComponentDirectory directory;

    public New(ComponentDirectory directory) {
        this.directory = directory;
    }
    
    @Override
    public String cmd() {
        return "new";
    }

    @Override
    public String help() {
        return "creates a new isntance of a component";
    }

    @Override
    public String execute(String[] parameter, Map<String, Object> heap, String retval) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
