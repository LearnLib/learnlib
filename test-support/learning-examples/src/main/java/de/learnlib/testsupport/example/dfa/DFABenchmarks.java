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
package de.learnlib.testsupport.example.dfa;

import java.io.IOException;
import java.io.InputStream;

import de.learnlib.logging.Category;
import de.learnlib.testsupport.example.DefaultLearningExample.DefaultDFALearningExample;
import de.learnlib.testsupport.example.LearningExample.DFALearningExample;
import net.automatalib.automaton.fsa.impl.CompactDFA;
import net.automatalib.common.util.IOUtil;
import net.automatalib.serialization.learnlibv2.LearnLibV2Serialization;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DFABenchmarks {

    private static final Logger LOGGER = LoggerFactory.getLogger(DFABenchmarks.class);

    private DFABenchmarks() {
        // prevent instantiation
    }

    public static @Nullable DFALearningExample<Integer> loadLearnLibV2Benchmark(String name) {
        final String resourceName = "/automata/learnlibv2/" + name + ".dfa.gz";
        final InputStream resourceStream = DFABenchmarks.class.getResourceAsStream(resourceName);

        if (resourceStream == null) {
            LOGGER.info(Category.SYSTEM, "Couldn't find resource '{}'", resourceName);
        } else {
            try (InputStream is = IOUtil.asUncompressedBufferedInputStream(resourceStream)) {
                CompactDFA<Integer> dfa = LearnLibV2Serialization.getInstance().readGenericDFA(is);
                return new DefaultDFALearningExample<>(dfa);
            } catch (IOException ex) {
                LOGGER.error(Category.SYSTEM, "Could not load benchmark", ex);
            }
        }

        return null;
    }

    public static DFALearningExample<Integer> loadPots2() {
        return load("pots2");
    }

    public static DFALearningExample<Integer> loadPots3() {
        return load("pots3");
    }

    public static DFALearningExample<Integer> loadPeterson2() {
        return load("peterson2");
    }

    public static DFALearningExample<Integer> loadPeterson3() {
        return load("peterson3");
    }

    private static DFALearningExample<Integer> load(String id) {
        final DFALearningExample<Integer> benchmark = loadLearnLibV2Benchmark(id);

        if (benchmark == null) {
            throw new IllegalStateException("Couldn't find '" + id + "'. Are the correct JARs loaded?");
        }

        return benchmark;
    }
}
