package de.learnlib.oracle;

import java.util.Collection;
import java.util.Collections;

import de.learnlib.query.AdaptiveQuery;

public interface SingleAdaptiveMembershipOracle<I,O> extends AdaptiveMembershipOracle<I, O> {

    @Override
    default void processQueries(Collection<? extends AdaptiveQuery<I,O>> queries ) {
        for (AdaptiveQuery<I, O> query : queries) {
            processQuery(query);
        }
    }

    @Override
    void processQuery(AdaptiveQuery<I, O> query);

    @Override
    default void processBatch(Collection<? extends AdaptiveQuery<I, O>> batch) {
        processQueries(batch);
    }
}

