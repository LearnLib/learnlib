package de.learnlib.algorithm.adt.Adaptive;

import java.util.ArrayList;
import java.util.Collection;

import de.learnlib.oracle.AdaptiveMembershipOracle;
import de.learnlib.oracle.MembershipOracle;
import de.learnlib.oracle.SymbolQueryOracle;
import de.learnlib.query.AdaptiveQuery;
import de.learnlib.query.Query;
import net.automatalib.word.Word;

public class A2S_Oracle<I,O> implements SymbolQueryOracle<I,O> {

    private final AdaptiveMembershipOracle<I,O> oracle;

    public A2S_Oracle(AdaptiveMembershipOracle<I, O> oracle) {
        this.oracle = oracle;
    }

    @Override
    public O query(I i) {
        return null;
    }

    @Override
    public void reset() {

    }

    @Override
    public void processQueries(Collection<? extends Query<I, Word<O>>> queries) {

        Collection<AdaptiveQuery<I,O>> adaptiveQueries = new ArrayList<>();

        for (Query<I, Word<O>> query : queries) {
            PresetAdaptiveQuery<I,O> presetAdaptive__query =
                    new PresetAdaptiveQuery<>(query);

            adaptiveQueries.add(presetAdaptive__query);

        }
        this.oracle.processQueries(adaptiveQueries);
    }
}
