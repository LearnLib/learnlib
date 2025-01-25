/* Copyright (C) 2013-2025 TU Dortmund University
 * This file is part of LearnLib <https://learnlib.de>.
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
package de.learnlib.example;

import java.awt.AWTEvent;
import java.awt.Desktop;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;

import de.learnlib.example.aaar.AlternatingBitExampleExplicit;
import de.learnlib.example.aaar.AlternatingBitExampleGeneric;
import net.automatalib.common.util.system.JVMUtil;
import net.automatalib.modelchecker.ltsmin.LTSminUtil;
import net.automatalib.modelchecker.ltsmin.LTSminVersion;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class ExamplesTest {

    @BeforeClass
    public void setupAutoClose() {
        if (isJVMCompatible()) {
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
    }

    @Test
    public void testAAARAlternatingBitExampleGeneric() throws InterruptedException, InvocationTargetException {
        requireJVMCompatibility();
        SwingUtilities.invokeAndWait(() -> {
            try {
                AlternatingBitExampleGeneric.main(new String[0]);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    public void testAAARAlternatingBitExampleExplicit() throws InterruptedException, InvocationTargetException {
        requireJVMCompatibility();
        SwingUtilities.invokeAndWait(() -> {
            try {
                AlternatingBitExampleExplicit.main(new String[0]);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    public void testBBCExample1() {
        requireLTSminAvailability(3, 0, 0);
        de.learnlib.example.bbc.Example1.main(new String[0]);
    }

    @Test
    public void testBBCExample2() {
        requireLTSminAvailability(3, 0, 0);
        de.learnlib.example.bbc.Example2.main(new String[0]);
    }

    @Test
    public void testBBCExample3() {
        requireLTSminAvailability(3, 0, 0);
        de.learnlib.example.bbc.Example3.main(new String[0]);
    }

    @Test
    public void testBBCExample4() {
        requireLTSminAvailability(3, 1, 0);
        de.learnlib.example.bbc.Example4.main(new String[0]);
    }

    @Test
    public void testParallelismExample1() {
        de.learnlib.example.parallelism.ParallelismExample1.main(new String[0]);
    }

    @Test
    public void testParallelismExample2() {
        de.learnlib.example.parallelism.ParallelismExample2.main(new String[0]);
    }

    @Test
    public void testPassiveExample1() {
        requireJVMCompatibility();
        de.learnlib.example.passive.Example1.main(new String[0]);
    }

    @Test
    public void testResumableExample() {
        de.learnlib.example.resumable.ResumableExample.main(new String[0]);
    }

    @Test
    public void testSLIExample1() {
        requireJVMCompatibility();
        de.learnlib.example.sli.Example1.main(new String[0]);
    }

    @Test
    public void testSLIExample2() {
        de.learnlib.example.sli.Example2.main(new String[0]);
    }

    @Test
    public void testExample1() throws Exception {
        requireJVMCompatibility();

        SwingUtilities.invokeAndWait(() -> {
            try {
                final Desktop mock = Mockito.mock(Desktop.class);
                Mockito.doNothing().when(mock).browse(Mockito.any());

                try (MockedStatic<Desktop> desktop = Mockito.mockStatic(Desktop.class)) {
                    desktop.when(Desktop::getDesktop).thenReturn(mock);

                    Example1.main(new String[0]);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    public void testExample2() throws InvocationTargetException, InterruptedException {
        requireJVMCompatibility();
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
        requireJVMCompatibility();
        SwingUtilities.invokeAndWait(() -> Example3.main(new String[0]));
    }

    private static boolean isJVMCompatible() {
        return JVMUtil.getCanonicalSpecVersion() == 11;
    }

    private static void requireJVMCompatibility() {
        if (JVMUtil.getCanonicalSpecVersion() != 11) {
            throw new SkipException("The headless AWT environment currently only works with Java 11 or <=8");
        }
    }

    private static void requireLTSminAvailability(int major, int minor, int patch) {
        if (!LTSminUtil.supports(LTSminVersion.of(major, minor, patch))) {
            throw new SkipException("LTSmin is not installed in the proper version");
        }
    }

}
