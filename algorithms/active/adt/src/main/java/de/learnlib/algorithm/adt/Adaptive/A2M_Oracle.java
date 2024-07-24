package de.learnlib.algorithm.adt.Adaptive;

import de.learnlib.oracle.AdaptiveMembershipOracle;
import de.learnlib.oracle.MembershipOracle;
import de.learnlib.query.AdaptiveQuery;
import de.learnlib.query.Query;
import net.automatalib.word.Word;

import java.util.ArrayList;
import java.util.Collection;

public class A2M_Oracle<I,O> implements MembershipOracle.MealyMembershipOracle<I,O> {

    private final AdaptiveMembershipOracle<I,O> oracle;

    public A2M_Oracle(AdaptiveMembershipOracle<I, O> oracle) {
        this.oracle = oracle;
    }

    @Override
    public void processQueries(Collection<? extends Query<I, Word<O>>> queries) {

        Collection<AdaptiveQuery<I,O>> adaptiveQueries = new ArrayList<>();

        for (Query<I, Word<O>> query : queries) {
            Adaptive_DEF_Query<I,O> adaptive_def_query =
                    new Adaptive_DEF_Query<>(query);

            adaptiveQueries.add(adaptive_def_query);

        }
        this.oracle.processQueries(adaptiveQueries);
    }
}
