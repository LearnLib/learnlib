package de.learnlib.algorithms.oml.ttt.mealy;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.learnlib.algorithms.oml.ttt.dt.Children;
import de.learnlib.algorithms.oml.ttt.dt.DTLeaf;
import de.learnlib.algorithms.oml.ttt.dt.DecisionTree;
import de.learnlib.algorithms.oml.ttt.pt.PTNode;
import de.learnlib.algorithms.oml.ttt.st.STNode;
import de.learnlib.api.oracle.MembershipOracle;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

public class DecisionTreeMealy<I, O> extends DecisionTree<I, Word<O>> {

    private final Map<Word<I>, Word<O>> outputs = new LinkedHashMap<>();

    DecisionTreeMealy(MembershipOracle<I, Word<O>> mqOracle, Alphabet<I> sigma, STNode<I> stRoot) {
        super(mqOracle, sigma, stRoot);
    }

    @Override
    protected Children<I, Word<O>> newChildren() {
        return new ChildrenMealy<>();
    }

    @Override
    protected Word<O> query(PTNode<I> prefix, STNode<I> suffix) {
        return mqOracle.answerQuery(prefix.word(), suffix.word()).suffix(suffix.word().length());
    }

    Word<O> getOutput(DTLeaf<I, Word<O>> leaf, I a) {
        return lookupOrQuery(leaf.getShortPrefixes().get(0).word(), Word.<I>fromLetter(a));
    }

    @Override
    public boolean makeConsistent() {
        for (DTLeaf<I, Word<O>> n : leaves()) {
            if (n.getShortPrefixes().size() < 2) {
                continue;
            }
            for (I a : sigma) {
                Word<O> refOut = null;
                List<PTNode<I>> sp = new LinkedList<>(n.getShortPrefixes());
                for (PTNode<I> u : sp) {
                    Word<O> out = lookupOrQuery(u.word(), Word.<I>fromLetter(a));
                    if (refOut == null) {
                        refOut = out;
                    } else if (!refOut.equals(out)) {
                        n.split(sp.get(0), u, a);
                        return true;
                    }
                }
            }
        }
        return super.makeConsistent();
    }

    private Word<O> lookupOrQuery(Word<I> prefix, Word<I> suffix) {
        Word<I> lookup = prefix.concat(suffix);
        Word<O> out = this.outputs.get(lookup);
        if (out == null) {
            out = mqOracle.answerQuery(prefix, suffix).suffix(1);
            this.outputs.put(lookup, out);
        }
        return out;
    }
}
