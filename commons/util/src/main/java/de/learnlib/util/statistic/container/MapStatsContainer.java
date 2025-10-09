package de.learnlib.util.statistic.container;

import de.learnlib.statistic.container.StatsContainerX;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.time.Duration;
import java.util.*;

/**
 * A {@link StatsContainerX} that stores all statistics in a {@link Map}.
 */
public class MapStatsContainer implements StatsContainerX {
    private final Map<String, LearnerStatistic> statistics = new HashMap<>(); // id -> stat

    @Override
    public void addTextInfo(String id, @Nullable String description, String text) {
        statistics.put(id, new TextStatistic(id, description, text));
    }

    @Override
    public Optional<String> getTextValue(String id) {
        var value = statistics.get(id);
        if (value instanceof TextStatistic textStatistic) {
            return Optional.of(textStatistic.getText());
        }
        return Optional.empty();
    }

    @Override
    public void setFlag(String id, @Nullable String description, boolean value) {
        statistics.put(id, new FlagStatistic(id, description, value));
    }

    @Override
    public Optional<Boolean> getFlagValue(String id) {
        var value = statistics.get(id);
        if (value instanceof FlagStatistic flagStatistic) {
            return Optional.of(flagStatistic.isFlagged());
        }
        return Optional.empty();
    }

    @Override
    public void startOrResumeClock(String id, @Nullable String description) {
        var value = statistics.get(id);
        if (value instanceof StopClockStatistic clockStatistic) {
            clockStatistic.resume();
        } else {
            // Create and start a new clock:
            var newClock = new StopClockStatistic(id, description);
            statistics.put(id, newClock);
            newClock.resume();
        }
    }

    @Override
    public void pauseClock(String id) {
        var value = statistics.get(id);
        if (value instanceof StopClockStatistic clockStatistic) {
            clockStatistic.pause();
        }
    }

    @Override
    public Optional<Duration> getClockValue(String id) {
        var value = statistics.get(id);
        if (value instanceof StopClockStatistic clockStatistic) {
            return Optional.of(clockStatistic.getElapsed());
        }
        return Optional.empty();
    }

    @Override
    public void increaseCounter(String id, @Nullable String description, long increment) {
        var value = statistics.get(id);
        if (value instanceof CounterStatistic counterStatistic) {
            counterStatistic.increase(increment);
        } else {
            // Create a new counter:
            setCounter(id, description, increment);
        }
    }

    @Override
    public void setCounter(String id, @Nullable String description, long count) {
        statistics.put(id, new CounterStatistic(id, description, count));
    }

    @Override
    public Optional<Long> getCount(String id) {
        var value = statistics.get(id);
        if (value instanceof CounterStatistic counterStatistic) {
            return Optional.of(counterStatistic.getCount());
        }
        return Optional.empty();
    }

    // ==================

    public String toJson() {
        List<LearnerStatistic> sortedStats = statistics.values().stream().sorted(Comparator.comparing(LearnerStatistic::getDescription)).toList();

        List<String> lines = new ArrayList<>();
        for (var stat : sortedStats) {
            if (stat instanceof StopClockStatistic sc) {
                lines.add(String.format("\"%s [ms]\": %d", stat.getDescription(), sc.getElapsed().toMillis()));
            } else if (stat instanceof CounterStatistic c) {
                lines.add(String.format("\"%s\": %d", stat.getDescription(), c.getCount()));
            } else if (stat instanceof FlagStatistic f) {
                lines.add(String.format("\"%s\": %s", stat.getDescription(), f.isFlagged()));
            } else if (stat instanceof TextStatistic t) {
                lines.add(String.format("\"%s\": \"%s\"", stat.getDescription(), t.getText()));
            }
        }

        if (lines.isEmpty()) {
            return "{}";
        }

        return "{" + String.join(",\n", lines) + "}";
    }


    public String toYaml() {
        List<LearnerStatistic> sortedStats = statistics.values().stream().sorted(Comparator.comparing(LearnerStatistic::getDescription)).toList();

        List<String> lines = new ArrayList<>();
        for (var stat : sortedStats) {
            if (stat instanceof StopClockStatistic sc) {
                lines.add(String.format("   \"%s [ms]\": %d", stat.getDescription(), sc.getElapsed().toMillis()));
            } else if (stat instanceof CounterStatistic c) {
                lines.add(String.format("   \"%s\": %d", stat.getDescription(), c.getCount()));
            } else if (stat instanceof FlagStatistic f) {
                lines.add(String.format("   \"%s\": %s", stat.getDescription(), f.isFlagged()));
            } else if (stat instanceof TextStatistic t) {
                lines.add(String.format("   \"%s\": \"%s\"", stat.getDescription(), t.getText()));
            }
        }

        if (lines.isEmpty()) {
            return null;
        }

        // Add dash to first line:
        String newFirstLine = " - " + lines.get(0).stripLeading();
        lines.set(0, newFirstLine);

        return String.join("\n", lines);
    }


    public void printStats() {
        // Print results:
        System.out.println("============================================");
        System.out.println("Statistics:");
        System.out.println(this.toYaml());
        System.out.println("============================================");
    }
}
