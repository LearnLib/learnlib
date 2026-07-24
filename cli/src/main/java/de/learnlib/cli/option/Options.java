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

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

public class Options {

    @Parameters(paramLabel = "<path>", descriptionKey = "option.sul", arity = "1..*")
    public List<File> sul;

    @Option(names = {"-t", "--type"}, defaultValue = "DFA", descriptionKey = "option.type")
    public Type type;

    @Option(names = {"-l", "--learner"}, defaultValue = "TTT", descriptionKey = "option.learner")
    public Learner learner;

    @Option(names = {"-r", "--reset"}, paramLabel = "<sym>", descriptionKey = "option.reset")
    public String reset;

    @Option(names = {"-d", "--delim"}, paramLabel = "<string>", defaultValue = "\n", descriptionKey = "option.delim")
    public String delimiter;

    @Option(names = {"-e", "--eqo"}, paramLabel = "<eqo>", defaultValue = "RANDOM_WP", descriptionKey = "option.eqo")
    public List<EQOracle> eqos;

    @Option(names = {"-c", "--cache"}, descriptionKey = "option.cache")
    public boolean cache;

    @Option(names = "--stats", descriptionKey = "option.stats")
    public boolean statistics;

    @Option(names = "--stdin", descriptionKey = "option.stdin")
    public boolean stdin;

    @Option(names = "--args", paramLabel = "<string>", descriptionKey = "option.args")
    public List<String> additionalArgs;

    @Option(names = {"-f", "--format"}, defaultValue = "DOT", descriptionKey = "option.format")
    public Output format;

    @Option(names = {"-o", "--output"}, paramLabel = "<path>", descriptionKey = "option.output")
    public Path output;

    @Option(names = {"-v", "--verbose"}, descriptionKey = "option.verbose")
    public boolean[] verbosity;

    @ArgGroup(multiplicity = "1")
    public Symbols symbols;

    @ArgGroup(validate = false, headingKey = "param.eqo.heading")
    public EQOParams eqoParams = new EQOParams();
}
