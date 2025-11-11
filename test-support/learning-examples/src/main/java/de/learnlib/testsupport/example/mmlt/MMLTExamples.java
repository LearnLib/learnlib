package de.learnlib.testsupport.example.mmlt;

import de.learnlib.algorithm.MMLTModelParams;
import net.automatalib.automaton.mmlt.impl.StringSymbolCombiner;
import net.automatalib.exception.FormatException;
import net.automatalib.serialization.dot.DOTParsers;
import net.automatalib.util.automaton.mmlt.MMLTUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class MMLTExamples {

    public static List<MMLTModel<?, String, ?, String>> getAll() {
        return List.of(HVAC(), SCTP(), SensorCollector(), WM(), Oven(), WSN());
    }

    /**
     * Returns an MMLT model of an HVAC system.
     * <p>
     * The system has been adapted from: Taylor and Taylor: Patterns in the Machine
     *
     * @return LocalTimerMealyModel
     */
    public static MMLTModel<?, String, ?, String> HVAC() {
        return automatonFromFile("HVAC");
    }

    /**
     * Returns an MMLT model of an endpoint in the stream control and transmission protocol.
     * <p>
     * The model has been adapted from: Stewart et al.: Stream Control Transmission Protocol (RFC 9260, Figure 3)
     *
     * @return LocalTimerMealyModel
     */
    public static MMLTModel<?, String, ?, String> SCTP() {
        return automatonFromFile("SCTP");
    }

    /**
     * Returns an MMLT model of a sensor collector.
     * <p>
     * The sensor measures particulate matter and ambient noise.
     * The measurement program automatically ends after some time. The program may be restarted at any time.
     * Alternatively, a self-check program can be entered. This also ends after some time and may be aborted.
     * At the end of either program, the collected data may be retrieved.
     *
     * @return LocalTimerMealyModel
     */
    public static MMLTModel<?, String, ?, String> SensorCollector() {
        return automatonFromFile("sensor_collector");
    }

    /**
     * Returns an MMLT model of a washing machine.
     * <p>
     * The machine is initially off. After powering it on and closing the door,
     * the user can start either the short or the normal program. An open
     * door prevents starting and triggers a warning. Not choosing a program within 10 seconds turns the machine off.
     * <p>
     * In normal model, the machine fills the drum, heats the water, and starts the main wash. During this wash,
     * it regularly adjusts the drum speed and maintains temperature. After 2 hours,
     * the water is drained and the drum is spun at full speed for some time. Afterwards the remaining water is drained.
     * The short program makes less adjustments, so that a wash ends after 1 hour.
     * <p>
     * Both programs are interrupted when a leak is detected. Normal mode may also be interrupted by "stop".
     * This drains the drum immediately. Once done, the door is unlocked, a message is shown, and the machine
     * beeps repeatedly until the user presses any button or opens the door.
     *
     * @return LocalTimerMealyModel
     */
    public static MMLTModel<?, String, ?, String> WM() {
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
    public static MMLTModel<?, String, ?, String> Oven() {
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
    public static MMLTModel<?, String, ?, String> WSN() {
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
    static MMLTModel<?, String, ?, String> automatonFromFile(String name) {
        var silentOutput = "void";
        var outputCombiner = StringSymbolCombiner.getInstance();
        var parser = DOTParsers.mmlt(silentOutput, outputCombiner);

        try (InputStream is = MMLTExamples.class.getResourceAsStream("/mmlt/" + name + ".dot")) {
            var model = parser.readModel(is);
            var automaton = model.model;

            long maxTimeoutDelay = MMLTUtil.getMaximumTimeoutDelay(automaton);
            long maxTimerQueryWaitingFinal = MMLTUtil.getMaximumInitialTimerValue(automaton) * 2;

            if (name.contains("SCTP")) {
                maxTimerQueryWaitingFinal = 9000; // SCTP needs more waiting time
            }

            return new MMLTModel<>(name,
                                   automaton,
                                   new MMLTModelParams<>(silentOutput,
                                                         maxTimeoutDelay,
                                                         maxTimerQueryWaitingFinal,
                                                         outputCombiner));
        } catch (IOException | FormatException e) {
            throw new RuntimeException("Unable to load model " + name, e);
        }
    }

}
