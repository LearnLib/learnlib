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

package de.learnlib.api.logging;

import java.io.ByteArrayOutputStream;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.turbo.MarkerFilter;
import ch.qos.logback.core.OutputStreamAppender;
import ch.qos.logback.core.spi.FilterReply;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author falkhowar
 */
public class LearnLoggerTest {

    /**
     * Test of getLogger method, of class LearnLogger.
     */
    @Test
    public void testGetLogger() {
        String name = LearnLoggerTest.class.getName();
        LearnLogger expResult = LearnLogger.getLogger(name);
        LearnLogger result = LearnLogger.getLogger(name);
        Assert.assertEquals(expResult, result);
    }

    /**
     * Test of logPhase method, of class LearnLogger.
     */
    @Test
    public void testLogPhase() {
        final LearnLogger learnLogger = LearnLogger.getLogger(LearnLoggerTest.class);
        final Logger logbackLogger = (Logger) LoggerFactory.getLogger(LearnLoggerTest.class);
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream(128);
        final OutputStreamAppender<ILoggingEvent> appender = buildAppender(outputStream);

        final String logStatement = "test phase";

        logbackLogger.addAppender(appender);
        learnLogger.logPhase(logStatement);

        final String loggedOutput = new String(outputStream.toByteArray());

        Assert.assertNotNull(loggedOutput);
        Assert.assertEquals(logStatement, loggedOutput);
    }

    private OutputStreamAppender<ILoggingEvent> buildAppender(final ByteArrayOutputStream outputStream) {
        final LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

        final PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(context);
        encoder.setPattern("%msg");
        encoder.start();

        final OutputStreamAppender<ILoggingEvent> appender = new OutputStreamAppender<>();
        appender.setName("Dummy Appender");
        appender.setContext(context);
        appender.setEncoder(encoder);
        appender.setOutputStream(outputStream);

        appender.start();

        return appender;
    }

    /**
     * Test of logQuery method, of class LearnLogger.
     */
    @Test
    public void testLogQuery() {
        final LearnLogger learnLogger = LearnLogger.getLogger(LearnLoggerTest.class);
        final Logger logbackLogger = (Logger) LoggerFactory.getLogger(LearnLoggerTest.class);
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream(128);
        final OutputStreamAppender<ILoggingEvent> appender = buildAppender(outputStream);

        // Add marker filter that only allows messages marked as phase
        final LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        final MarkerFilter phaseMarker = buildMarkerFilter(Category.PHASE);
        context.addTurboFilter(phaseMarker);

        final String logStatement = "test query";

        logbackLogger.addAppender(appender);
        learnLogger.logQuery(logStatement);

        final String loggedOutput = new String(outputStream.toByteArray());

        Assert.assertNotNull(loggedOutput);
        Assert.assertTrue(loggedOutput.isEmpty());
    }

    private MarkerFilter buildMarkerFilter(final Category category) {
        final MarkerFilter result = new MarkerFilter();

        result.setMarker(Category.PHASE.toMarkerLabel());
        result.setOnMatch(FilterReply.ACCEPT.name());
        result.setOnMismatch(FilterReply.DENY.name());
        result.start();

        return result;
    }
}
