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

import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Option;

public class Symbols {

    @ArgGroup(headingKey = "param.symbol.reg.heading")
    public RegularSymbols regularSymbols;

    @ArgGroup(exclusive = false, headingKey = "param.symbol.cf.heading")
    public ContextFreeSymbols contextFreeSymbols;

    public static class RegularSymbols {

        @Option(names = {"-s", "--symbol"},
                required = true,
                paramLabel = "<sym>",
                descriptionKey = "param.symbol.reg.description")
        public List<String> symbols;
    }

    public static class ContextFreeSymbols {

        @Option(names = "--call",
                required = true,
                paramLabel = "<sym>",
                descriptionKey = "param.symbol.cf.call.description")
        public List<String> callSymbols;
        @Option(names = "--int",
                required = true,
                paramLabel = "<sym>",
                descriptionKey = "param.symbol.cf.int.description")
        public List<String> internalSymbols;
        @Option(names = "--ret",
                required = true,
                paramLabel = "<sym>",
                descriptionKey = "param.symbol.cf.ret.description")
        public List<String> returnSymbols;
    }

}
