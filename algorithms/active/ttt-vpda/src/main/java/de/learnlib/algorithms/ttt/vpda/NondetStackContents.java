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
package de.learnlib.algorithms.ttt.vpda;

import java.util.Collections;
import java.util.Set;

import net.automatalib.automata.vpda.StackContents;

/**
 * @author Malte Isberner
 */
final class NondetStackContents {

    private final Set<Integer> syms;

    private final NondetStackContents rest;

    private final boolean isTrueNondet;

    NondetStackContents(Set<Integer> syms, NondetStackContents rest) {
        this.syms = syms;
        this.rest = rest;
        this.isTrueNondet = syms.size() > 1 || (rest != null && rest.isTrueNondet);
    }

    public static NondetStackContents push(Set<Integer> syms, NondetStackContents rest) {
        return new NondetStackContents(syms, rest);
    }

    public static NondetStackContents fromDet(StackContents sc) {
        if (sc == null) {
            return null;
        }
        return push(Collections.singleton(sc.peek()), fromDet(sc.pop()));
    }

    public static StackContents toDet(NondetStackContents nsc) {
        if (nsc == null) {
            return null;
        }
        return StackContents.push(nsc.syms.iterator().next(), toDet(nsc.pop()));
    }

    public Set<Integer> peek() {
        return syms;
    }

    public NondetStackContents pop() {
        return rest;
    }

    public boolean isTrueNondet() {
        return this.isTrueNondet;
    }

}
