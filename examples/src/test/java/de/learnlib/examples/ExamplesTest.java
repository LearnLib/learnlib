/* Copyright (C) 2013-2022 TU Dortmund
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
package de.learnlib.examples;

import java.awt.AWTEvent;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Function;

import javax.swing.SwingUtilities;

import de.learnlib.datastructure.observationtable.OTUtils;
import de.learnlib.datastructure.observationtable.ObservationTable;
import mockit.Mock;
import mockit.MockUp;
import net.automatalib.commons.util.system.JVMUtil;
import net.automatalib.modelcheckers.ltsmin.LTSminUtil;
import net.automatalib.modelcheckers.ltsmin.LTSminVersion;
import net.automatalib.words.Word;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author frohme
 */
public class ExamplesTest {

    @BeforeClass
    public void setupAutoClose() {
        // As soon as we observe an event that indicates a new window, close it to prevent blocking the tests.
        Toolkit.getDefaultToolkit().addAWTEventListener(event -> {
            final WindowEvent windowEvent = (WindowEvent) event;
            final Window w = windowEvent.getWindow();
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw new IllegalStateException(e);
            }
            w.dispatchEvent(new WindowEvent(w, WindowEvent.WINDOW_CLOSING));
        }, AWTEvent.WINDOW_FOCUS_EVENT_MASK);
    }

    @Test
    public void testBBCExample1() {
        checkLTSminAvailability(3, 0, 0);
        de.learnlib.examples.bbc.Example1.main(new String[0]);
    }

    @Test
    public void testBBCExample2() {
        checkLTSminAvailability(3, 0, 0);
        de.learnlib.examples.bbc.Example2.main(new String[0]);
    }

    @Test
    public void testBBCExample3() {
        checkLTSminAvailability(3, 0, 0);
        de.learnlib.examples.bbc.Example3.main(new String[0]);
    }

    @Test
    public void testBBCExample4() {
        checkLTSminAvailability(3, 1, 0);
        de.learnlib.examples.bbc.Example4.main(new String[0]);
    }

    @Test
    public void testParallelismExample1() {
        de.learnlib.examples.parallelism.ParallelismExample1.main(new String[0]);
    }

    @Test
    public void testParallelismExample2() {
        de.learnlib.examples.parallelism.ParallelismExample2.main(new String[0]);
    }

    @Test
    public void testPassiveExample1() {
        checkJVMCompatibility();
        de.learnlib.examples.passive.Example1.main(new String[0]);
    }

    @Test
    public void testResumableExample() {
        de.learnlib.examples.resumable.ResumableExample.main(new String[0]);
    }

    @Test
    public void testSLIExample1() {
        checkJVMCompatibility();
        de.learnlib.examples.sli.Example1.main(new String[0]);
    }

    @Test
    public void testSLIExample2() {
        de.learnlib.examples.sli.Example2.main(new String[0]);
    }

    @Test
    public void testExample1() throws Exception {
        checkJVMCompatibility();

        // Mock OTUtils class, so we don't actually open a browser during the test
        new MockUp<OTUtils>() {

            @Mock
            public <I, D> void displayHTMLInBrowser(ObservationTable<I, D> table,
                                                    Function<? super Word<? extends I>, ? extends String> wordToString,
                                                    Function<? super D, ? extends String> outputToString) {
                // do nothing
            }
        };

        SwingUtilities.invokeAndWait(() -> {
            try {
                Example1.main(new String[0]);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    public void testExample2() throws InvocationTargetException, InterruptedException {
        checkJVMCompatibility();
        SwingUtilities.invokeAndWait(() -> {
            try {
                Example2.main(new String[0]);
            } catch (IOException | NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    public void testExample3() throws InvocationTargetException, InterruptedException {
        checkJVMCompatibility();
        SwingUtilities.invokeAndWait(() -> Example3.main(new String[0]));
    }

    private static void checkJVMCompatibility() {
        final int canonicalSpecVersion = JVMUtil.getCanonicalSpecVersion();
        if (!(canonicalSpecVersion <= 8 || canonicalSpecVersion == 11)) {
            throw new SkipException("The headless AWT environment currently only works with Java 11 or <=8");
        }
    }

    private static void checkLTSminAvailability(int major, int minor, int patch) {
        if (!LTSminUtil.supports(LTSminVersion.of(major, minor, patch))) {
            throw new SkipException("LTSmin is not installed in the proper version");
        }
    }

}
