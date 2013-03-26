/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.learnlib.tool.commands;

import java.util.Map;
import java.util.Map.Entry;
import javax.swing.text.html.parser.Entity;

/**
 *
 * @author falkhowar
 */
public class Heap implements Command {

    @Override
    public String cmd() {
        return "heap";
    }

    @Override
    public String help() {
        return "show heap contents.";
    }

    @Override
    public String execute(String[] parameter, Map<String, Object> heap, String retval) {
        StringBuilder sb = new StringBuilder();
        for (Entry<String,Object> e : heap.entrySet()) {
            sb.append(e.getKey()).append(": ").append(e.getValue()).append(System.getProperty("line.separator"));
        }
        return sb.toString();
    }
    
}
