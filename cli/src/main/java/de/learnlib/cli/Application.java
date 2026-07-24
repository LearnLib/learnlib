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
package de.learnlib.cli;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import de.learnlib.cli.option.Options;
import de.learnlib.cli.util.VersionProvider;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

@Command(name = "learnlib",
         mixinStandardHelpOptions = true,
         versionProvider = VersionProvider.class,
         resourceBundle = Application.PROPERTIES,
         showDefaultValues = true,
         showAtFileInUsageHelp = true,
         descriptionHeading = "%nDescription:%n%n",
         parameterListHeading = "%nParameters:%n",
         optionListHeading = "%nOptions:%n",
         description = "Runs an active automata learning process by invoking the provided SUL to answer membership queries. For learning acceptor-based formalisms, the tool will use the exitcode of the binary to determine acceptance where an exitcode of 0 equals 'accept' and an exitcode unequal to 0 equals 'reject'. For learning transduction-based formalisms, the tool expects the SUL to emit an output that is transformed into individual symbols using the provided 'delimiter'. For additional information on the involved components of LearnLib, you may use the documentation available at https://learnlib.de/learnlib/maven-site/.")
public class Application implements Runnable {

    public static final String PROPERTIES = "application";

    @Mixin
    private Options options;

    public static void main(String[] args) {
        CommandLine commandLine = new CommandLine(new Application());
        commandLine.setCaseInsensitiveEnumValuesAllowed(true);
        System.exit(commandLine.execute(args));
    }

    @Override
    public void run() {
        setLogLevel(options);
        options.type.runner(options).run(options);
    }

    private void setLogLevel(Options options) {
        if (options.verbosity != null) {
            final Logger root = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);

            final Level level = switch (options.verbosity.length) {
                case 0 -> Level.INFO;
                case 1 -> Level.DEBUG;
                default -> Level.TRACE;
            };

            root.setLevel(level);
        }
    }

}

