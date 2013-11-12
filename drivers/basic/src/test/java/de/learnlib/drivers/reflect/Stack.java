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
package de.learnlib.drivers.reflect;

import java.util.LinkedList;

/**
 *
 * @author falkhowar
 */
public class Stack {
    
    private final int capacity;

    private final LinkedList<Object> back = new LinkedList<>();
    
    public Stack(int capacity) {
        this.capacity = capacity;
    }
    
    public void push(Object o) {
        if (back.size()>= capacity) {
            throw new IllegalStateException("capacity exceeded");
        }
        back.push(o);
    }
    
    public Object pop() {
        return back.pop();
    } 
    
}
