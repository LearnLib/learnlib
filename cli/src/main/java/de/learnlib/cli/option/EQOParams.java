/* Copyright (C) 2013-2026 TU Dortmund University
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
package de.learnlib.cli.option;

import java.util.List;

import net.automatalib.util.automaton.conformance.KWayStateCoverTestsIterator.CombinationMethod;
import net.automatalib.util.automaton.conformance.KWayTransitionCoverTestsIterator.GenerationMethod;
import net.automatalib.util.automaton.conformance.KWayTransitionCoverTestsIterator.OptimizationMetric;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Option;

public class EQOParams {

    @ArgGroup(validate = false)
    public KWayStateMethod kWayState = new KWayStateMethod();
    @ArgGroup(validate = false)
    public KWayTransitionMethod kWayTransition = new KWayTransitionMethod();
    @ArgGroup(validate = false)
    public RandomMethod random = new RandomMethod();
    @ArgGroup(validate = false)
    public RandomWMethod randomWMethod = new RandomWMethod();
    @ArgGroup(validate = false)
    public RandomWpMethod randomWpMethod = new RandomWpMethod();
    @ArgGroup(validate = false)
    public Samples samples = new Samples();
    @ArgGroup(validate = false)
    public WMethod wMethod = new WMethod();
    @ArgGroup(validate = false)
    public WpMethod wpMethod = new WpMethod();

    public static class KWayStateMethod {

        @Option(names = "--eqo-kway-s-k",
                defaultValue = "2",
                paramLabel = "<int>",
                descriptionKey = "param.eqo.kway-s.k")
        public int k;

        @Option(names = "--eqo-kway-s-randomWalkLen",
                defaultValue = "20",
                paramLabel = "<int>",
                descriptionKey = "param.eqo.kway-s.randomWalkLen")
        public int randomWalkLen;

        @Option(names = "--eqo-kway-s-combinationMethod",
                defaultValue = "PERMUTATIONS",
                paramLabel = "<method>",
                descriptionKey = "param.eqo.kway-s.combinationMethod")
        public CombinationMethod combinationMethod;

        @Option(names = "--eqo-kway-s-seed",
                defaultValue = "42",
                paramLabel = "<int>",
                descriptionKey = "param.eqo.kway-s.seed")
        public int seed;
    }

    public static class KWayTransitionMethod {

        @Option(names = "--eqo-kway-t-k",
                defaultValue = "2",
                paramLabel = "<int>",
                descriptionKey = "param.eqo.kway-t.k")
        public int k;

        @Option(names = "--eqo-kway-t-randomWalkLen",
                defaultValue = "10",
                paramLabel = "<int>",
                descriptionKey = "param.eqo.kway-t.randomWalkLen")
        public int randomWalkLen;

        @Option(names = "--eqo-kway-t-numGeneratePaths",
                defaultValue = "100",
                paramLabel = "<int>",
                descriptionKey = "param.eqo.kway-t.numGeneratePaths")
        public int numGeneratePaths;

        @Option(names = "--eqo-kway-t-maxPathLen",
                defaultValue = "50",
                paramLabel = "<int>",
                descriptionKey = "param.eqo.kway-t.maxPathLen")
        public int maxPathLen;

        @Option(names = "--eqo-kway-t-maxNumberOfSteps",
                defaultValue = "100",
                paramLabel = "<int>",
                descriptionKey = "param.eqo.kway-t.maxNumberOfSteps")
        public int maxNumberOfSteps;

        @Option(names = "--eqo-kway-t-optimizationMetric",
                defaultValue = "STEPS",
                paramLabel = "<metric>",
                descriptionKey = "param.eqo.kway-t.optimizationMetric")
        public OptimizationMetric optimizationMetric;

        @Option(names = "--eqo-kway-t-generationMethod",
                defaultValue = "RANDOM",
                paramLabel = "<method>",
                descriptionKey = "param.eqo.kway-t.generationMethod")
        public GenerationMethod generationMethod;

        @Option(names = "--eqo-kway-t-seed",
                defaultValue = "42",
                paramLabel = "<int>",
                descriptionKey = "param.eqo.kway-t.seed")
        public int seed;
    }

    public static class RandomMethod {

        @Option(names = "--eqo-random-minLength",
                defaultValue = "10",
                paramLabel = "<int>",
                descriptionKey = "param.eqo.random.minLength")
        public int minLength;

        @Option(names = "--eqo-random-maxLength",
                defaultValue = "20",
                paramLabel = "<int>",
                descriptionKey = "param.eqo.random.maxLength")
        public int maxLength;

        @Option(names = "--eqo-random-maxTests",
                defaultValue = "100",
                paramLabel = "<int>",
                descriptionKey = "param.eqo.random.maxTests")
        public int maxTests;

        @Option(names = "--eqo-random-seed",
                defaultValue = "42",
                paramLabel = "<int>",
                descriptionKey = "param.eqo.random.seed")
        public int seed;
    }

    public static class RandomWMethod {

        @Option(names = "--eqo-random-w-minimalSize",
                defaultValue = "0",
                paramLabel = "<int>",
                descriptionKey = "param.eqo.random-w.minimalSize")
        public int minimalSize;

        @Option(names = "--eqo-random-w-rndLength",
                defaultValue = "5",
                paramLabel = "<int>",
                descriptionKey = "param.eqo.random-w.rndLength")
        public int rndLength;

        @Option(names = "--eqo-random-w-bound",
                defaultValue = "100",
                paramLabel = "<int>",
                descriptionKey = "param.eqo.random-w.bound")
        public int bound;

        @Option(names = "--eqo-random-w-seed",
                defaultValue = "42",
                paramLabel = "<int>",
                descriptionKey = "param.eqo.random-w.seed")
        public int seed;
    }

    public static class RandomWpMethod {

        @Option(names = "--eqo-random-wp-minimalSize",
                defaultValue = "0",
                paramLabel = "<int>",
                descriptionKey = "param.eqo.random-wp.minimalSize")
        public int minimalSize;

        @Option(names = "--eqo-random-wp-rndLength",
                defaultValue = "5",
                paramLabel = "<int>",
                descriptionKey = "param.eqo.random-wp.rndLength")
        public int rndLength;

        @Option(names = "--eqo-random-wp-bound",
                defaultValue = "100",
                paramLabel = "<int>",
                descriptionKey = "param.eqo.random-wp.bound")
        public int bound;

        @Option(names = "--eqo-random-wp-seed",
                defaultValue = "42",
                paramLabel = "<int>",
                descriptionKey = "param.eqo.random-wp.seed")
        public int seed;
    }

    public static class Samples {

        @Option(names = "--eqo-sample", paramLabel = "<string>", descriptionKey = "param.eqo.sample")
        public List<String> samples;

        @Option(names = "--eqo-sample-split",
                defaultValue = "\\s",
                paramLabel = "<string>",
                descriptionKey = "param.eqo.sample.split")
        public String split;
    }

    public static class WMethod {

        @Option(names = "--eqo-w-lookahead",
                defaultValue = "2",
                paramLabel = "<int>",
                descriptionKey = "param.eqo.w.lookahead")
        public int lookahead;

        @Option(names = "--eqo-w-expectedSize",
                defaultValue = "5",
                paramLabel = "<int>",
                descriptionKey = "param.eqo.w.expectedSize")
        public int expectedSize;
    }

    public static class WpMethod {

        @Option(names = "--eqo-wp-lookahead",
                defaultValue = "2",
                paramLabel = "<int>",
                descriptionKey = "param.eqo.wp.lookahead")
        public int lookahead;

        @Option(names = "--eqo-wp-expectedSize",
                defaultValue = "5",
                paramLabel = "<int>",
                descriptionKey = "param.eqo.wp.expectedSize")
        public int expectedSize;
    }

}
