/* Copyright (C) 2013-2025 TU Dortmund University
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
package de.learnlib.testsupport.example.mmlt;

import java.io.IOException;
import java.io.InputStream;

import de.learnlib.testsupport.example.LearningExample.MMLTLearningExample;
import de.learnlib.time.MMLTModelParams;
import net.automatalib.automaton.mmlt.MMLT;
import net.automatalib.automaton.mmlt.impl.CompactMMLT;
import net.automatalib.automaton.mmlt.impl.StringSymbolCombiner;
import net.automatalib.exception.FormatException;
import net.automatalib.serialization.dot.DOTInputModelData;
import net.automatalib.serialization.dot.DOTInputModelDeserializer;
import net.automatalib.serialization.dot.DOTParsers;
import net.automatalib.util.automaton.mmlt.MMLTs;

/**
 * A collection of {@link MMLT}-based learning examples.
 */
public final class MMLTExamples {

    private MMLTExamples() {
        // prevent instantiation
    }

    /**
     * Returns an MMLT example of an HVAC system.
     * <p>
     * The system has been adapted from: Taylor and Taylor: Patterns in the Machine
     *
     * @return a learning example for the specified machine
     */
    public static MMLTLearningExample<String, String> hvac() {
        return new Example("HVAC");
    }

    /**
     * Returns an MMLT example of an endpoint in the stream control and transmission protocol.
     * <p>
     * The model has been adapted from: Stewart et al.: Stream Control Transmission Protocol (RFC 9260, Figure 3)
     *
     * @return a learning example for the specified machine
     */
    public static MMLTLearningExample<String, String> sctp() {
        return new Example("SCTP");
    }

    /**
     * Returns an MMLT example of a sensor collector.
     * <p>
     * The sensor measures particulate matter and ambient noise. The measurement program automatically ends after some
     * time. The program may be restarted at any time. Alternatively, a self-check program can be entered. This also
     * ends after some time and may be aborted. At the end of either program, the collected data may be retrieved.
     *
     * @return a learning example for the specified machine
     */
    public static MMLTLearningExample<String, String> sensorCollector() {
        return new Example("sensor_collector");
    }

    /**
     * Returns an MMLT example of a washing machine.
     * <p>
     * The machine is initially off. After powering it on and closing the door, the user can start either the short or
     * the normal program. An open door prevents starting and triggers a warning. Not choosing a program within 10
     * seconds turns the machine off.
     * <p>
     * In normal model, the machine fills the drum, heats the water, and starts the main wash. During this wash, it
     * regularly adjusts the drum speed and maintains temperature. After 2 hours, the water is drained and the drum is
     * spun at full speed for some time. Afterwards the remaining water is drained. The short program makes less
     * adjustments, so that a wash ends after 1 hour.
     * <p>
     * Both programs are interrupted when a leak is detected. Normal mode may also be interrupted by "stop". This drains
     * the drum immediately. Once done, the door is unlocked, a message is shown, and the machine beeps repeatedly until
     * the user presses any button or opens the door.
     *
     * @return a learning example for the specified machine
     */
    public static MMLTLearningExample<String, String> wm() {
        return new Example("WM");
    }

    /**
     * Returns an MMLT example of an oven with a time-controlled baking program.
     * <p>
     * After powering the oven on, the oven remains idle until the program is started. During the program, the oven
     * regularly measures and adjusts the temperature. At the end of the program, an alarm sounds. Then, the user may
     * extend the program. If not extended, the program ends either when the user opens the door, presses a button, or a
     * timeout occurs.
     *
     * @return a learning example for the specified machine
     */
    public static MMLTLearningExample<String, String> oven() {
        return new Example("Oven");
    }

    /**
     * Returns an MMLT example of a wireless sensor node.
     * <p>
     * The node regularly collects and transmits data. If the battery is low, no data is transmitted. Then, a user may
     * collect the data manually. The node can be shut down at any time. If the battery is empty, it is shut down
     * automatically.
     *
     * @return a learning example for the specified machine
     */
    public static MMLTLearningExample<String, String> wsn() {
        return new Example("WSN");
    }

    private static final class Example implements MMLTLearningExample<String, String> {

        private static final int SCTP_TIMEOUT = 9000; // SCTP needs more waiting time

        private final String name;
        private final MMLT<?, String, ?, String> mmlt;
        private final MMLTModelParams<String> params;

        private Example(String name) {
            this.name = name;

            final String silentOutput = "void";
            final StringSymbolCombiner outputCombiner = StringSymbolCombiner.getInstance();
            final DOTInputModelDeserializer<Integer, String, CompactMMLT<String, String>> parser =
                    DOTParsers.mmlt(silentOutput, outputCombiner);

            try (InputStream is = MMLTExamples.class.getResourceAsStream("/mmlt/" + name + ".dot")) {
                final DOTInputModelData<Integer, String, CompactMMLT<String, String>> model = parser.readModel(is);
                final CompactMMLT<String, String> automaton = model.model;

                final long maxTimeoutDelay = MMLTs.getMaximumTimeoutDelay(automaton);
                final long maxTimerQueryWaitingFinal;

                if (name.contains("SCTP")) {
                    maxTimerQueryWaitingFinal = SCTP_TIMEOUT;
                } else {
                    maxTimerQueryWaitingFinal = MMLTs.getMaximumInitialTimerValue(automaton) * 2;
                }

                this.mmlt = automaton;
                this.params =
                        new MMLTModelParams<>(silentOutput, outputCombiner, maxTimeoutDelay, maxTimerQueryWaitingFinal);
            } catch (IOException | FormatException e) {
                throw new IllegalStateException("Unable to load model " + name, e);
            }
        }

        @Override
        public MMLTModelParams<String> getParams() {
            return this.params;
        }

        @Override
        public MMLT<?, String, ?, String> getReferenceAutomaton() {
            return this.mmlt;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }

}
