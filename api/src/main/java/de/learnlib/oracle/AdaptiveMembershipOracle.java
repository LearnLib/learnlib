package de.learnlib.oracle;

import de.learnlib.query.AdaptiveQuery;

import java.util.Collection;

public interface AdaptiveMembershipOracle<I,O> extends BatchProcessor<AdaptiveQuery<I,O>> {

    void processQueries(Collection<? extends AdaptiveQuery<I,O>> queries );

    @Override
    default void processBatch(Collection<? extends AdaptiveQuery<I, O>> batch) {
        processQueries(batch);
    }
}

