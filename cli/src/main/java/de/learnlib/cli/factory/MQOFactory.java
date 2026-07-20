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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Pattern;

import de.learnlib.cli.option.Options;
import de.learnlib.filter.cache.dfa.DFACaches;
import de.learnlib.filter.cache.mealy.MealyCaches;
import de.learnlib.filter.statistic.oracle.CounterOracle;
import de.learnlib.oracle.MembershipOracle;
import de.learnlib.oracle.membership.CLIOracle;
import de.learnlib.oracle.membership.CLIOutputOracle;
import de.learnlib.oracle.membership.StdInOracle;
import de.learnlib.oracle.membership.StdInOutputOracle;
import net.automatalib.word.Word;

@FunctionalInterface
public interface MQOFactory<D> extends Function<Options, MembershipOracle<String, D>> {

    MQOFactory<Boolean> ACCEPTOR = MQOFactory::getAcceptorOracle;
    MQOFactory<Word<String>> TRANSDUCER = MQOFactory::getTransducerOracle;

    private static MembershipOracle<String, Boolean> getAcceptorOracle(Options options) {
        verifyPath(options);

        MembershipOracle<String, Boolean> oracle;

        if (options.stdin) {
            oracle = new StdInOracle<>(buildCommandLine(options), options.reset);
        } else {
            oracle = new CLIOracle<>(buildCommandLine(options), options.reset);

        }

        if (options.statistics) {
            oracle = new CounterOracle<>(oracle, "sul");
        }

        if (options.cache) {
            oracle = DFACaches.createHashCache(oracle);
            if (options.statistics) {
                oracle = new CounterOracle<>(oracle, "cache");
            }
        }

        return oracle;
    }

    private static MembershipOracle<String, Word<String>> getTransducerOracle(Options options) {
        verifyPath(options);

        MembershipOracle<String, Word<String>> oracle;

        if (options.stdin) {
            oracle = new StdInOutputOracle<>(buildCommandLine(options), new OutputTransformer(options), options.reset);
        } else {
            oracle = new CLIOutputOracle<>(buildCommandLine(options), new OutputTransformer(options), options.reset);

        }

        if (options.statistics) {
            oracle = new CounterOracle<>(oracle, "sul");
        }

        if (options.cache) {
            oracle = MealyCaches.createDynamicTreeCache(oracle);
            if (options.statistics) {
                oracle = new CounterOracle<>(oracle, "cache");
            }
        }

        return oracle;
    }

    private static List<String> buildCommandLine(Options options) {
        final String absolutePath = options.sul.getAbsolutePath();
        if (options.additionalArgs == null) {
            return Collections.singletonList(absolutePath);
        } else {
            final List<String> cmd = new ArrayList<>(options.additionalArgs.size() + 1);
            cmd.add(absolutePath);
            cmd.addAll(options.additionalArgs);
            return cmd;
        }
    }

    private static void verifyPath(Options options) {
        if (!options.sul.exists()) {
            throw new IllegalArgumentException(String.format("Specified SUL '%s' does not exist", options.sul));
        }
    }

    class OutputTransformer implements BiFunction<String, Integer, Word<String>> {

        private final Pattern pattern;

        public OutputTransformer(Options options) {
            this.pattern = Pattern.compile(options.delimiter);
        }

        @Override
        public Word<String> apply(String string, Integer offset) {
            if (string.isEmpty()) {
                return Word.epsilon();
            }
            final String[] orig = pattern.split(string);
            return Word.fromArray(orig, offset, orig.length - offset);
        }
    }
}
