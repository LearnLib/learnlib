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

import de.learnlib.components.LLComponentParameter;
import de.learnlib.tool.discovery.ComponentDirectory;
import java.lang.reflect.Method;
import java.util.Map;

/**
 *
 * @author falkhowar
 */
public class List implements Command {

    private ComponentDirectory directory;

    public List(ComponentDirectory directory) {
        this.directory = directory;
    }
    
    
    @Override
    public String cmd() {
        return "list";
    }

    @Override
    public String help() {
        return "lists available components. pass component name for detailed information.";
    }

    @Override
    public String execute(String[] parameter, Map<String, Object> heap, String retval) {
        StringBuilder sb = new StringBuilder();
        if (parameter.length < 2) {
            for (ComponentDirectory.ComponentDescriptor d : 
                    directory.getDescriptors(Object.class)) {
                sb.append(d.componentInfo.name()).append(" : ").
                        append(d.componentInfo.type()).append(System.getProperty("line.separator"));
            }
        } else {
            ComponentDirectory.ComponentDescriptor d = directory.getDescriptor(parameter[1]);
            if (d == null) {
                sb.append("unknown component: ").append(parameter[1]).append(System.getProperty("line.separator"));
            }
            else {
                sb.append("component: ").append(d.componentInfo.name()).append(System.getProperty("line.separator"));
                sb.append("  description: ").append(d.componentInfo.description()).append(System.getProperty("line.separator"));
                sb.append("  class: ").append(d.factory.getClass().getName()).append(System.getProperty("line.separator"));
                sb.append("  parameters: ").append(System.getProperty("line.separator"));
                for (Map.Entry<LLComponentParameter,Method> p : d.parameters.entrySet()) {
                    sb.append("    ").append(p.getKey().name()).append(
                            p.getKey().required() ? " REQUIRED" : " OPTIONAL").append(
                            " [").append(p.getValue().getParameterTypes()[0].getName()).append(
                            "]    ").append(p.getKey().description()).append(
                            System.getProperty("line.separator"));        
                }            
            }
        }
        
        return sb.toString();
    }
    
}
