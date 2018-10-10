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
package de.learnlib.datastructure.discriminationtree;

import java.awt.AWTEvent;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.WindowEvent;
import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;

import net.automatalib.commons.util.system.JVMUtil;
import net.automatalib.visualization.Visualization;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author frohme
 */
public class VisualizationTest {

    @BeforeClass
    public void setupAutoClose() {
        // As soon as we observe an event that indicates a new window, close it to prevent blocking the tests.
        Toolkit.getDefaultToolkit().addAWTEventListener(event -> {
            final WindowEvent windowEvent = (WindowEvent) event;
            final Window w = windowEvent.getWindow();
            w.dispatchEvent(new WindowEvent(w, WindowEvent.WINDOW_CLOSING));
        }, AWTEvent.WINDOW_FOCUS_EVENT_MASK);
    }

    @Test
    public void testVisualization() throws InvocationTargetException, InterruptedException {
        if (JVMUtil.getCanonicalSpecVersion() > 8) {
            throw new SkipException("The headless AWT environment currently only works with Java 8 and below");
        }

        SwingUtilities.invokeAndWait(() -> Visualization.visualize(DummyDT.DT));
    }
}
