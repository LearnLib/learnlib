package de.learnlib.statistic.container;

/**
 * Interface for a component that is interested in storing statistics in a container.
 */
public interface LearnerStatsProvider {
    /**
     * Provides a container for storing statistics.
     *
     * @param container Stats container.
     */
    void setStatsContainer(StatsContainer container);
}
