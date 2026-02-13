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
package de.learnlib.algorithm.lambda.ttt.mealy;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.learnlib.algorithm.lambda.ttt.dt.AbstractDecisionTree;
import de.learnlib.algorithm.lambda.ttt.dt.Children;
import de.learnlib.algorithm.lambda.ttt.dt.DTLeaf;
import de.learnlib.algorithm.lambda.ttt.pt.PTNode;
import de.learnlib.algorithm.lambda.ttt.st.STNode;
import de.learnlib.oracle.MembershipOracle;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.word.Word;

class DecisionTreeMealy<I, O> extends AbstractDecisionTree<I, Word<O>> {

    private final Map<Word<I>, O> outputs;

    DecisionTreeMealy(Alphabet<I> sigma, STNode<I> stRoot) {
        super(sigma, stRoot);
        this.outputs = new HashMap<>();
    }

    @Override
    protected Children<I, Word<O>> newChildren() {
        return new ChildrenMealy<>();
    }

    O getOutput(MembershipOracle<I, Word<O>> oracle, DTLeaf<I, Word<O>> leaf, I a) {
        return lookupOrQuery(oracle, leaf.getShortPrefixes().get(0).word(), a);
    }

    @Override
    public boolean makeConsistent(MembershipOracle<I, Word<O>> oracle) {
        for (DTLeaf<I, Word<O>> n : leaves()) {
            if (n.getShortPrefixes().size() < 2) {
                continue;
            }
            for (I a : getAlphabet()) {
                O refOut = null;
                List<PTNode<I, Word<O>>> sp = new LinkedList<>(n.getShortPrefixes());
                for (PTNode<I, Word<O>> u : sp) {
                    O out = lookupOrQuery(oracle, u.word(), a);
                    if (refOut == null) {
                        refOut = out;
                    } else if (!Objects.equals(refOut, out)) {
                        n.split(oracle, sp.get(0), u, a);
                        return true;
                    }
                }
            }
        }
        return super.makeConsistent(oracle);
    }

    private O lookupOrQuery(MembershipOracle<I, Word<O>> oracle, Word<I> prefix, I step) {
        Word<I> lookup = prefix.append(step);
        O out = this.outputs.get(lookup);
        if (out == null) {
            out = oracle.answerQuery(prefix, Word.fromLetter(step)).lastSymbol();
            this.outputs.put(lookup, out);
        }
        return out;
    }
}
