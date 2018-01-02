/* Copyright (C) 2013-2018 TU Dortmund
 * This file is part of LearnLib, http://www.learnlib.de/.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.learnlib.drivers.reflect;

import java.util.LinkedList;

/**
 * A stack implementation with limited size, that throws exception when pushing/pop beyond its capacity/size.
 *
 * @author falkhowar
 */
public class StackWithException {

    private final int capacity;

    private final LinkedList<Object> back = new LinkedList<>();

    public StackWithException(int capacity) {
        this.capacity = capacity;
    }

    public void push(Object o) {
        if (back.size() >= capacity) {
            throw new IllegalStateException("capacity exceeded");
        }
        back.push(o);
    }

    public Object pop() {
        return back.pop();
    }

}
