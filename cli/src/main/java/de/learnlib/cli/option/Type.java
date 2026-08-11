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
            return new PresetRunner<>(AlphabetFactory::getRegularAlphabet,
                                      MQOFactory::getAcceptorOracle,
                                      LearnerFactory::getDFALearner,
                                      EQOFactory::getRegularOracles,
                                      SerializerFactory::getDFASerializer);
        }
    },
    MEALY {
        @Override
        public Runner runner(Options options) {
            if (options.learner == Learner.ADT || options.learner == Learner.L_SHARP) {
                return new AdaptiveRunner<>(AlphabetFactory::getRegularAlphabet,
                                            MQOFactory::getAdaptiveOracle,
                                            LearnerFactory::getAdaptiveLearner,
                                            EQOFactory::getAdaptiveOracles,
                                            SerializerFactory::getMealySerializer);
            } else {
                return new PresetRunner<>(AlphabetFactory::getRegularAlphabet,
                                          MQOFactory::getTransducerOracle,
                                          LearnerFactory::getMealyLearner,
                                          EQOFactory::getRegularOracles,
                                          SerializerFactory::getMealySerializer);
            }
        }
    },
    NFA {
        @Override
        public Runner runner(Options options) {
            return new PresetRunner<>(AlphabetFactory::getRegularAlphabet,
                                      MQOFactory::getAcceptorOracle,
                                      LearnerFactory::getNFALearner,
                                      EQOFactory::getNFAOracles,
                                      SerializerFactory::getNFASerializer);
        }
    },
    SBA {
        @Override
        public Runner runner(Options options) {
            return new PresetRunner<>(AlphabetFactory::getProceduralAlphabet,
                                      MQOFactory::getAcceptorOracle,
                                      LearnerFactory::getSBALearner,
                                      EQOFactory::getSBAOracles,
                                      SerializerFactory::getSBASerializer);
        }
    },
    SPA {
        @Override
        public Runner runner(Options options) {
            return new PresetRunner<>(AlphabetFactory::getProceduralAlphabet,
                                      MQOFactory::getAcceptorOracle,
                                      LearnerFactory::getSPALearner,
                                      EQOFactory::getSPAOracles,
                                      SerializerFactory::getSPASerializer);
        }
    },
    SPMM {
        @Override
        public Runner runner(Options options) {
            return new PresetRunner<>(AlphabetFactory::getProceduralAlphabet,
                                      MQOFactory::getTransducerOracle,
                                      LearnerFactory::getSPMMLearner,
                                      EQOFactory::getSPMMOracles,
                                      SerializerFactory::getSPMMSerializer);
        }
    },
    VPA {
        @Override
        public Runner runner(Options options) {
            return new PresetRunner<>(AlphabetFactory::getVPAlphabet,
                                      MQOFactory::getAcceptorOracle,
                                      LearnerFactory::getVPALearner,
                                      EQOFactory::getVPAOracles,
                                      SerializerFactory::getVPASerializer);
        }
    };

    public abstract Runner runner(Options options);

}
