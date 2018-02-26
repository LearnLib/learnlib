/* Copyright (C) 2013-2018 TU Dortmund
 * This file is part of LearnLib, http://www.learnlib.de/.
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
package de.learnlib.modelchecking.modelchecker;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import de.learnlib.api.exception.ModelCheckingException;
import de.learnlib.api.modelchecking.counterexample.Lasso;
import de.learnlib.api.modelchecking.modelchecker.ModelChecker;
import de.learnlib.setting.LearnLibSettings;
import net.automatalib.automata.concepts.Output;
import net.automatalib.serialization.etf.writer.AbstractETFWriter;
import net.automatalib.serialization.fsm.parser.AbstractFSMParser;
import net.automatalib.serialization.fsm.parser.FSMParseException;
import net.automatalib.ts.simple.SimpleDTS;

/**
 * An LTL model checker using LTSmin.
 *
 * The user must install LTSmin in order for {@link AbstractLTSminLTL} to run without exceptions. Once LTSmin is installed
 * the user may specify the path to the installed LTSmin binaries with the property
 * <b>learnlib.external.ltsmin.path</b>. If this property is not set the binaries will be run as usual (e.g. simply
 * by invoking etf2lts-mc, and ltsmin-convert), which means the user can also specify the location of the binaries
 * in the PATH environment variable.
 *
 * This model checker is implemented as follows. The hypothesis automaton is first written to an LTS in ETF
 * {@link AbstractETFWriter} file, which serves as input for the etf2lts-mc binary.
 * Then the etf2lts-mc binary is run, which will write an LTS in GCF format. This LTS will be a subset of the
 * language of the given hypothesis. Next, the GCF is converted to FSM using the ltsmin-convert binary. Lastly, the
 * FSM is read back into an automaton using an {@link AbstractFSMParser}.
 *
 * @author Jeroen Meijer
 *
 * @see <a href="http://ltsmin.utwente.nl">http://ltsmin.utwente.nl</a>
 * @see AbstractFSMParser
 * @see AbstractETFWriter
 * @see LearnLibSettings
 *
 * @param <I> the input type.
 * @param <A> the output type.
 * @param <L> the Lasso type.
 */
public abstract class AbstractLTSminLTL<I,
                                A extends SimpleDTS<?, I> & Output<I, ?>,
                                L extends Lasso<?, ? extends A, I, ?>>
        extends AbstractUnfoldingModelChecker<I, A, String, L> implements ModelChecker<I, A, String, L> {

    /**
     * @see #isKeepFiles()
     */
    private final boolean keepFiles;

    /**
     * @see #getString2Input()
     */
    private final Function<String, I> string2Input;

    /**
     * Whether or not we made sure the LTSmin binaries can be run.
     */
    private static boolean binariesChecked;

    /**
     * Constructs a new AbstractLTSminLTL.
     *
     * @param keepFiles whether to keep intermediate files, (e.g. etfs, gcfs etc.).
     * @param string2Input a function that transforms edges in FSM files to actual input.
     * @param minimumUnfolds the minimum number of unfolds.
     * @param multiplier the multiplier
     * @throws ModelCheckingException when the LTSmin binaries can not be run successfully.
     */
    protected AbstractLTSminLTL(boolean keepFiles,
                                Function<String, I> string2Input,
                                int minimumUnfolds,
                                double multiplier) throws ModelCheckingException {
        super(minimumUnfolds, multiplier);
        this.keepFiles = keepFiles;
        this.string2Input = string2Input;

        if (!binariesChecked) {
            if (!LTSminUtil.checkUsable()) {
                throw new ModelCheckingException("LTSmin binary could not be executed correctly");
            }
            binariesChecked = true;
        }
    }

    /**
     * Returns whether intermediate files should be kept, e.g. etfs, gcfs, etc.
     *
     * @return the boolean
     */
    protected boolean isKeepFiles() {
        return keepFiles;
    }

    /**
     * Returns the function that transforms edges in FSM files to actual input.
     *
     * @return the Function.
     */
    public Function<String, I> getString2Input() {
        return string2Input;
    }

    /**
     * Writes the given {@code automaton} to the given {@code etf} file.
     *
     * @param automaton the automaton to write.
     * @param inputs the alphabet.
     * @param etf the file to write to.
     *
     * @throws IOException when the given {@code automaton} can not be written to {@code etf}.
     */
    protected abstract void automaton2ETF(A automaton, Collection<? extends I> inputs, File etf) throws IOException;

    /**
     * Reads the {@code fsm} and converts it to a {@link Lasso}.
     *
     * @param fsm the FSM to read.
     * @param automaton the automaton that was used as a hypothesis.
     *
     * @return the {@link Lasso}.
     *
     * @throws IOException when {@code fsm} can not be read correctly.
     * @throws FSMParseException when the FSM definition in {@code fsm} is invalid.
     */
    protected abstract L fsm2Lasso(File fsm, A automaton) throws IOException, FSMParseException;

    /**
     * Finds a counterexample for the given {@code formula}, and given {@code hypothesis}.
     *
     * @see AbstractLTSminLTL
     */
    @Override
    public final L findCounterExample(A hypothesis, Collection<? extends I> inputs, String formula) throws ModelCheckingException {

        final File etf, gcf;
        try {
            // create the ETF that will contain the LTS of the hypothesis
            etf = File.createTempFile("automaton2etf", ".etf");

            // create the GCF that will possibly contain the counterexample
            gcf = File.createTempFile("etf2gcf", ".gcf");

            // write to the ETF file
            automaton2ETF(hypothesis, inputs, etf);

        } catch (IOException ioe) {
            throw new ModelCheckingException(ioe);
        }

        // the command lines for the ProcessBuilder
        final List<String> commandLines = new ArrayList<>();

        // add the etf2lts-mc binary
        commandLines.add(LTSminUtil.ETF2LTS_MC);

        // add the ETF file that contains the LTS of the hypothesis
        commandLines.add(etf.getAbsolutePath());

        // add the LTL formula
        commandLines.add("--ltl=" + formula);

        // use Buchi automata created by spot
        commandLines.add("--buchi-type=spotba");

        // use the Union-Find strategy
        commandLines.add("--strategy=ufscc");

        // write the lasso to this file
        commandLines.add("--trace=" + gcf.getAbsolutePath());

        // use only one thread (hypotheses are always small)
        commandLines.add("--threads=1");

        // use LTSmin LTL semantics
        commandLines.add("--ltl-semantics=ltsmin");

        // do not abort on partial LTSs
        commandLines.add("--allow-undefined-edges");

        final Process ltsmin;
        try {
            // run the etf2lts-mc binary
            ProcessBuilder processBuilder = new ProcessBuilder(commandLines);
            ltsmin = processBuilder.start();
            ltsmin.waitFor();
        } catch (IOException | InterruptedException e) {
            throw new ModelCheckingException(e);
        }

        // check if we need to delete the ETF
        if (!keepFiles && !etf.delete()) {
            throw new ModelCheckingException("Could not delete file: " + etf.getAbsolutePath());
        }

        final L result;

        if (ltsmin.exitValue() == 1) {
            // we have found a counterexample
            commandLines.clear();

            final File fsm;
            try {
                // create a file for the FSM
                fsm = File.createTempFile("gcf2fsm", ".fsm");
            } catch (IOException ioe) {
                throw new ModelCheckingException(ioe);
            }

            // add the ltsmin-convert binary
            commandLines.add(LTSminUtil.LTSMIN_CONVERT);

            // use the GCF as input
            commandLines.add(gcf.getAbsolutePath());

            // use the FSM as output
            commandLines.add(fsm.getAbsolutePath());

            // required option
            commandLines.add("--rdwr");

            final Process convert;
            try {
                // convert the GCF to FSM
                ProcessBuilder processBuilder = new ProcessBuilder(commandLines);
                convert = processBuilder.start();
                convert.waitFor();
            } catch (IOException | InterruptedException e) {
                throw new ModelCheckingException(e);
            }

            // check the conversion is successful
            if (convert.exitValue() != 0) {
                throw new ModelCheckingException("Could not convert gcf to fsm");
            }

            try {
                // convert the FSM to a Lasso
                result = fsm2Lasso(fsm, hypothesis);

                // check if we must keep the FSM file
                if (!keepFiles && !fsm.delete()) {
                    throw new ModelCheckingException("Could not delete file: " + fsm.getAbsolutePath());
                }
            } catch (IOException | FSMParseException e) {
                throw new ModelCheckingException(e);
            }
        } else {
            result = null;
        }

        // check if we must keep the GCF
        if (!keepFiles && !gcf.delete()) {
            throw new ModelCheckingException("Could not delete file: " + gcf.getAbsolutePath());
        }

        return result;
    }

    public static class BuilderDefaults {

        public static boolean keepFiles() {
            return false;
        }

        public static int minimumUnfolds() {
            return 3; // super arbitrary number
        }

        public static double multiplier() {
            return 1.0; // quite arbitrary too
        }
    }
}
