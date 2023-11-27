/* Copyright (C) 2013-2023 TU Dortmund
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

package de.learnlib.algorithm.lsharp;

import java.util.HashMap;
import java.util.Map;

import net.automatalib.common.util.Pair;
import org.checkerframework.checker.nullness.qual.Nullable;

public class MapTransitions<I, O> implements TransitionInformation<I, O> {
    private final Map<I, Pair<O, LSState>> trans;

    public MapTransitions(Integer inSize) {
        trans = new HashMap<>(inSize);
    }

    @Override
    public @Nullable Pair<O, LSState> getOutSucc(I input) {
        return trans.getOrDefault(input, null);
    }

    @Override
    public void addTrans(I input, O output, LSState d) {
        Pair<O, LSState> out = trans.put(input, Pair.of(output, d));
        assert out == null;
    }

}
