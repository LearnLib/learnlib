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
public class Exit implements Command {

    @Override
    public String cmd() {
        return "exit";
    }

    @Override
    public String help() {
        return "exits the shell";
    }

    @Override
    public String execute(String[] parameter, Map<String, Object> heap, String retval) {
        return "";
    }
    
}
