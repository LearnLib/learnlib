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
package de.learnlib.cli.factory;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Pattern;

import de.learnlib.cli.option.Options;
import de.learnlib.filter.cache.dfa.DFACaches;
import de.learnlib.filter.cache.mealy.MealyCaches;
import de.learnlib.filter.statistic.oracle.CounterAdaptiveQueryOracle;
import de.learnlib.filter.statistic.oracle.CounterOracle;
import de.learnlib.oracle.AdaptiveMembershipOracle;
import de.learnlib.oracle.MembershipOracle;
import de.learnlib.oracle.membership.CLIOracle;
import de.learnlib.oracle.membership.CLIOutputAdaptiveOracle;
import de.learnlib.oracle.membership.CLIOutputOracle;
import de.learnlib.oracle.membership.StdInOracle;
import de.learnlib.oracle.membership.StdInOutputAdaptiveOracle;
import de.learnlib.oracle.membership.StdInOutputOracle;
import de.learnlib.oracle.parallelism.ParallelOracleBuilders;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.word.Word;

@FunctionalInterface
public interface MQOFactory<OR, A> extends BiFunction<Options, A, OR> {

    MQOFactory<MembershipOracle<String, Boolean>, Alphabet<String>> ACCEPTOR = MQOFactory::getAcceptorOracle;
    MQOFactory<MembershipOracle<String, Word<String>>, Alphabet<String>> TRANSDUCER = MQOFactory::getTransducerOracle;
    MQOFactory<AdaptiveMembershipOracle<String, String>, Alphabet<String>> ADAPTIVE = MQOFactory::getAdaptiveOracle;

    private static MembershipOracle<String, Boolean> getAcceptorOracle(Options options, Alphabet<String> alphabet) {
        MembershipOracle<String, Boolean> oracle;

        if (options.sul.size() == 1) {
            oracle = buildSingleAcceptorOracle(options, options.sul.get(0));
        } else {
            final List<MembershipOracle<String, Boolean>> suls = new ArrayList<>(options.sul.size());
            for (File sul : options.sul) {
                suls.add(buildSingleAcceptorOracle(options, sul));
            }
            oracle = ParallelOracleBuilders.newStaticParallelOracle(suls).create();
        }

        if (options.statistics) {
            oracle = new CounterOracle<>(oracle, "sul");
        }

        if (options.cache) {
            oracle = DFACaches.createDAGCache(alphabet, oracle);
            if (options.statistics) {
                oracle = new CounterOracle<>(oracle, "cache");
            }
        }

        return oracle;
    }

    private static MembershipOracle<String, Boolean> buildSingleAcceptorOracle(Options options, File path) {
        if (options.stdin) {
            return new StdInOracle<>(buildCommandLine(options, path), options.reset);
        } else {
            return new CLIOracle<>(buildCommandLine(options, path), options.reset);
        }
    }

    private static MembershipOracle<String, Word<String>> getTransducerOracle(Options options,
                                                                              Alphabet<String> alphabet) {
        MembershipOracle<String, Word<String>> oracle;

        if (options.sul.size() == 1) {
            oracle = buildSingleTransducerOracle(options, options.sul.get(0));
        } else {
            final List<MembershipOracle<String, Word<String>>> suls = new ArrayList<>(options.sul.size());
            for (File sul : options.sul) {
                suls.add(buildSingleTransducerOracle(options, sul));
            }
            oracle = ParallelOracleBuilders.newStaticParallelOracle(suls).create();
        }

        if (options.statistics) {
            oracle = new CounterOracle<>(oracle, "sul");
        }

        if (options.cache) {
            oracle = MealyCaches.createDAGCache(alphabet, oracle);
            if (options.statistics) {
                oracle = new CounterOracle<>(oracle, "cache");
            }
        }

        return oracle;
    }

    private static MembershipOracle<String, Word<String>> buildSingleTransducerOracle(Options options, File path) {
        if (options.stdin) {
            return new StdInOutputOracle<>(buildCommandLine(options, path),
                                           new OutputTransformer(options),
                                           options.reset);
        } else {
            return new CLIOutputOracle<>(buildCommandLine(options, path),
                                         new OutputTransformer(options),
                                         options.reset);
        }
    }

    private static AdaptiveMembershipOracle<String, String> getAdaptiveOracle(Options options,
                                                                              Alphabet<String> alphabet) {
        verifyReset(options);

        AdaptiveMembershipOracle<String, String> oracle;

        if (options.sul.size() == 1) {
            oracle = buildSingleAdaptiveOracle(options, options.sul.get(0));
        } else {
            final List<AdaptiveMembershipOracle<String, String>> suls = new ArrayList<>(options.sul.size());
            for (File sul : options.sul) {
                suls.add(buildSingleAdaptiveOracle(options, sul));
            }
            oracle = ParallelOracleBuilders.newStaticParallelAdaptiveOracle(suls).create();
        }

        if (options.statistics) {
            oracle = new CounterAdaptiveQueryOracle<>(oracle, "sul");
        }

        if (options.cache) {
            oracle = MealyCaches.createAdaptiveQueryCache(alphabet, oracle);
            if (options.statistics) {
                oracle = new CounterAdaptiveQueryOracle<>(oracle, "cache");
            }
        }

        return oracle;
    }

    private static AdaptiveMembershipOracle<String, String> buildSingleAdaptiveOracle(Options options, File path) {
        if (options.stdin) {
            return new StdInOutputAdaptiveOracle<>(buildCommandLine(options, path), Function.identity(), options.reset);
        } else {
            return new CLIOutputAdaptiveOracle<>(buildCommandLine(options, path), Function.identity(), options.reset);
        }
    }

    private static List<String> buildCommandLine(Options options, File file) {
        verifyPath(file);
        final String absolutePath = file.getAbsolutePath();
        if (options.additionalArgs == null) {
            return Collections.singletonList(absolutePath);
        } else {
            final List<String> cmd = new ArrayList<>(options.additionalArgs.size() + 1);
            cmd.add(absolutePath);
            cmd.addAll(options.additionalArgs);
            return cmd;
        }
    }

    private static void verifyPath(File file) {
        if (!file.exists()) {
            throw new IllegalArgumentException(String.format("Specified SUL '%s' does not exist", file));
        }
    }

    private static void verifyReset(Options options) {
        if (options.reset == null) {
            throw new IllegalArgumentException(String.format(
                    "Learner '%s' requires a stateful oracle. Provide a --reset",
                    options.learner));
        }
    }

    class OutputTransformer implements BiFunction<String, Integer, Word<String>> {

        private final Pattern pattern;

        public OutputTransformer(Options options) {
            this.pattern = Pattern.compile(options.delimiter);
        }

        @Override
        public Word<String> apply(String string, Integer suffixLength) {
            if (string.isEmpty()) {
                return Word.epsilon();
            }
            final String[] orig = pattern.split(string);
            return Word.fromArray(orig, orig.length - suffixLength, suffixLength);
        }
    }
}
