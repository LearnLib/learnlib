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

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Arrays;

import de.learnlib.cli.Application;
import de.learnlib.cli.ApplicationIT;
import de.learnlib.cli.util.Util;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.alphabet.impl.DefaultProceduralInputAlphabet;
import net.automatalib.alphabet.impl.DefaultVPAlphabet;
import org.testng.Assert;
import org.testng.annotations.Test;
import picocli.CommandLine;
import picocli.CommandLine.ParameterException;

public class AlphabetFactoryTest {

    @Test
    public void testSymbolDefinitions() {
        final Application app = new Application();
        final CommandLine cmd = new CommandLine(app);
        cmd.setErr(new PrintWriter(OutputStream.nullOutputStream()));

        var regular = new String[] {ApplicationIT.STATELESS, "-sb", "-sa"};
        var procedural = new String[] {ApplicationIT.STATELESS, "--call=a", "--int=b1", "--int=b2", "--ret=c"};
        var pushdown = new String[] {ApplicationIT.STATELESS, "--call=a", "--int=b", "--ret=c2", "--ret=c1"};

        var regularOptions = Util.parseOptions(cmd, regular);
        Assert.assertEquals(Alphabets.fromArray("b", "a"), AlphabetFactory.getRegularAlphabet(regularOptions));

        var proceduralOptions = Util.parseOptions(cmd, procedural);
        Assert.assertEquals(new DefaultProceduralInputAlphabet<>(Alphabets.fromArray("b1", "b2"),
                                                                 Alphabets.singleton("a"),
                                                                 "c"),
                            AlphabetFactory.getProceduralAlphabet(proceduralOptions));

        var pushdownOptions = Util.parseOptions(cmd, pushdown);
        Assert.assertEquals(new DefaultVPAlphabet<>(Alphabets.singleton("b"),
                                                    Alphabets.singleton("a"),
                                                    Alphabets.fromArray("c2", "c1")),
                            AlphabetFactory.getVPAlphabet(pushdownOptions));

        var bad = new String[][] {// no symbols
                                  {ApplicationIT.STATELESS},
                                  // regular symbols on context-free learner
                                  {ApplicationIT.STATELESS, "-sa", "-tSBA"},
                                  // regular symbols on context-free learner
                                  {ApplicationIT.STATELESS, "-sa", "-tVPA"},
                                  // incomplete context-free symbols
                                  {ApplicationIT.STATELESS, "--call=a", "-tVPA"},
                                  // incomplete context-free symbols
                                  {ApplicationIT.STATELESS, "--int=a", "-tSPMM"},
                                  // incomplete context-free symbols
                                  {ApplicationIT.STATELESS, "--ret=a", "-tVPA"},
                                  // context-free symbols on regular learner
                                  {ApplicationIT.STATELESS, "--call=a", "--int=b", "--ret=c", "-tMEALY"},
                                  // multiple return symbols on a procedural learner
                                  {ApplicationIT.STATELESS, "--call=a", "--int=b", "--ret=c", "--ret=d", "-tSBA"}};

        for (String[] args : bad) {
            try {
                var options = Util.parseOptions(cmd, args);
                switch (options.type) {
                    case DFA:
                    case MEALY:
                    case NFA:
                        AlphabetFactory.getRegularAlphabet(options);
                    case SBA:
                    case SPA:
                    case SPMM:
                        AlphabetFactory.getProceduralAlphabet(options);
                    case VPA:
                        AlphabetFactory.getVPAlphabet(options);
                    default:
                        Assert.fail(Arrays.toString(args));
                }
            } catch (ParameterException | NullPointerException | IllegalArgumentException ignored) {
                // ignore
            }
        }
    }
}
