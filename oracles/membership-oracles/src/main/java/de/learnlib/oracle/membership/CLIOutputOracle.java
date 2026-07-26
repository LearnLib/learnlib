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
import java.util.StringJoiner;

import de.learnlib.oracle.SingleQueryOracle;
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
 * Queries are translated to program arguments via the symbol's {@link Object#toString()} method. Depending on whether a
 * {@code reset} symbol has been specified, this oracle assumes either a stateless ({@code reset == null}) or stateful
 * ({@code reset != null}) communication.
 * <p>
 * In a stateless communication, all symbols of a query are passed to the program at once and invocations should be
 * treated independently of each other. In a stateful communication, the program is executed multiple times with a
 * single query symbol each, preceded by a single invocation with only the {@code reset} symbol.
 * <p>
 * With stateless communication, the {@code outputTransformer} receives the full program's output at once. With stateful
 * communication, the individual invocation's outputs are concatenated via {@link System#lineSeparator()} before being
 * passed to the transformer at once.
 *
 * @param <I>
 *         input symbol type
 * @param <D>
 *         output domain type
 */
public class CLIOutputOracle<I, D> implements SingleQueryOracle<I, D> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CLIOutputOracle.class);

    private final List<String> commandLine;
    private final OutputTransformer<D> outputTransformer;
    private final @Nullable String reset;

    /**
     * Constructor. Does not set a {@code reset} symbol.
     *
     * @param commandLine
     *         the command line, containing the main binary and potential additional arguments
     * @param outputTransformer
     *         the transformer for the program's output. Receives the full (stdout) output as well as the length of the
     *         query prefix and suffix for properly offsetting potentially {@link Word}-based output types.
     *
     * @see #CLIOutputOracle(List, OutputTransformer, String)
     */
    public CLIOutputOracle(List<String> commandLine, OutputTransformer<D> outputTransformer) {
        this(commandLine, outputTransformer, null);
    }

    /**
     * Constructor.
     *
     * @param commandLine
     *         the command line, containing the main binary and potential additional arguments
     * @param outputTransformer
     *         the transformer for the program's output. Receives the full (stdout) output as well as the length of the
     *         query prefix and suffix for properly offsetting potentially {@link Word}-based output types.
     * @param reset
     *         the symbol passed to the program to indicate a reset
     */
    public CLIOutputOracle(List<String> commandLine, OutputTransformer<D> outputTransformer, @Nullable String reset) {
        this.commandLine = commandLine;
        this.reset = reset;
        this.outputTransformer = outputTransformer;
    }

    @Override
    public D answerQuery(Word<I> prefix, Word<I> suffix) {
        return reset == null ? answerStatelessQuery(prefix, suffix) : answerStatefulQuery(prefix, suffix);
    }

    @SuppressWarnings("toarray.nullable.elements") // we make sure fo fill the remaining elements with non-null values
    private D answerStatelessQuery(Word<I> prefix, Word<I> suffix) {
        final String[] args = new String[this.commandLine.size() + prefix.size() + suffix.size()];
        int idx = this.commandLine.size();

        this.commandLine.toArray(args);

        for (I p : prefix) {
            args[idx++] = Objects.toString(p);
        }

        for (I s : suffix) {
            args[idx++] = Objects.toString(s);
        }

        // ProcessUtil calls the stdout consumer for every line, so replicate the newlines in the output
        final StringJoiner sj = new StringJoiner(System.lineSeparator());

        try {
            logInvocation(args);
            ProcessUtil.invokeProcess(args, sj::add, LOGGER::warn);
            logResult(sj);
            return outputTransformer.transform(sj.toString(), prefix.length(), suffix.length());
        } catch (IOException | InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

    @RequiresNonNull("this.reset")
    private D answerStatefulQuery(Word<I> prefix, Word<I> suffix) {
        // ProcessUtil calls the stdout consumer for every line, so replicate the newlines in the output
        final StringJoiner sj = new StringJoiner(System.lineSeparator());

        try {
            final String[] resetCommand = CLIOracle.toCommand(commandLine, reset);
            logInvocation(resetCommand);
            ProcessUtil.invokeProcess(resetCommand, LOGGER::debug, LOGGER::warn);

            for (I p : prefix) {
                answerStatefulSymbol(p, sj);
            }

            for (I s : suffix) {
                answerStatefulSymbol(s, sj);
            }

            return outputTransformer.transform(sj.toString(), prefix.length(), suffix.length());
        } catch (IOException | InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

    private void answerStatefulSymbol(I i, StringJoiner sj) throws IOException, InterruptedException {
        String[] command = CLIOracle.toCommand(commandLine, i);
        logInvocation(command);
        ProcessUtil.invokeProcess(command, sj::add, LOGGER::warn);
        logResult(sj);
    }

    private static void logInvocation(String[] command) {
        LOGGER.debug("Invoking '{}'", (Object) command);
    }

    private static void logResult(Object output) {
        LOGGER.debug("Received output '{}'", output);
    }

    /**
     * Transformer for converting the {@link String}-based output of a CLI application to a custom-typed output.
     *
     * @param <D>
     *         output domain type
     */
    @FunctionalInterface
    public interface OutputTransformer<D> {

        /**
         * Transforms the provided output to a custom output object. Additionally, receives information about the length
         * of the original query's prefix and suffix (e.g., for {@link Word}-based outputs).
         *
         * @param output
         *         the stdout output of the invocation
         * @param prefixLength
         *         the length of the query prefix
         * @param suffixLength
         *         the length of the query suffix
         *
         * @return the output
         */
        D transform(String output, int prefixLength, int suffixLength);
    }
}
