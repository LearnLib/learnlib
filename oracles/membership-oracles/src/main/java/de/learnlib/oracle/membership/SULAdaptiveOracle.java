package de.learnlib.oracle.membership;

import java.util.Collection;

import de.learnlib.oracle.AdaptiveMembershipOracle;
import de.learnlib.query.AdaptiveQuery;
import de.learnlib.query.AdaptiveQuery.Response;
import de.learnlib.sul.SUL;

public class SULAdaptiveOracle<I, O> implements AdaptiveMembershipOracle<I, O> {

    private final SUL<I, O> sul;

    public SULAdaptiveOracle(SUL<I, O> sul) {
        this.sul = sul;
    }

    @Override
    public void processQueries(Collection<? extends AdaptiveQuery<I, O>> queries) {
        for (AdaptiveQuery<I, O> query : queries) {
            processQuery(query);
        }
    }

    public void processQuery(AdaptiveQuery<I, O> query) {
        sul.pre();

        Response response;

        do {
            final I in = query.getInput();
            final O out = sul.step(in);

            response = query.processOutput(out);

            if (response == Response.RESET) {
                sul.post();
                sul.pre();
            }
        } while (response != Response.FINISHED);

        sul.post();
    }
}

