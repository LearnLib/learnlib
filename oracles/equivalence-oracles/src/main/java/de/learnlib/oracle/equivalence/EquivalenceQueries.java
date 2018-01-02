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
package de.learnlib.oracle.equivalence;

import de.learnlib.api.oracle.MembershipOracle;
import net.automatalib.automata.UniversalDeterministicAutomaton;
import net.automatalib.automata.concepts.Output;

public final class EquivalenceQueries {

    private EquivalenceQueries() {
        // prevent instantiation
    }

    public static <A extends Output<I, D>, I, D> RandomWordsEQOracle<A, I, D>
    randomWords(MembershipOracle<I, D> sulOracle, int minLength, int maxLength, int maxTests) {
        return new RandomWordsEQOracle<>(sulOracle, minLength, maxLength, maxTests);
    }

    public static <I, D> CompleteExplorationEQOracle<I, D>
    complete(MembershipOracle<I, D> sulOracle, int maxDepth) {
        return new CompleteExplorationEQOracle<>(sulOracle, maxDepth);
    }

    public static <A extends UniversalDeterministicAutomaton<?, I, ?, ?, ?> & Output<I, D>, I, D> WMethodEQOracle<A, I, D>
    wMethod(MembershipOracle<I, D> sulOracle, int maxDepth) {
        return new WMethodEQOracle<>(sulOracle, maxDepth);
    }

    public static <A extends UniversalDeterministicAutomaton<?, I, ?, ?, ?> & Output<I, D>, I, D> WpMethodEQOracle<A, I, D>
    wpMethod(MembershipOracle<I, D> sulOracle, int maxDepth) {
        return new WpMethodEQOracle<>(sulOracle, maxDepth);
    }

    public static <A extends UniversalDeterministicAutomaton<?, I, ?, ?, ?> & Output<I, D>, I, D> SimulatorEQOracle<I, D>
    simulator(A target) {
        return new SimulatorEQOracle<>(target);
    }

}
