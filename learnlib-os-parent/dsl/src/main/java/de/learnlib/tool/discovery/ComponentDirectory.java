/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.learnlib.tool.discovery;

import de.learnlib.components.LLComponent;
import de.learnlib.components.LLComponentFactory;
import de.learnlib.components.LLComponentParameter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author falkhowar
 */
public class ComponentDirectory {

    public static class ComponentDescriptor {
    
        public final LLComponent componentInfo;
        public final LLComponentFactory factory;        
        public final Method instantiator;
        public final Map<LLComponentParameter, Method> parameters = new HashMap<>();
        
        public ComponentDescriptor(LLComponent componentInfo, LLComponentFactory factory) 
                throws NoSuchMethodException {
            this.componentInfo = componentInfo;
            this.factory = factory;
            this.instantiator = factory.getClass().getMethod("instantiate");
            for (Method m : factory.getClass().getDeclaredMethods()) {
                if (m.isAnnotationPresent(LLComponentParameter.class)) {
                    LLComponentParameter cp = (LLComponentParameter) m.getAnnotation(LLComponentParameter.class);
                    this.parameters.put(cp, m);
                }
            }
        }        
    }

    private static final Logger logger = Logger.getLogger(ComponentDirectory.class.getName());
    
    private Map<String, ComponentDescriptor> components = new HashMap<>();  
    
    public boolean registerComponent(String className) {
    
        try {
            Class<?> clazz = Class.forName(className);     
            if (!clazz.isAnnotationPresent(LLComponent.class)) {
                logger.log(Level.SEVERE, "Class {0} is not a LearnLib component factory", 
                        className);
                return false;            
            }
            Object instance = clazz.newInstance();
            if (!(instance instanceof LLComponentFactory)) {
                logger.log(Level.SEVERE, "Class {0} is not a ComponentFactory", 
                        className);
                return false;                        
            }
            
            LLComponentFactory factory = (LLComponentFactory) instance;
            LLComponent llComponent = (LLComponent) clazz.getAnnotation(LLComponent.class);
            if (this.components.containsKey(llComponent.name())) {
                logger.log(Level.SEVERE, "Name {0} is already in use", 
                        llComponent.name());
                return false;                                    
            }
            
            ComponentDescriptor dsc = new ComponentDescriptor(llComponent, factory);
            this.components.put(llComponent.name(), dsc);
            return true;
            
        } catch (ClassNotFoundException | InstantiationException | 
                IllegalAccessException | NoSuchMethodException ex) {
            logger.log(Level.SEVERE, "Class {0} could not be registered as component: {1}", 
                    new Object[]{className, ex});
        }
        
        return false;        
    }
    
    public ComponentDescriptor getDescriptor(String name) {
        return this.components.get(name);
    }
    
 
    public Collection<ComponentDescriptor> getDescriptors(Class<?> type) {
        List<ComponentDescriptor> dscs = new LinkedList<>();
        for (ComponentDescriptor d : this.components.values()) {
            if (type.isAssignableFrom(d.componentInfo.type())) {
                dscs.add(d);
            }
        }
        return dscs;
    }
                     
    
    public void discoverComponents() {
        try {
            Enumeration<URL> lists = ClassLoader.getSystemResources("META-INF/learnlib/factories");
            while (lists.hasMoreElements()) {
                URL url = lists.nextElement();
                URLConnection uc = url.openConnection();
		BufferedReader in = new BufferedReader(new InputStreamReader(uc.getInputStream()));
		String line;
		while ((line = in.readLine()) != null) {
			line = line.trim();
			if("".equals(line))
				continue;
                    registerComponent(line);
		}            
            }            
        } catch (IOException ex) {
            Logger.getLogger(ComponentDirectory.class.getName()).log(Level.SEVERE, null, ex);
        }
    
    }
    
}
