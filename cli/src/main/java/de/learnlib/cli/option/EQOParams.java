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

        @Option(names = "--eqo-kway-s-len",
                defaultValue = "20",
                paramLabel = "<int>",
                descriptionKey = "param.eqo.kway-s.len")
        public int randomWalkLen;

        @Option(names = "--eqo-kway-s-method",
                defaultValue = "PERMUTATIONS",
                paramLabel = "<method>",
                descriptionKey = "param.eqo.kway-s.method")
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

        @Option(names = "--eqo-kway-t-len",
                defaultValue = "10",
                paramLabel = "<int>",
                descriptionKey = "param.eqo.kway-t.len")
        public int randomWalkLen;

        @Option(names = "--eqo-kway-t-num",
                defaultValue = "1000",
                paramLabel = "<int>",
                descriptionKey = "param.eqo.kway-t.num")
        public int numGeneratePaths;

        @Option(names = "--eqo-kway-t-path",
                defaultValue = "50",
                paramLabel = "<int>",
                descriptionKey = "param.eqo.kway-t.path")
        public int maxPathLen;

        @Option(names = "--eqo-kway-t-step",
                defaultValue = "100",
                paramLabel = "<int>",
                descriptionKey = "param.eqo.kway-t.step")
        public int maxNumberOfSteps;

        @Option(names = "--eqo-kway-t-metric",
                defaultValue = "STEPS",
                paramLabel = "<metric>",
                descriptionKey = "param.eqo.kway-t.metric")
        public OptimizationMetric optimizationMetric;

        @Option(names = "--eqo-kway-t-method",
                defaultValue = "RANDOM",
                paramLabel = "<method>",
                descriptionKey = "param.eqo.kway-t.method")
        public GenerationMethod generationMethod;

        @Option(names = "--eqo-kway-t-seed",
                defaultValue = "42",
                paramLabel = "<int>",
                descriptionKey = "param.eqo.kway-t.seed")
        public int seed;
    }

    public static class RandomMethod {

        @Option(names = "--eqo-random-min",
                defaultValue = "10",
                paramLabel = "<int>",
                descriptionKey = "param.eqo.random.min")
        public int minLength;

        @Option(names = "--eqo-random-max",
                defaultValue = "20",
                paramLabel = "<int>",
                descriptionKey = "param.eqo.random.max")
        public int maxLength;

        @Option(names = "--eqo-random-num",
                defaultValue = "1000",
                paramLabel = "<int>",
                descriptionKey = "param.eqo.random.num")
        public int maxTests;

        @Option(names = "--eqo-random-seed",
                defaultValue = "42",
                paramLabel = "<int>",
                descriptionKey = "param.eqo.random.seed")
        public int seed;
    }

    public static class RandomWMethod {

        @Option(names = "--eqo-random-w-min",
                defaultValue = "0",
                paramLabel = "<int>",
                descriptionKey = "param.eqo.random-w.min")
        public int minimalSize;

        @Option(names = "--eqo-random-w-len",
                defaultValue = "5",
                paramLabel = "<int>",
                descriptionKey = "param.eqo.random-w.len")
        public int rndLength;

        @Option(names = "--eqo-random-w-bound",
                defaultValue = "1000",
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

        @Option(names = "--eqo-random-wp-min",
                defaultValue = "0",
                paramLabel = "<int>",
                descriptionKey = "param.eqo.random-wp.min")
        public int minimalSize;

        @Option(names = "--eqo-random-wp-len",
                defaultValue = "5",
                paramLabel = "<int>",
                descriptionKey = "param.eqo.random-wp.len")
        public int rndLength;

        @Option(names = "--eqo-random-wp-bound",
                defaultValue = "1000",
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
                defaultValue = " ",
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

        @Option(names = "--eqo-w-expected",
                defaultValue = "5",
                paramLabel = "<int>",
                descriptionKey = "param.eqo.w.expected")
        public int expectedSize;
    }

    public static class WpMethod {

        @Option(names = "--eqo-wp-lookahead",
                defaultValue = "2",
                paramLabel = "<int>",
                descriptionKey = "param.eqo.wp.lookahead")
        public int lookahead;

        @Option(names = "--eqo-wp-expected",
                defaultValue = "5",
                paramLabel = "<int>",
                descriptionKey = "param.eqo.wp.expected")
        public int expectedSize;
    }

}
