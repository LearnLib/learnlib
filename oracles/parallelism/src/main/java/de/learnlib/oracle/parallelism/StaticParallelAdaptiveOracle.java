package de.learnlib.oracle.parallelism;

import java.util.Collection;

import de.learnlib.oracle.AdaptiveMembershipOracle;
import de.learnlib.oracle.ParallelAdaptiveOracle;
import de.learnlib.query.AdaptiveQuery;
import org.checkerframework.checker.index.qual.NonNegative;

/**
 * A specialized {@link AbstractStaticBatchProcessor} for {@link AdaptiveMembershipOracle}s that implements
 * {@link ParallelAdaptiveOracle}.
 *
 * @param <I>
 *         input symbol type
 * @param <O>
 *         output symbol type
 */
public class StaticParallelAdaptiveOracle<I, O>
        extends AbstractStaticBatchProcessor<AdaptiveQuery<I, O>, AdaptiveMembershipOracle<I, O>>
        implements ParallelAdaptiveOracle<I, O> {

    public StaticParallelAdaptiveOracle(Collection<? extends AdaptiveMembershipOracle<I, O>> oracles,
                                        @NonNegative int minBatchSize,
                                        PoolPolicy policy) {
        super(oracles, minBatchSize, policy);
    }

    @Override
    public void processQueries(Collection<? extends AdaptiveQuery<I, O>> queries) {
        processBatch(queries);
    }
}
