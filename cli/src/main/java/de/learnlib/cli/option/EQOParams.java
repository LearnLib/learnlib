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

        @Option(names = "--eqo-kways-k", defaultValue = "2", paramLabel = "<int>", descriptionKey = "param.eqo.kways.k")
        public int k;

        @Option(names = "--eqo-kways-randomWalkLen",
                defaultValue = "20",
                paramLabel = "<int>",
                descriptionKey = "param.eqo.kways.randomWalkLen")
        public int randomWalkLen;

        @Option(names = "--eqo-kways-combinationMethod",
                defaultValue = "PERMUTATIONS",
                paramLabel = "<method>",
                descriptionKey = "param.eqo.kways.combinationMethod")
        public CombinationMethod combinationMethod;

        @Option(names = "--eqo-kways-seed",
                defaultValue = "42",
                paramLabel = "<int>",
                descriptionKey = "param.eqo.kways.seed")
        public int seed;
    }

    public static class KWayTransitionMethod {

        @Option(names = "--eqo-kwayt-k", defaultValue = "2", paramLabel = "<int>", descriptionKey = "param.eqo.kwayt.k")
        public int k;

        @Option(names = "--eqo-kwayt-randomWalkLen",
                defaultValue = "10",
                paramLabel = "<int>",
                descriptionKey = "param.eqo.kwayt.randomWalkLen")
        public int randomWalkLen;

        @Option(names = "--eqo-kwayt-numGeneratePaths",
                defaultValue = "100",
                paramLabel = "<int>",
                descriptionKey = "param.eqo.kwayt.numGeneratePaths")
        public int numGeneratePaths;

        @Option(names = "--eqo-kwayt-maxPathLen",
                defaultValue = "50",
                paramLabel = "<int>",
                descriptionKey = "param.eqo.kwayt.maxPathLen")
        public int maxPathLen;

        @Option(names = "--eqo-kwayt-maxNumberOfSteps",
                defaultValue = "100",
                paramLabel = "<int>",
                descriptionKey = "param.eqo.kwayt.maxNumberOfSteps")
        public int maxNumberOfSteps;

        @Option(names = "--eqo-kwayt-optimizationMetric",
                defaultValue = "STEPS",
                paramLabel = "<metric>",
                descriptionKey = "param.eqo.kwayt.optimizationMetric")
        public OptimizationMetric optimizationMetric;

        @Option(names = "--eqo-kwayt-generationMethod",
                defaultValue = "RANDOM",
                paramLabel = "<method>",
                descriptionKey = "param.eqo.kwayt.generationMethod")
        public GenerationMethod generationMethod;

        @Option(names = "--eqo-kwayt-seed",
                defaultValue = "42",
                paramLabel = "<int>",
                descriptionKey = "param.eqo.kwayt.seed")
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

        @Option(names = "--eqo-randomw-minimalSize",
                defaultValue = "0",
                paramLabel = "<int>",
                descriptionKey = "param.eqo.randomw.minimalSize")
        public int minimalSize;

        @Option(names = "--eqo-randomw-rndLength",
                defaultValue = "5",
                paramLabel = "<int>",
                descriptionKey = "param.eqo.randomw.rndLength")
        public int rndLength;

        @Option(names = "--eqo-randomw-bound",
                defaultValue = "100",
                paramLabel = "<int>",
                descriptionKey = "param.eqo.randomw.bound")
        public int bound;

        @Option(names = "--eqo-randomw-seed",
                defaultValue = "42",
                paramLabel = "<int>",
                descriptionKey = "param.eqo.randomw.seed")
        public int seed;
    }

    public static class RandomWpMethod {

        @Option(names = "--eqo-randomwp-minimalSize",
                defaultValue = "0",
                paramLabel = "<int>",
                descriptionKey = "param.eqo.randomwp.minimalSize")
        public int minimalSize;

        @Option(names = "--eqo-randomwp-rndLength",
                defaultValue = "5",
                paramLabel = "<int>",
                descriptionKey = "param.eqo.randomwp.rndLength")
        public int rndLength;

        @Option(names = "--eqo-randomwp-bound",
                defaultValue = "100",
                paramLabel = "<int>",
                descriptionKey = "param.eqo.randomwp.bound")
        public int bound;

        @Option(names = "--eqo-randomwp-seed",
                defaultValue = "42",
                paramLabel = "<int>",
                descriptionKey = "param.eqo.randomwp.seed")
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
