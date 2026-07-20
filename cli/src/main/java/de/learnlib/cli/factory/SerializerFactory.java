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

import java.io.DataOutput;
import java.util.function.Function;

import de.learnlib.cli.option.Options;
import de.learnlib.cli.option.Output;
import net.automatalib.automaton.Automaton;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.automaton.fsa.NFA;
import net.automatalib.automaton.procedural.SBA;
import net.automatalib.automaton.procedural.SPA;
import net.automatalib.automaton.procedural.SPMM;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.automaton.vpa.OneSEVPA;
import net.automatalib.graph.concept.GraphViewable;
import net.automatalib.serialization.InputModelSerializer;
import net.automatalib.serialization.aut.AUTWriter;
import net.automatalib.serialization.ba.BAWriter;
import net.automatalib.serialization.dot.DOTSerializationProvider;
import net.automatalib.serialization.learnlibv2.LearnLibV2Serialization;
import net.automatalib.serialization.mata.writer.MataNFAWriter;
import net.automatalib.serialization.saf.SAFWriters;
import net.automatalib.ts.simple.SimpleTS;

@FunctionalInterface
public interface SerializerFactory<M extends SimpleTS<?, String>>
        extends Function<Options, InputModelSerializer<String, M>> {

    SerializerFactory<DFA<?, String>> DFA_SERIALIZER = SerializerFactory::getDFASerializer;
    SerializerFactory<MealyMachine<?, String, ?, String>> MEALY_SERIALIZER = SerializerFactory::getMealySerializer;
    SerializerFactory<NFA<?, String>> NFA_SERIALIZER = SerializerFactory::getNFASerializer;
    SerializerFactory<SBA<?, String>> SBA_SERIALIZER = SerializerFactory::getSBASerializer;
    SerializerFactory<SPA<?, String>> SPA_SERIALIZER = SerializerFactory::getSPASerializer;
    SerializerFactory<SPMM<?, String, ?, String>> SPMM_SERIALIZER = SerializerFactory::getSPMMSerializer;
    SerializerFactory<OneSEVPA<?, String>> VPA_SERIALIZER = SerializerFactory::getVPASerializer;

    private static <I> InputModelSerializer<I, DFA<?, I>> getDFASerializer(Options options) {
        final InputModelSerializer<I, ? super DFA<?, I>> dfaSerializer =
                SerializerFactory.<I>getDFASerializerInternal(options);
        return dfaSerializer::writeModel;
    }

    private static <I> InputModelSerializer<I, ? super DFA<?, I>> getDFASerializerInternal(Options options) {
        return switch (options.format) {
            case AUT -> new AUTWriter<>();
            case BA -> new BAWriter<>();
            case DOT -> dotAutomatonWriter();
            case LEARNLIBV2 -> LearnLibV2Serialization.getInstance();
            case MATA -> new MataNFAWriter<>();
            case SAF -> SAFWriters.dfa();
            // case TAF -> TAFWriters.dfa();
            default -> throw new UnsupportedCombinationException(options);
        };
    }

    private static <I> InputModelSerializer<I, MealyMachine<?, I, ?, String>> getMealySerializer(Options options) {
        return switch (options.format) {
            case DOT -> dotAutomatonWriter();
            case SAF -> SAFWriters.mealy(DataOutput::writeUTF);
            // case TAF -> TAFWriters.mealy();
            default -> throw new UnsupportedCombinationException(options);
        };
    }

    private static <I> InputModelSerializer<I, NFA<?, I>> getNFASerializer(Options options) {
        final InputModelSerializer<I, ? super NFA<?, I>> nfaSerializer =
                SerializerFactory.<I>getNFASerializerInternal(options);
        return nfaSerializer::writeModel;
    }

    private static <I> InputModelSerializer<I, ? super NFA<?, I>> getNFASerializerInternal(Options options) {
        return switch (options.format) {
            case AUT -> new AUTWriter<>();
            case BA -> new BAWriter<>();
            case DOT -> dotAutomatonWriter();
            case MATA -> new MataNFAWriter<>();
            case SAF -> SAFWriters.nfa();
            default -> throw new UnsupportedCombinationException(options);
        };
    }

    private static <I> InputModelSerializer<I, SBA<?, I>> getSBASerializer(Options options) {
        if (options.format == Output.DOT) {
            return dotGraphWriter();
        }
        throw new UnsupportedCombinationException(options);
    }

    private static <I> InputModelSerializer<I, SPA<?, I>> getSPASerializer(Options options) {
        if (options.format == Output.DOT) {
            return dotGraphWriter();
        }
        throw new UnsupportedCombinationException(options);
    }

    private static <I, O> InputModelSerializer<I, SPMM<?, I, ?, O>> getSPMMSerializer(Options options) {
        if (options.format == Output.DOT) {
            return dotGraphWriter();
        }
        throw new UnsupportedCombinationException(options);
    }

    private static <I> InputModelSerializer<I, OneSEVPA<?, I>> getVPASerializer(Options options) {
        if (options.format == Output.DOT) {
            return dotGraphWriter();
        }
        throw new UnsupportedCombinationException(options);
    }

    private static <I, M extends Automaton<?, I, ?>> InputModelSerializer<I, M> dotAutomatonWriter() {
        return (os, model, inputs) -> DOTSerializationProvider.getInstance()
                                                              .writeModel(os, model.transitionGraphView(inputs));
    }

    private static <I, M extends SimpleTS<?, I> & GraphViewable> InputModelSerializer<I, M> dotGraphWriter() {
        return (os, model, inputs) -> DOTSerializationProvider.getInstance().writeModel(os, model.graphView());
    }

    class UnsupportedCombinationException extends IllegalArgumentException {

        UnsupportedCombinationException(Options options) {
            super(String.format("Type '%s' cannot be written into '%s' format", options.type, options.format));
        }
    }
}
