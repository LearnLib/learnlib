/* Copyright (C) 2013-2023 TU Dortmund
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
package de.learnlib.algorithms.aaar.abstraction;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.function.Function;

import de.learnlib.algorithms.aaar.abstraction.Node.InnerNode;
import de.learnlib.algorithms.aaar.abstraction.Node.Leaf;
import de.learnlib.api.oracle.MembershipOracle;
import net.automatalib.words.Word;

/**
 * @author fhowar
 * @author frohme
 */
public class AbstractionTree<AI, CI, D> implements Abstraction<AI, CI> {

    private final Map<AI, CI> gamma = new HashMap<>();

    private final MembershipOracle<CI, D> oracle;

    private Node root;

    private final Function<CI, AI> abstractor;

    public AbstractionTree(AI rootA, CI rootC, MembershipOracle<CI, D> o, Function<CI, AI> abstractor) {
        this.oracle = o;
        this.abstractor = abstractor;
        Leaf<AI, CI> l = new Leaf<>();
        l.abs = rootA;
        l.rep = rootC;
        root = l;

        gamma.put(rootA, rootC);
    }

    public AI splitLeaf(CI repOld, CI repNew, Word<CI> prefix, Word<CI> suffix, D outOld, D outNew) {

        //        logger.logMultiline("Splitting Leaf in Abstraction Tree ("+ rootA + ")",
        //                "old rep: " + repOld+ "\n" +
        //                "new rep: " + repNew+ "\n" +
        //                "prefix: " + prefix+ "\n" +
        //                "suffix: " + suffix+ "\n" +
        //                "old out: " + outOld+ "\n" +
        //                "new out: " + outNew+ "\n",
        //                            LogLevel.INFO);

        Leaf<AI, CI> l = new Leaf<>();
        // TODO: Use Wrapper Objects?
        l.abs = this.abstractor.apply(repNew);
        l.rep = repNew;
        gamma.put(l.abs, repNew);

        Node cur = root;
        Node inner = null;

        while (!(cur instanceof Leaf)) {
            @SuppressWarnings("unchecked")
            InnerNode<CI, D> n = (InnerNode<CI, D>) cur;
            Word<CI> test1 = n.prefix.append(repOld).concat(n.suffix);
            D out1 = oracle.answerQuery(test1);
            //                Word test2 = WordUtil.concat(WordUtil.concat(n.prefix, repNew), n.suffix);
            //                Word out2 = oracle.processQuery(test2);

            //                // new leaf without additional information
            //                if (!n.next.containsKey(out2))
            //                {
            //                    n.next.put(out2, l);
            //                    return l.abs;
            //                }
            //
            //                key = out1;
            //                inner = cur;
            //                cur = n.next.get(out1);

            inner = cur;
            if (Objects.equals(n.out, out1)) {
                cur = n.equalsNext;
            } else {
                cur = n.otherNext;
            }

        }

        InnerNode<CI, D> nn = new InnerNode<>();
        nn.prefix = prefix;
        nn.suffix = suffix;
        //        nn.next.put(outOld, cur);
        //        nn.next.put(outNew, l);
        nn.out = outOld;
        nn.equalsNext = cur;
        nn.otherNext = l;

        if (inner == null) {
            root = nn;
            return l.abs;
        }

        //        ((InnerNode)inner).next.remove(key);
        //        ((InnerNode)inner).next.put(key,nn);
        if (((InnerNode<?, ?>) inner).equalsNext == cur) {
            ((InnerNode<?, ?>) inner).equalsNext = nn;
        } else {
            ((InnerNode<?, ?>) inner).otherNext = nn;
        }

        return l.abs;
    }

    @Override
    public AI getAbstractSymbol(CI c) {
        return getAbstractSymbol(c, this.oracle);
    }

    public AI getAbstractSymbol(CI c, MembershipOracle<CI, D> oracle) {
        Node cur = root;
        while (!(cur instanceof Leaf)) {
            @SuppressWarnings("unchecked")
            InnerNode<CI, D> n = (InnerNode<CI, D>) cur;
            Word<CI> test = n.prefix.append(c).concat(n.suffix);
            D out = oracle.answerQuery(test);

            if (Objects.equals(n.out, out)) {
                cur = n.equalsNext;
            } else {
                cur = n.otherNext;
            }

            if (cur == null) {
                return null;
            }
        }
        return ((Leaf<AI, CI>) cur).abs;
    }

    @Override
    public CI getRepresentative(AI a) {
        return gamma.get(a);
    }

    public Collection<CI> getRepresentativeSymbols() {
        List<CI> ret = new ArrayList<>();

        Queue<Node> nodes = new ArrayDeque<>();
        nodes.add(root);

        while (!nodes.isEmpty()) {
            Node n = nodes.poll();
            if (n instanceof InnerNode) {
                @SuppressWarnings("unchecked")
                InnerNode<CI, D> in = (InnerNode<CI, D>) n;
                //                for (Node next : in.next.values())
                //                    nodes.add(next);
                nodes.add(in.equalsNext);
                nodes.add(in.otherNext);
            } else {
                @SuppressWarnings("unchecked")
                Leaf<AI, CI> l = (Leaf<AI, CI>) n;
                ret.add(l.rep);
            }
        }

        return ret;
    }

    public int countLeaves() {
        return gamma.size();
    }

}
