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

import de.learnlib.cli.factory.AlphabetFactory;
import de.learnlib.cli.factory.EQOFactory;
import de.learnlib.cli.factory.LearnerFactory;
import de.learnlib.cli.factory.MQOFactory;
import de.learnlib.cli.factory.SerializerFactory;
import de.learnlib.cli.util.AdaptiveRunner;
import de.learnlib.cli.util.PresetRunner;
import de.learnlib.cli.util.Runner;

public enum Type {
    DFA {
        @Override
        public Runner runner(Options options) {
            return new PresetRunner<>(AlphabetFactory.REGULAR,
                                      MQOFactory.ACCEPTOR,
                                      LearnerFactory.DFA_LEARNER,
                                      EQOFactory.DFA_ORACLES,
                                      SerializerFactory.DFA_SERIALIZER);
        }
    },
    MEALY {
        @Override
        public Runner runner(Options options) {
            if (options.learner == Learner.ADT || options.learner == Learner.LSHARP) {
                return new AdaptiveRunner<>(AlphabetFactory.REGULAR,
                                            MQOFactory.ADAPTIVE,
                                            LearnerFactory.ADAPTIVE_LEARNER,
                                            EQOFactory.ADAPTIVE_ORACLES,
                                            SerializerFactory.MEALY_SERIALIZER);
            } else {
                return new PresetRunner<>(AlphabetFactory.REGULAR,
                                          MQOFactory.TRANSDUCER,
                                          LearnerFactory.MEALY_LEARNER,
                                          EQOFactory.MEALY_ORACLES,
                                          SerializerFactory.MEALY_SERIALIZER);
            }
        }
    },
    NFA {
        @Override
        public Runner runner(Options options) {
            return new PresetRunner<>(AlphabetFactory.REGULAR,
                                      MQOFactory.ACCEPTOR,
                                      LearnerFactory.NFA_LEARNER,
                                      EQOFactory.NFA_ORACLES,
                                      SerializerFactory.NFA_SERIALIZER);
        }
    },
    SBA {
        @Override
        public Runner runner(Options options) {
            return new PresetRunner<>(AlphabetFactory.PROCEDURAL,
                                      MQOFactory.ACCEPTOR,
                                      LearnerFactory.SBA_LEARNER,
                                      EQOFactory.SBA_ORACLES,
                                      SerializerFactory.SBA_SERIALIZER);
        }
    },
    SPA {
        @Override
        public Runner runner(Options options) {
            return new PresetRunner<>(AlphabetFactory.PROCEDURAL,
                                      MQOFactory.ACCEPTOR,
                                      LearnerFactory.SPA_LEARNER,
                                      EQOFactory.SPA_ORACLES,
                                      SerializerFactory.SPA_SERIALIZER);
        }
    },
    SPMM {
        @Override
        public Runner runner(Options options) {
            return new PresetRunner<>(AlphabetFactory.PROCEDURAL,
                                      MQOFactory.TRANSDUCER,
                                      LearnerFactory.SPMM_LEARNER,
                                      EQOFactory.SPMM_ORACLES,
                                      SerializerFactory.SPMM_SERIALIZER);
        }
    },
    VPA {
        @Override
        public Runner runner(Options options) {
            return new PresetRunner<>(AlphabetFactory.VPA,
                                      MQOFactory.ACCEPTOR,
                                      LearnerFactory.VPA_LEARNER,
                                      EQOFactory.VPA_ORACLES,
                                      SerializerFactory.VPA_SERIALIZER);
        }
    };

    public abstract Runner runner(Options options);

}
