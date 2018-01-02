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
package de.learnlib.acex.analyzers;

import de.learnlib.acex.AbstractCounterexample;

public class DummyAcex implements AbstractCounterexample<Integer> {

    private final int[] values;

    public DummyAcex(int[] values) {
        this.values = values.clone();
    }

    @Override
    public int getLength() {
        return values.length;
    }

    @Override
    public boolean checkEffects(Integer eff1, Integer eff2) {
        return eff1.equals(eff2);
    }

    @Override
    public Integer effect(int index) {
        return values[index];
    }

}
