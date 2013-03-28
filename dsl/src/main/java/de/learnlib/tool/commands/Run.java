/* Copyright (C) 2013 TU Dortmund
 * This file is part of LearnLib, http://www.learnlib.de/.
 * 
 * LearnLib is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 3.0 as published by the Free Software Foundation.
 * 
 * LearnLib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with LearnLib; if not, see
 * <http://www.gnu.de/documents/lgpl.en.html>.
 */
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.learnlib.tool.commands;

import java.util.Map;

import de.learnlib.experiments.Experiment;

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
        
        Experiment<?, ?, ?> experiment = new Experiment<Object,Object,Object>(null, null, null);
        experiment.run();
        return null;
    }
    
}
