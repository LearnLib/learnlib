/* Copyright (C) 2013 TU Dortmund
 * This file is part of LearnLib, http://www.learnlib.de/.
 * 
 * LearnLib is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 3.0 as published by the Free Software Foundation.
 * 
 * LearnLib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with LearnLib; if not, see
 * <http://www.gnu.de/documents/lgpl.en.html>.
 */
package de.learnlib.dhc.mealy;

import de.learnlib.api.LearningAlgorithm;
import de.learnlib.api.MembershipOracle;
import de.learnlib.api.Query;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.automata.transout.impl.FastMealy;
import net.automatalib.automata.transout.impl.FastMealyState;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.SimpleAlphabet;

/**
 *
 * @author Maik Merten <maikmerten@googlemail.com>
 */
public class MealyDHC<I, O> implements LearningAlgorithm<MealyMachine<?, I, ?, O>, I, Word<O>> {

    private Alphabet<I> alphabet;
    private MembershipOracle<I, Word<O>> oracle;
    private SimpleAlphabet<Word<I>> splitters = new SimpleAlphabet<>();
    private FastMealy<I, O> hypothesis;

    public MealyDHC(Alphabet<I> alphabet, MembershipOracle<I, Word<O>> oracle) {
        this.alphabet = alphabet;
        this.oracle = oracle;

    }

    @Override
    public void startLearning() {

        // the effective alphabet is the concatenation of the real alphabet
        // wrapped in Words and the list of splitters
        SimpleAlphabet<Word<I>> effectivealpha = new SimpleAlphabet<>();
        for (I input : alphabet) {
            effectivealpha.add(Word.fromLetter(input));
        }
        effectivealpha.addAll(splitters);

        // initialize structure to store state output signatures
        Map<List<Word<O>>, FastMealyState<O>> signatures = new HashMap<>();

        // initialize structures for storing access sequences
        Map<FastMealyState<O>, FastMealyState<O>> predecessors = new HashMap<>();
        Map<FastMealyState<O>, I> presymbols = new HashMap<>();

        // set up new hypothesis machine
        hypothesis = new FastMealy<>(alphabet);

        // initialize exploration queue
        Queue<FastMealyState<O>> queue = new LinkedList<>();
        queue.add(hypothesis.addInitialState());

        while (!queue.isEmpty()) {
            // get state to be explored from queue
            FastMealyState<O> state = queue.poll();

            // determine access sequence for state
            Word<I> access = assembleAccessSequence(state, predecessors, presymbols);

            // assemble queries
            ArrayList<Query<I, Word<O>>> queries = new ArrayList<>(effectivealpha.size());
            for (Word<I> suffix : effectivealpha) {
                queries.add(new Query<I, Word<O>>(access, suffix));
            }

            // retrieve answers
            oracle.processQueries(queries);

            // assemble output signature
            List<Word<O>> sig = new ArrayList<>(effectivealpha.size());
            for (Query<I, Word<O>> query : queries) {
                sig.add(query.getOutput());
            }

            if (signatures.keySet().contains(sig)) {
                // this state does not possess a new output signature, remove
                // state and rewire all transitions to it to the sibling
                
                FastMealyState<O> sibling = signatures.get(sig);
                
                FastMealyState<O> predecessor = predecessors.get(state);
                I input = presymbols.get(state);
                O output = hypothesis.getOutput(predecessor, input);
                
                predecessors.remove(state);
                presymbols.remove(state);
                
                hypothesis.removeState(state);
                
                hypothesis.addTransition(predecessor, input, sibling, output);
                
            } else {
                // this is actually an observably distinct state! Progress!
                
                signatures.put(sig, state);
                
                for (int i = 0; i < alphabet.size(); ++i) {
                    // create successor
                    FastMealyState<O> succ = hypothesis.addState();
                    
                    // retrieve I/O for transition
                    I input = alphabet.getSymbol(i);
                    O output = queries.get(i).getOutput().getSymbol(0);
                    
                    // establish transition
                    hypothesis.addTransition(state, input, succ, output);
                    predecessors.put(succ, state);
                    presymbols.put(succ, input);

                    // schedule successor for exploration
                    queue.add(succ);
                }
            }


        }

    }

    @Override
    public boolean refineHypothesis(Query<I, Word<O>> ceQuery) {
        // TODO: extract new splitters from counterexample

        startLearning();
        return true;
    }

    @Override
    public MealyMachine<?, I, ?, O> getHypothesisModel() {
        if (hypothesis == null) {
            throw new IllegalStateException("No hypothesis learned yet");
        }
        return (MealyMachine<?, I, ?, O>) hypothesis;
    }

    private Word<I> assembleAccessSequence(FastMealyState<O> state, Map<FastMealyState<O>, FastMealyState<O>> prestate, Map<FastMealyState<O>, I> presymbol) {
        List<I> sequence = new LinkedList<>();

        do {
            FastMealyState<O> pre = prestate.get(state);
            I sym = presymbol.get(state);
            if (pre != null && sym != null) {
                sequence.add(0, sym);
                state = pre;
            } else {
                break;
            }
        } while (true);

        return Word.fromList(sequence);
    }


}
