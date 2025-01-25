/* Copyright (C) 2013-2025 TU Dortmund University
 * This file is part of LearnLib <https://learnlib.de>.
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
package de.learnlib.algorithm.ttt.vpa;

import java.util.Collections;
import java.util.Set;

import net.automatalib.automaton.vpa.StackContents;
import org.checkerframework.checker.nullness.qual.Nullable;

final class NondetStackContents {

    private final Set<Integer> syms;

    private final NondetStackContents rest;

    private final boolean isTrueNondet;

    NondetStackContents(Set<Integer> syms, NondetStackContents rest) {
        this.syms = syms;
        this.rest = rest;
        this.isTrueNondet = rest != null && rest.isTrueNondet || syms.size() > 1;
    }

    public static NondetStackContents push(Set<Integer> syms, NondetStackContents rest) {
        return new NondetStackContents(syms, rest);
    }

    public static @Nullable NondetStackContents fromDet(@Nullable StackContents sc) {
        if (sc == null) {
            return null;
        }
        return push(Collections.singleton(sc.peek()), fromDet(sc.pop()));
    }

    public static @Nullable StackContents toDet(@Nullable NondetStackContents nsc) {
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
