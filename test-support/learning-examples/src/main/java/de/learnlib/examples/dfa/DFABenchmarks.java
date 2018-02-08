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
package de.learnlib.examples.dfa;

import java.io.IOException;
import java.io.InputStream;

import de.learnlib.examples.DefaultLearningExample.DefaultDFALearningExample;
import de.learnlib.examples.LearningExample.DFALearningExample;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.serialization.learnlibv2.LearnLibV2Serialization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DFABenchmarks {

    private static final Logger LOGGER = LoggerFactory.getLogger(DFABenchmarks.class);

    private DFABenchmarks() {
        // prevent instantiation
    }

    public static DFALearningExample<Integer> loadPots2() {
        return loadLearnLibV2Benchmark("pots2");
    }

    public static DFALearningExample<Integer> loadLearnLibV2Benchmark(String name) {
        String resourceName = "/automata/learnlibv2/" + name + ".dfa.gz";
        if (DFABenchmarks.class.getResource(resourceName) == null) {
            return null;
        }

        try (InputStream is = DFABenchmarks.class.getResourceAsStream(resourceName)) {
            CompactDFA<Integer> dfa = LearnLibV2Serialization.getInstance().readGenericDFA(is);
            return new DefaultDFALearningExample<>(dfa);
        } catch (IOException ex) {
            LOGGER.error("Could not load benchmark", ex);
            return null;
        }
    }

    public static DFALearningExample<Integer> loadPots3() {
        return loadLearnLibV2Benchmark("pots3");
    }

    public static DFALearningExample<Integer> loadPeterson2() {
        return loadLearnLibV2Benchmark("peterson2");
    }

    public static DFALearningExample<Integer> loadPeterson3() {
        return loadLearnLibV2Benchmark("peterson3");
    }
}
