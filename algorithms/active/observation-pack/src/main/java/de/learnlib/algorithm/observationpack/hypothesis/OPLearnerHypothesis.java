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
package de.learnlib.algorithm.observationpack.hypothesis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import de.learnlib.AccessSequenceTransformer;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.SupportsGrowingAlphabet;
import net.automatalib.automaton.UniversalDeterministicAutomaton;
import net.automatalib.automaton.concept.StateIDs;
import net.automatalib.graph.Graph;
import net.automatalib.graph.concept.GraphViewable;
import net.automatalib.graph.concept.NodeIDs;
import net.automatalib.visualization.DefaultVisualizationHelper;
import net.automatalib.visualization.VisualizationHelper;
import net.automatalib.word.Word;

/**
 * Basic hypothesis data structure for Discrimination Tree learning algorithms.
 *
 * @param <I>
 *         input symbol type
 * @param <O>
 *         SUL output type
 * @param <SP>
 *         state property type
 * @param <TP>
 *         transition property type
 */
public class OPLearnerHypothesis<I, O, SP, TP>
        implements UniversalDeterministicAutomaton<HState<I, O, SP, TP>, I, HTransition<I, O, SP, TP>, SP, TP>,
                   AccessSequenceTransformer<I>,
                   StateIDs<HState<I, O, SP, TP>>,
                   SupportsGrowingAlphabet<I>,
                   GraphViewable {

    private final Alphabet<I> alphabet;
    private int alphabetSize;
    private HState<I, O, SP, TP> root;
    private final List<HState<I, O, SP, TP>> nodes = new ArrayList<>();

    public OPLearnerHypothesis(Alphabet<I> alphabet) {
        this.alphabet = alphabet;
        this.alphabetSize = this.alphabet.size();
    }

    public HState<I, O, SP, TP> createInitialState() {
        this.root = new HState<>(alphabetSize);
        this.nodes.add(root);
        return this.root;
    }

    public HState<I, O, SP, TP> createState(HTransition<I, O, SP, TP> treeIncoming) {
        HState<I, O, SP, TP> state = new HState<>(alphabetSize, nodes.size(), treeIncoming);
        nodes.add(state);
        treeIncoming.makeTree(state);
        return state;
    }

    @Override
    public HTransition<I, O, SP, TP> getTransition(HState<I, O, SP, TP> state, I symbol) {
        int symIdx = alphabet.getSymbolIndex(symbol);
        return state.getTransition(symIdx);
    }

    @Override
    public HState<I, O, SP, TP> getInitialState() {
        return root;
    }

    @Override
    public SP getStateProperty(HState<I, O, SP, TP> state) {
        return state.getProperty();
    }

    @Override
    public TP getTransitionProperty(HTransition<I, O, SP, TP> trans) {
        return trans.getProperty();
    }

    @Override
    public int getStateId(HState<I, O, SP, TP> state) {
        return state.getId();
    }

    @Override
    public HState<I, O, SP, TP> getState(int id) {
        if (id < 0 || id >= nodes.size()) {
            throw new IndexOutOfBoundsException("No valid id");
        }

        return nodes.get(id);
    }

    @Override
    public Word<I> transformAccessSequence(Word<I> word) {
        HState<I, O, SP, TP> state = getState(word);
        assert state != null;
        return state.getAccessSequence();
    }

    @Override
    public boolean isAccessSequence(Word<I> word) {
        HState<I, O, SP, TP> curr = root;
        for (I sym : word) {
            int symIdx = alphabet.getSymbolIndex(sym);
            HTransition<I, O, SP, TP> trans = curr.getTransition(symIdx);
            if (!trans.isTree()) {
                return false;
            }
            curr = trans.getTreeTarget();
        }
        return true;
    }

    @Override
    public HState<I, O, SP, TP> getSuccessor(HTransition<I, O, SP, TP> trans) {
        return trans.currentTarget();
    }

    @Override
    public void addAlphabetSymbol(I symbol) {

        if (!this.alphabet.containsSymbol(symbol)) {
            this.alphabet.asGrowingAlphabetOrThrowException().addSymbol(symbol);
        }

        final int newAlphabetSize = this.alphabet.size();

        if (alphabetSize < newAlphabetSize) {
            for (HState<I, O, SP, TP> s : this.getStates()) {
                s.ensureInputCapacity(newAlphabetSize);
            }

            this.alphabetSize = newAlphabetSize;
        }
    }

    @Override
    public Collection<HState<I, O, SP, TP>> getStates() {
        return Collections.unmodifiableCollection(nodes);
    }

    @Override
    public StateIDs<HState<I, O, SP, TP>> stateIDs() {
        return this;
    }

    @Override
    public GraphView graphView() {
        return new GraphView();
    }

    public class GraphView
            implements Graph<HState<I, O, SP, TP>, HTransition<I, O, SP, TP>>, NodeIDs<HState<I, O, SP, TP>> {

        @Override
        public Collection<HState<I, O, SP, TP>> getNodes() {
            return Collections.unmodifiableCollection(nodes);
        }

        @Override
        public NodeIDs<HState<I, O, SP, TP>> nodeIDs() {
            return this;
        }

        @Override
        public Collection<HTransition<I, O, SP, TP>> getOutgoingEdges(HState<I, O, SP, TP> node) {
            return node.getOutgoingTransitions();
        }

        @Override
        public HState<I, O, SP, TP> getTarget(HTransition<I, O, SP, TP> edge) {
            return edge.currentTarget();
        }

        @Override
        public int getNodeId(HState<I, O, SP, TP> node) {
            return getStateId(node);
        }

        @Override
        public HState<I, O, SP, TP> getNode(int id) {
            return getState(id);
        }

        @Override
        public VisualizationHelper<HState<I, O, SP, TP>, HTransition<I, O, SP, TP>> getVisualizationHelper() {
            return new DefaultVisualizationHelper<HState<I, O, SP, TP>, HTransition<I, O, SP, TP>>() {

                @Override
                protected Collection<HState<I, O, SP, TP>> initialNodes() {
                    return Collections.singleton(root);
                }

                @Override
                public boolean getEdgeProperties(HState<I, O, SP, TP> src,
                                                 HTransition<I, O, SP, TP> edge,
                                                 HState<I, O, SP, TP> tgt,
                                                 Map<String, String> properties) {
                    super.getEdgeProperties(src, edge, tgt, properties);

                    properties.put(EdgeAttrs.LABEL, String.valueOf(edge.getSymbol()));
                    if (edge.isTree()) {
                        properties.put(EdgeAttrs.STYLE, EdgeStyles.BOLD);
                    }

                    return true;
                }
            };
        }
    }
}
