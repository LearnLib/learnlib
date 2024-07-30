package de.learnlib.oracle;

import de.learnlib.query.AdaptiveQuery;

import java.util.Collection;
import java.util.Collections;

public interface AdaptiveMembershipOracle<I,O> extends BatchProcessor<AdaptiveQuery<I,O>> {

    void processQueries(Collection<? extends AdaptiveQuery<I,O>> queries );

    default void processQuery(AdaptiveQuery<I, O> query) {
        processQueries(Collections.singleton(query));
    }

    @Override
    default void processBatch(Collection<? extends AdaptiveQuery<I, O>> batch) {
        processQueries(batch);
    }
}

