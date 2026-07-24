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
import java.util.List;
import java.util.StringJoiner;
import java.util.function.Function;

import de.learnlib.oracle.AdaptiveMembershipOracle;
import de.learnlib.oracle.SingleAdaptiveMembershipOracle;
import de.learnlib.query.AdaptiveQuery;
import de.learnlib.query.AdaptiveQuery.Response;
import net.automatalib.common.util.process.ProcessUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An oracle that delegates its queries to an external program via the command-line interface. Outputs of the queries
 * are determined based on the provided output transformer.
 * <p>
 * Queries are translated to program arguments via the symbol's {@link Object#toString()} method. Due to the nature of
 * {@link AdaptiveMembershipOracle}s, communication is inherently stateful, i.e., the program is executed multiple times
 * with a single query symbol each, preceded by a single invocation with only the {@code reset} symbol.
 * <p>
 * The {@code outputTransformer} is used to transform responses of the individual invocations.
 *
 * @param <I>
 *         input symbol type
 * @param <O>
 *         output symbol type
 */
public class CLIOutputAdaptiveOracle<I, O> implements SingleAdaptiveMembershipOracle<I, O> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CLIOutputAdaptiveOracle.class);

    private final List<String> commandLine;
    private final Function<String, O> outputTransformer;
    private final String reset;

    /**
     * Constructor.
     *
     * @param commandLine
     *         the command line, containing the main binary and potential additional arguments
     * @param outputTransformer
     *         the transformer for the program's output. Receives the full (stdout) output of an individual invocation.
     * @param reset
     *         the symbol passed to the program to indicate a reset
     */
    public CLIOutputAdaptiveOracle(List<String> commandLine, Function<String, O> outputTransformer, String reset) {
        this.commandLine = commandLine;
        this.reset = reset;
        this.outputTransformer = outputTransformer;
    }

    @Override
    public void processQuery(AdaptiveQuery<I, O> query) {
        try {
            final String[] resetCommand = CLIOracle.toCommand(commandLine, reset);
            logInvocation(resetCommand);
            ProcessUtil.invokeProcess(resetCommand, LOGGER::debug, LOGGER::warn);

            Response response;

            do {
                // ProcessUtil calls the stdout consumer for every line, so replicate the newlines in the output
                final StringJoiner sj = new StringJoiner(System.lineSeparator());
                final I input = query.getInput();

                final String[] command = CLIOracle.toCommand(commandLine, input);

                logInvocation(command);
                ProcessUtil.invokeProcess(command, sj::add, LOGGER::warn);
                logResult(sj);

                final O output = outputTransformer.apply(sj.toString());
                response = query.processOutput(output);

                if (response == Response.RESET) {
                    ProcessUtil.invokeProcess(commandLine, new StringReader(reset), LOGGER::debug, LOGGER::warn);
                }
            } while (response != Response.FINISHED);
        } catch (IOException | InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

    private static void logInvocation(String[] command) {
        LOGGER.debug("Invoking '{}'", (Object) command);
    }

    private static void logResult(StringJoiner output) {
        LOGGER.debug("Received output '{}'", output);
    }
}
