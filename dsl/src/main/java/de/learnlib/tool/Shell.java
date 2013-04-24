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
package de.learnlib.tool;

import de.learnlib.tool.commands.Command;
import de.learnlib.tool.commands.New;
import de.learnlib.tool.commands.Exit;
import de.learnlib.tool.commands.Heap;
import de.learnlib.tool.commands.List;
import de.learnlib.tool.commands.Run;
import de.learnlib.tool.discovery.ComponentDirectory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author falkhowar
 */
public class Shell {
    
    private static final String prompt = "$ ";
    
    private static final Logger logger = Logger.getLogger(Shell.class.getName());
    
    private Map<String,Command> commands = new HashMap<>();
    
    private Map<String,Object> heap = new HashMap<>();
    
    private PrintStream out = System.out;

    private boolean interactive;
    
    private BufferedReader in;
    
    private String readLine() {
        if (interactive) {
            out.print(prompt);
        }
        try {
            return in.readLine().trim();
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        return "";
    }
    
    private void run() {        
        while (true) {        
            String command = readLine();
            if (command == null) {
                command = "exit";
            }
            
            if (command.length() < 1 || command.startsWith("//")) {
                continue;
            }
            
            
            
            String[] parameters = command.split(" ");
            for (int i=0;i<parameters.length;i++) {
                parameters[i] = parameters[i].trim();
            }
            command = parameters[0];
            
            if (command.equals("help") && interactive) {
                if (parameters.length < 2) {
                    out.println("use help [command] for specific help");
                    out.println("commands: " + Arrays.toString(commands.keySet().toArray()));
                } else {
                    Command cmd = this.commands.get(parameters[1]);
                    if (cmd == null) {
                        out.println("unknown command: " + cmd);
                    } else {
                        out.println(cmd.help());
                    }                    
                }
                continue;
            }
            
            String retval = null;
            if (command.contains("=")) {
                String[] temp = command.split("=",2);
                retval = temp[0].trim();
                command = temp[1].trim();
                parameters[0] = command;
            }
            
            Command cmd = this.commands.get(command);
            if (cmd == null) {
                if (interactive) {
                    out.println("unknown command: " + cmd);
                } else {
                    logger.log(Level.SEVERE, "unknown command: {0}", cmd);
                }
                continue;
            }
            
            String output;
            try {
                output = cmd.execute(parameters, heap, retval);
            } catch (Throwable e) {
                if (!interactive) {
                    throw e;
                }
                output = "error: " + e;
            }
            if (output != null && output.length() > 0) {
                if (interactive) {
                    out.println(output);
                } else {
                    logger.log(Level.INFO, output);
                }
            }
            
            if (command.equals("exit")) {
                break;
            }
        }
        
        if (interactive) {
            out.println("bye!");
        }
        
    }
    
    public Shell(BufferedReader in, boolean interactive) {
        this.in = in;
        this.interactive = interactive;
        
        ComponentDirectory directory = new ComponentDirectory();
        directory.discoverComponents();
        
        this.commands.put("exit", new Exit());
        this.commands.put("new", new New(directory));
        this.commands.put("list", new List(directory));
        this.commands.put("run", new Run());
        this.commands.put("heap", new Heap());
    }
    
    
    public static void main(String[] args) {
    
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        boolean interactive = true;
        if (args.length > 0) {
        }
        
        Shell shell = new Shell(in, interactive);
        shell.run();
    
    }
    
    
    
}
