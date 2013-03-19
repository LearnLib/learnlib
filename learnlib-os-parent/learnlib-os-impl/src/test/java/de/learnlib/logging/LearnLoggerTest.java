/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.learnlib.logging;

import de.learnlib.logging.flter.CategoryFilter;
import java.util.EnumSet;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author falkhowar
 */
public class LearnLoggerTest {
    
    public LearnLoggerTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of getLogger method, of class LearnLogger.
     */
    @Test
    public void testGetLogger() {
        System.out.println("getLogger");
        String name = LearnLoggerTest.class.getName();
        LearnLogger expResult = LearnLogger.getLogger(name);
        LearnLogger result = LearnLogger.getLogger(name);
        assertEquals(expResult, result);
    }

    /**
     * Test of logPhase method, of class LearnLogger.
     */
    @Test
    public void testLogPhase() {
        System.out.println("logPhase");
        LearnLogger.defaultSetup();
        LearnLogger.setGlobalFilter(new CategoryFilter(EnumSet.of(Category.PHASE)));
        LearnLogger logger = LearnLogger.getLogger(LearnLoggerTest.class.getName());
        TestHandler th = new TestHandler(new CategoryFilter(EnumSet.of(Category.PHASE)));
        logger.addHandler(th);
        logger.logPhase("test phase");
        logger.removeHandler(th);
        assertNotNull(th.getLastMessage());
    }

    /**
     * Test of logQuery method, of class LearnLogger.
     */
    @Test
    public void testLogQuery() {
        System.out.println("logQuery");
        LearnLogger.defaultSetup();
        LearnLogger.setGlobalFilter(new CategoryFilter(EnumSet.of(Category.PHASE)));
        LearnLogger logger = LearnLogger.getLogger(LearnLoggerTest.class.getName());
        TestHandler th = new TestHandler(new CategoryFilter(EnumSet.of(Category.PHASE)));
        logger.addHandler(th);
        logger.logQuery("test query");
        logger.removeHandler(th);
        assertNull(th.getLastMessage());
    }
}
