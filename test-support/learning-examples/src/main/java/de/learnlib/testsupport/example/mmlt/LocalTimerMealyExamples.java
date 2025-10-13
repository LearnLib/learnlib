package de.learnlib.testsupport.example.mmlt;

import de.learnlib.algorithm.LocalTimerMealyModelParams;
import net.automatalib.automaton.time.impl.mmlt.StringSymbolCombiner;
import net.automatalib.serialization.dot.LocalTimerMealyGraphvizParser;
import net.automatalib.util.automaton.mmlt.LocalTimerMealyUtil;

import java.io.File;
import java.util.List;

public class LocalTimerMealyExamples {

    public static List<LocalTimerMealyModel<Integer, String, String>> getAll() {
        return List.of(
                HVAC(), SCTP(), SensorCollector(), WM(), Oven(), WSN());
    }

    /**
     * Returns an MMLT model of an HVAC system.
     * <p>
     * The system has been adapted from: Taylor and Taylor: Patterns in the Machine
     *
     * @return LocalTimerMealyModel
     */
    public static LocalTimerMealyModel<Integer, String, String> HVAC() {
        return automatonFromFile("HVAC");
    }

    /**
     * Returns an MMLT model of an endpoint in the stream control and transmission protocol.
     * <p>
     * The model has been adapted from: Stewart et al.: Stream Control Transmission Protocol (RFC 9260, Figure 3)
     *
     * @return LocalTimerMealyModel
     */
    public static LocalTimerMealyModel<Integer, String, String> SCTP() {
        return automatonFromFile("SCTP");
    }

    /**
     * Returns an MMLT model of a sensor collector.
     * <p>
     * The sensor measures particulate matter and ambient noise.
     * This program automatically ends after some time. The program may be restarted at any time.
     * Alternatively, a self-check program can be entered. This also ends after some time and may be aborted.
     * At the end of either program, the collected data may be collected.
     *
     * @return LocalTimerMealyModel
     */
    public static LocalTimerMealyModel<Integer, String, String> SensorCollector() {
        return automatonFromFile("sensor_collector");
    }

    /**
     * Returns an MMLT model of a washing machine.
     *
     * @return LocalTimerMealyModel
     */
    public static LocalTimerMealyModel<Integer, String, String> WM() {
        return automatonFromFile("WM");
    }

    /**
     * Returns an MMLT model of an oven with a time-controlled baking program.
     * <p>
     * After powering the oven on, the oven remains idle until the program is started.
     * During the program, the oven regularly measures and adjusts the temperature.
     * At the end of the program, an alarm sounds. Then, the user may extend the program.
     * If not extended, the program ends either when the user opens the door, presses a button, or a timeout occurs.
     *
     * @return LocalTimerMealyModel
     */
    public static LocalTimerMealyModel<Integer, String, String> Oven() {
        return automatonFromFile("Oven");
    }

    /**
     * Returns an MMLT model of a wireless sensor node.
     * <p>
     * The node regularly collects and transmits data. If the battery is low, no data is transmitted. Then,
     * a user may collect the data manually.
     * The node can be shut down at any time. If the battery is empty, it is shut down automatically.
     *
     * @return LocalTimerMealyModel
     */
    public static LocalTimerMealyModel<Integer, String, String> WSN() {
        return automatonFromFile("WSN");
    }

    // ===================================

    /**
     * Loads the automaton model with the provided resource name.
     * Also infers suitable model parameters:
     * - Maximum time to wait for a timeout in any configuration.
     * - Maximum waiting time for timer queries. This must be at least the max. time for a timeout.
     * We choose twice the maximum value of any timer in the model. When inferring a timer with one of these values,
     * the learner has the chance to observe its timeout at least twice. This increases the chance of observing non-periodic behavior.
     *
     */
    static LocalTimerMealyModel<Integer, String, String> automatonFromFile(String name) {

        net.automatalib.automaton.time.mmlt.LocalTimerMealy<Integer, String, String> automaton;
        try (var modelResource = LocalTimerMealyExamples.class.getResourceAsStream("/mmlt/" + name + ".dot")) {
            automaton = LocalTimerMealyGraphvizParser.parseLocalTimerMealy(modelResource, "void", StringSymbolCombiner.getInstance());
        } catch (Exception ex) {
            throw new RuntimeException("Failed to load automaton from resource " + name, ex);
        }

        long maxTimeoutDelay = LocalTimerMealyUtil.getMaximumTimeoutDelay(automaton);
        long maxTimerQueryWaitingFinal = LocalTimerMealyUtil.getMaximumInitialTimerValue(automaton) * 2;

        if (name.contains("SCTP")) {
            maxTimerQueryWaitingFinal = 9000; // SCTP needs more waiting time
        }

        return new LocalTimerMealyModel<>(name, automaton, new LocalTimerMealyModelParams<>("void", maxTimeoutDelay, maxTimerQueryWaitingFinal, StringSymbolCombiner.getInstance()));
    }


}
