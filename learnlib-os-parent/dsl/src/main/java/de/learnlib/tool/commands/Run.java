/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.learnlib.tool.commands;

import de.learnlib.experiments.Experiment;
import java.util.Map;

/**
 *
 * @author falkhowar
 */
public class Run implements Command {

    @Override
    public String cmd() {
        return "run";
    }

    @Override
    public String help() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String execute(String[] parameter, Map<String, Object> heap, String retval) {
        
        Experiment experiment = new Experiment(null, null, null);
        experiment.run();
        return null;
    }
    
}
