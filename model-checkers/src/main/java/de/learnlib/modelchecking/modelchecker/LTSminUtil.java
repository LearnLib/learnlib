/* Copyright (C) 2013-2018 TU Dortmund
 * This file is part of LearnLib, http://www.learnlib.de/.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.learnlib.modelchecking.modelchecker;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import de.learnlib.setting.LearnLibProperty;
import de.learnlib.setting.LearnLibSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A utility class that encapsulates certain technical aspects of LTSmin (e.g. accessibility of the binary, etc.)
 *
 * @author Jeroen Meijer
 * @author frohme
 */
public final class LTSminUtil {

    /**
     * Path to the "etf2lts-mc" binary.
     */
    static final String ETF2LTS_MC;

    /**
     * Path to the "ltsmin-convert" binary.
     */
    static final String LTSMIN_CONVERT;

    private static final Logger LOGGER = LoggerFactory.getLogger(LTSminUtil.class);

    private static final String CHECK = "An exception occurred while checking if LTSmin is installed. " +
                                        "Could not run binary '%s', the following exception occurred: %s. " +
                                        "LTSmin can be obtained at https://ltsmin.utwente.nl. If you installed LTSmin " +
                                        "in a non standard location you can set the property: '" +
                                        LearnLibProperty.LTSMIN_PATH.getPropertyKey() +
                                        "'. Setting the $PATH variable works too.";

    /**
     * The exit code for running an LTSmin binary with --version.
     */
    private static final int VERSION_EXIT = 255;

    static {
        LearnLibSettings settings = LearnLibSettings.getInstance();

        final String ltsMinPath = settings.getProperty(LearnLibProperty.LTSMIN_PATH, "");

        ETF2LTS_MC = Paths.get(ltsMinPath, "etf2lts-mc").toString();
        LTSMIN_CONVERT = Paths.get(ltsMinPath, "ltsmin-convert").toString();
    }

    private LTSminUtil() {
        throw new AssertionError();
    }

    /**
     * Checks whether the required binaries for the {@link AbstractLTSminLTL LTSmin modelchecker} can be executed, by
     * performing a version check.
     *
     * @return {@code true} if the binary returned with the expected exit value, {@code false} otherwise.
     *
     * @see #ETF2LTS_MC
     * @see #LTSMIN_CONVERT
     */
    public static boolean checkUsable() {
        return checkUsable(ETF2LTS_MC) && checkUsable(LTSMIN_CONVERT);
    }

    /**
     * Checks whether the given binary can be executed, by performing a version check.
     *
     * @param bin
     *         the binary to check.
     *
     * @return {@code true} if the binary returned with the expected exit value, {@code false} otherwise.
     */
    private static boolean checkUsable(String bin) {

        // the command lines for the ProcessBuilder
        final List<String> commandLines = new ArrayList<>();

        // add the binary
        commandLines.add(bin);

        // just run a version check
        commandLines.add("--version");

        final Process check;
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(commandLines);
            check = processBuilder.start();
            check.waitFor();
        } catch (IOException | InterruptedException e) {
            LOGGER.error(String.format(CHECK, bin, e.toString()), e);
            return false;
        }

        if (check.exitValue() != VERSION_EXIT) {
            LOGGER.error(String.format(CHECK, bin, String.format("Command '%s --version' did not exit with 255", bin)));
            return false;
        }

        return true;
    }
}
