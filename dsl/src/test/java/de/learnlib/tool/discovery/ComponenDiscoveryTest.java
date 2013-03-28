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

package de.learnlib.tool.discovery;


import de.learnlib.api.MembershipOracle;
import de.learnlib.components.LLComponentParameter;
import java.lang.reflect.Method;
import java.util.Map;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 *
 * @author falkhowar
 */
public class ComponenDiscoveryTest {
    
    @Test
    public void testDiscovery() {
        System.out.println("testDiscovery");
        String className = "de.learnlib.oracles.CounterOracleFactory";
        ComponentDirectory instance = new ComponentDirectory();
        instance.discoverComponents();
        boolean result = instance.registerComponent(className);        
        Assert.assertFalse(result);
        ComponentDirectory.ComponentDescriptor e = instance.getDescriptor("CounterOracle");
        Assert.assertNotNull(e);
        
        for (ComponentDirectory.ComponentDescriptor d : instance.getDescriptors(MembershipOracle.class)) {
        
            System.out.println("  Component: " + d.componentInfo.name());
            System.out.println("  Description: " + d.componentInfo.description());
            System.out.println("  Class: " + d.factory.getClass().getName());
            System.out.println("  Parameters: ");
            for (Map.Entry<LLComponentParameter,Method> p : d.parameters.entrySet()) {
                System.out.println("    " + p.getKey().name() + 
                        (p.getKey().required() ? " REQUIRED" : " OPTIONAL") +
                        " [" +  p.getValue().getParameterTypes()[0].getName() + "]    " + 
                        p.getKey().description());        
            }
        
        }
        
    }

}
