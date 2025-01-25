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
package de.learnlib.algorithm.aaar.abstraction;

import java.util.function.Function;

import de.learnlib.oracle.MembershipOracle;

public class ExplicitAbstractionTree<AI, CI, D> extends AbstractAbstractionTree<AI, CI, D> {

    private final AI rootA;
    private final Function<AI, AI> incrementor;

    public ExplicitAbstractionTree(AI rootA, CI rootC, MembershipOracle<CI, D> o, Function<AI, AI> incrementor) {
        super(rootA, rootC, o);

        this.rootA = rootA;
        this.incrementor = incrementor;
    }

    @Override
    protected AI createAbstractionForRepresentative(CI ci) {
        return this.incrementor.apply(this.rootA);
    }
}
