package de.learnlib.algorithm.adt.Adaptive;

import de.learnlib.oracle.AdaptiveMembershipOracle;
import de.learnlib.query.AdaptiveQuery;
import de.learnlib.sul.SUL;

import java.util.Collection;


/*
    Adaptive oracle
    - answers queries by means of SUL
    - is indifferent to the query type ( default, ADT )

 */

public class Sul_Adaptive_Oracle<I,O> implements AdaptiveMembershipOracle<I,O> {
    private final SUL<I,O> sul;

    public Sul_Adaptive_Oracle(SUL<I, O> sul) {
        this.sul = sul;
    }

    @Override
    public void processQueries(Collection<? extends AdaptiveQuery<I, O>> queries) {
        for (AdaptiveQuery<I, O> query : queries) {
            processQuery(query);
        }
    }

    public void processQuery(AdaptiveQuery<I,O> query ) {
        sul.pre();

        while (!query.getIsFinished()) {

            I in = query.getInput();
            if (in == null) {
                //either reset node or final node
                sul.post();

                //if query is not finished, continue querying
                if (!query.getIsFinished()) {
                    sul.pre();
                    query.processOutput(null);
                }
            } else {

                //query the next symbol of the SUL
                O out = sul.step(in);
                query.processOutput(out);
            }
        }
    }
}

