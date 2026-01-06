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
import java.util.List;
import java.util.Objects;

import de.learnlib.oracle.SingleQueryOracle;
import net.automatalib.common.util.process.ProcessUtil;
import net.automatalib.word.Word;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.checker.nullness.qual.RequiresNonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An oracle that delegates its queries to an external program via the command-line interface. Acceptance of the queries
 * is determined based on the program's return code where {@code 0} indicates success and any other value indicates
 * failure.
 * <p>
 * Queries are translated to program arguments (via the symbol's {@link #toString()} method). Depending on whether a
 * {@code reset} symbol has been specified, this oracle assumes either a stateless ({@code reset == null}) or stateful
 * ({@code reset != null}) communication.
 * <p>
 * In a stateless communication, all symbols of a query are passed to the program at once and invocations should be
 * treated independently from each other. In a stateful communication, the program is executed multiple times with a
 * single query symbol each, preceded by a single invocation with only the {@code reset} symbol. The exit code of the
 * last invocation determines the query response.
 *
 * @param <I>
 *         input symbol type
 */
public class CLIOracle<I> implements SingleQueryOracle<I, Boolean> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CLIOracle.class);

    private final List<String> commandLine;
    private final @Nullable String reset;

    /**
     * Constructor. Does not set a {@code reset} symbol.
     *
     * @param commandLine
     *         the command line, containing the main binary and potential additional arguments
     *
     * @see #CLIOracle(List, String)
     */
    public CLIOracle(List<String> commandLine) {
        this(commandLine, null);
    }

    /**
     * Constructor.
     *
     * @param commandLine
     *         the command line, containing the main binary and potential additional arguments
     * @param reset
     *         the symbol passed to the program to indicate a reset
     */
    public CLIOracle(List<String> commandLine, @Nullable String reset) {
        this.commandLine = commandLine;
        this.reset = reset;
    }

    @Override
    public Boolean answerQuery(Word<I> prefix, Word<I> suffix) {
        return reset == null ? answerStatelessQuery(prefix, suffix) : answerStatefulQuery(prefix, suffix);
    }

    @SuppressWarnings("toarray.nullable.elements") // we make sure fo fill the remaining elements with non-null values
    private boolean answerStatelessQuery(Word<I> prefix, Word<I> suffix) {
        final String[] args = new String[this.commandLine.size() + prefix.size() + suffix.size()];
        int idx = this.commandLine.size();

        this.commandLine.toArray(args);

        for (I p : prefix) {
            args[idx++] = Objects.toString(p);
        }

        for (I s : suffix) {
            args[idx++] = Objects.toString(s);
        }

        try {
            return ProcessUtil.invokeProcess(args, LOGGER::debug, LOGGER::warn) == 0;
        } catch (IOException | InterruptedException e) {
            LOGGER.warn("Error while invoking process", e);
            return false;
        }
    }

    @RequiresNonNull("this.reset")
    private boolean answerStatefulQuery(Word<I> prefix, Word<I> suffix) {
        try {
            int returnCode = ProcessUtil.invokeProcess(toCommand(commandLine, reset), LOGGER::debug, LOGGER::warn);

            for (I p : prefix) {
                returnCode = ProcessUtil.invokeProcess(toCommand(commandLine, p), LOGGER::debug, LOGGER::warn);
            }

            for (I s : suffix) {
                returnCode = ProcessUtil.invokeProcess(toCommand(commandLine, s), LOGGER::debug, LOGGER::warn);
            }

            return returnCode == 0;
        } catch (IOException | InterruptedException e) {
            LOGGER.warn("Error while invoking process", e);
            return false;
        }
    }

    @SuppressWarnings("toarray.nullable.elements") // we make sure fo fill the remaining elements with non-null values
    static <T> String[] toCommand(List<String> args, T arg) {
        final String[] result = new String[args.size() + 1];

        args.toArray(result);
        result[args.size()] = Objects.toString(arg);

        return result;
    }
}
