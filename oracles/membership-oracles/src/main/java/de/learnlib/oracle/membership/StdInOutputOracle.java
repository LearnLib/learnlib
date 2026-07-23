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
package de.learnlib.oracle.membership;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.BiFunction;

import de.learnlib.oracle.SingleQueryOracle;
import net.automatalib.common.setting.AutomataLibProperty;
import net.automatalib.common.setting.AutomataLibSettings;
import net.automatalib.common.util.process.ProcessUtil;
import net.automatalib.word.Word;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.checker.nullness.qual.RequiresNonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An oracle that delegates its queries to an external program via the command-line interface. Outputs of the queries
 * are determined based on the provided output transformer.
 * <p>
 * Queries are passed to the program's stdin stream (via the queries' {@link Word#toString()} method. You may adjust
 * formatting via the available properties in AutomataLib's settings). Depending on whether a {@code reset} symbol has
 * been specified, this oracle assumes either a stateless ({@code reset == null}) or stateful ({@code reset != null})
 * communication.
 * <p>
 * In a stateless communication, all symbols of a query are passed to the program at once and invocations should be
 * treated independently from each other. In a stateful communication, the program is executed multiple times with a
 * single query symbol each, preceded by a single invocation with only the {@code reset} symbol. The exit code of the
 * last invocation determines the query response.
 *
 * @param <I>
 *         input symbol type
 * @param <D>
 *         output domain type
 */
public class StdInOutputOracle<I, D> implements SingleQueryOracle<I, D> {

    private static final Logger LOGGER = LoggerFactory.getLogger(StdInOutputOracle.class);
    private static final String DELIM =
            AutomataLibSettings.getInstance().getProperty(AutomataLibProperty.WORD_SYMBOL_SEPARATOR, " ");

    private final List<String> commandLine;
    private final BiFunction<List<String>, Integer, D> outputTransformer;
    private final @Nullable String reset;

    /**
     * Constructor. Does not set a {@code reset} symbol.
     *
     * @param commandLine
     *         the command line, containing the main binary and potential additional arguments
     * @param outputTransformer
     *         the transformer for the program's output. Receives the full (stdout) output as well as the length of the
     *         query prefix for properly offsetting potentially {@link Word}-based output types.
     *
     * @see #StdInOutputOracle(List, BiFunction, String)
     */
    public StdInOutputOracle(List<String> commandLine, BiFunction<List<String>, Integer, D> outputTransformer) {
        this(commandLine, outputTransformer, null);
    }

    /**
     * Constructor.
     *
     * @param commandLine
     *         the command line, containing the main binary and potential additional arguments
     * @param outputTransformer
     *         the transformer for the program's output. Receives the full (stdout) output as well as the length of the
     *         query prefix for properly offsetting potentially {@link Word}-based output types.
     * @param reset
     *         the symbol passed to the program to indicate a reset
     */
    public StdInOutputOracle(List<String> commandLine,
                             BiFunction<List<String>, Integer, D> outputTransformer,
                             String reset) {
        this.commandLine = commandLine;
        this.outputTransformer = outputTransformer;
        this.reset = reset;
    }

    @Override
    public D answerQuery(Word<I> prefix, Word<I> suffix) {
        return reset == null ? answerStatelessQuery(prefix, suffix) : answerStatefulQuery(prefix, suffix);
    }

    private D answerStatelessQuery(Word<I> prefix, Word<I> suffix) {
        final StringJoiner input = new StringJoiner(DELIM);

        for (I p : prefix) {
            input.add(String.valueOf(p));
        }

        for (I s : suffix) {
            input.add(String.valueOf(s));
        }

        try {
            final List<String> output = new ArrayList<>();

            logInvocation(commandLine, input);
            ProcessUtil.invokeProcess(commandLine, new StringReader(input.toString()), output::add, LOGGER::warn);
            logResult(output);

            return outputTransformer.apply(output, prefix.length());
        } catch (IOException | InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

    @RequiresNonNull("this.reset")
    private D answerStatefulQuery(Word<I> prefix, Word<I> suffix) {
        final List<String> output = new ArrayList<>();

        try {
            logInvocation(commandLine, reset);
            ProcessUtil.invokeProcess(commandLine, new StringReader(reset), LOGGER::debug, LOGGER::warn);

            for (I p : prefix) {
                answerStatefulSymbol(p, output);
            }

            for (I s : suffix) {
                answerStatefulSymbol(s, output);
            }

            return outputTransformer.apply(output, prefix.length());
        } catch (IOException | InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

    private void answerStatefulSymbol(I i, List<String> output) throws IOException, InterruptedException {
        // ProcessUtil calls the stdout consumer for every line, so replicate the newlines in the output
        final StringJoiner sj = new StringJoiner(System.lineSeparator());
        logInvocation(commandLine, i);
        ProcessUtil.invokeProcess(commandLine, new StringReader(String.valueOf(i)), sj::add, LOGGER::warn);
        logResult(sj);
        output.add(sj.toString());
    }

    private static void logInvocation(List<String> command, Object payload) {
        LOGGER.debug("Invoking '{}' with payload '{}'", command, payload);
    }

    private static void logResult(Object output) {
        LOGGER.debug("Received output '{}'", output);
    }
}
