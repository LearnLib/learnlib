package de.learnlib.oracle.parallelism;

import de.learnlib.oracle.AdaptiveMembershipOracle;
import de.learnlib.oracle.MembershipOracle;
import de.learnlib.oracle.ParallelAdaptiveOracle;
import de.learnlib.query.AdaptiveQuery;
import de.learnlib.query.Query;
import net.automatalib.word.Word;
import org.checkerframework.checker.index.qual.NonNegative;

import java.util.Collection;

public class StaticParallelAdaptiveOracle<I,O> extends AbstractStaticBatchProcessor<AdaptiveQuery<I, O>, AdaptiveMembershipOracle<I, O>>
        implements ParallelAdaptiveOracle<I, O> {

    public StaticParallelAdaptiveOracle(Collection<? extends AdaptiveMembershipOracle<I, O>> oracles,
                                        @NonNegative int minBatchSize,
                                        PoolPolicy policy) {
        super(oracles, minBatchSize, policy);
    }

    @Override
    public void processQueries(Collection<? extends AdaptiveQuery<I, O>> queries ) {
        processBatch(queries);
    }
}
