package de.learnlib.algorithms.oml.lstar;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import de.learnlib.api.algorithm.LearningAlgorithm;
import de.learnlib.api.oracle.MembershipOracle;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.impl.FastDFA;
import net.automatalib.automata.fsa.impl.FastDFAState;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

public class OptimalLStarDFA<I> extends ObservationTable<DFA<?, I>, I, Boolean>
        implements LearningAlgorithm.DFALearner<I> {

    private FastDFA<I> hypothesis = null;

    private final Map<FastDFAState, Boolean[]> hypStateMap = new LinkedHashMap<>();

    public OptimalLStarDFA(Alphabet<I> sigma,
                           MembershipOracle.DFAMembershipOracle<I> mqs,
                           MembershipOracle.DFAMembershipOracle<I> ceqs) {
        super(sigma, mqs, ceqs);
    }

    @Override
    Word<I>[] initSuffixes() {
        return new Word[] { Word.<I>epsilon() };
    }

    @Override
    Boolean[] newRowVector(int i) {
        return new Boolean[i];
    }

    @Override
    int maxSearchIndex(int ceLength) {
        return ceLength;
    }

    @Override
    void automatonFromTable() {
        hypStateMap.clear();
        FastDFA<I> hyp = new FastDFA<>(getSigma());
        Map<List<Boolean>, FastDFAState> stateMap = new HashMap<>();
        Boolean[] rowData = getRow( Word.<I>epsilon() );
        FastDFAState q = hyp.addInitialState( rowData[0] );
        stateMap.put(Arrays.asList(rowData), q);
        hypStateMap.put(q, rowData);

        for (Word<I> u : getShortPrefixes()) {
            rowData = getRow(u);
            if (stateMap.containsKey(Arrays.asList(rowData))) {
                continue;
            }
            q = hyp.addState( rowData[0] );
            stateMap.put(Arrays.asList(rowData), q);
            hypStateMap.put(q, rowData);
        }

        for (Map.Entry<FastDFAState, Boolean[]> e : hypStateMap.entrySet()) {
            Word<I> u = getShortPrefixes(e.getValue()).get(0);
            for (I a : getSigma()) {
                Boolean[] destData = getRow(u.append(a));
                assert destData != null;
                FastDFAState dst = stateMap.get(Arrays.asList(destData));
                //System.out.println(Arrays.toString(destData) + " " + dst);
                //System.out.println("Transition: " + u + " (" +
                //        e.getKey().isAccepting() + ") -" + a + "-> " +
                //        getShortPrefixes(destData).get(0) + "(" +
                //        dst.isAccepting() + ")");
                hyp.setTransition(e.getKey(), a, dst);
            }
        }
        this.hypothesis = hyp;
    }

    @Override
    Boolean suffix(Boolean output, int length) {
        return output;
    }

    @Override
    public int size() {
        return hypothesis.size();
    }

    @Override
    public Boolean[] rowForState(Word<I> input) {
        return hypStateMap.get(hypothesis.getState(input));
    }

    @Override
    public Boolean getOutput(Word<I> input, int length) {
        return hypothesis.accepts(input);
    }

    @Override
    public DFA<?, I> getModel() {
        return hypothesis;
    }
}
