package de.learnlib.filter.statistic.container;

import de.learnlib.statistic.StatisticsProvider;
import de.learnlib.statistic.StatsContainer;
import org.kohsuke.MetaInfServices;

@MetaInfServices(StatisticsProvider.class)
public class MapStatsProvider implements StatisticsProvider {

    final ThreadLocal<StatsContainer> threadLocal = ThreadLocal.withInitial(MapStatsContainer::new);

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public StatsContainer getContainer() {
        return threadLocal.get();
    }
}
