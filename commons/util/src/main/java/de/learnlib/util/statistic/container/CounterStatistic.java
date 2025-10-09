package de.learnlib.util.statistic.container;

/**
 * A counter that can be increased and set to a particular positive number.
 */
class CounterStatistic extends LearnerStatistic {
    private long count;

    public CounterStatistic(String id, String description) {
        this(id, description, 0);
    }

    public CounterStatistic(String id, String description, long count) {
        super(id, description);
        this.count = count;
    }

    public void setCount(long count) {
        if (count < 0) {
            throw new IllegalArgumentException();
        }
        this.count = count;
    }

    public void increase(long increment) {
        this.count += increment;
    }

    public long getCount() {
        return count;
    }
}
