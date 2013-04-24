/* Copyright (C) 2013 TU Dortmund
 * This file is part of LearnLib, http://www.learnlib.de/.
 * 
 * LearnLib is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 3.0 as published by the Free Software Foundation.
 * 
 * LearnLib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with LearnLib; if not, see
 * <http://www.gnu.de/documents/lgpl.en.html>.
 */

package de.learnlib.logging;


import java.util.EnumSet;


import de.learnlib.logging.filter.CategoryFilter;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

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
    

    /**
     * Test of getLogger method, of class LearnLogger.
     */
    @Test
    public void testGetLogger() {
        System.out.println("getLogger");
        String name = LearnLoggerTest.class.getName();
        LearnLogger expResult = LearnLogger.getLogger(name);
        LearnLogger result = LearnLogger.getLogger(name);
        Assert.assertEquals(expResult, result);
    }

    /**
     * Test of logPhase method, of class LearnLogger.
     */
    @Test
    @SuppressWarnings("deprecation")
    public void testLogPhase() {
        System.out.println("logPhase");
        LearnLogger.defaultSetup();
        LearnLogger.setGlobalFilter(new CategoryFilter(EnumSet.of(Category.PHASE)));
        LearnLogger logger = LearnLogger.getLogger(LearnLoggerTest.class.getName());
        TestHandler th = new TestHandler(new CategoryFilter(EnumSet.of(Category.PHASE)));
        logger.addHandler(th);
        logger.logPhase("test phase");
        logger.removeHandler(th);
        Assert.assertNotNull(th.getLastMessage());
    }

    /**
     * Test of logQuery method, of class LearnLogger.
     */
    @Test
    @SuppressWarnings("deprecation")
    public void testLogQuery() {
        System.out.println("logQuery");
        LearnLogger.defaultSetup();
        LearnLogger.setGlobalFilter(new CategoryFilter(EnumSet.of(Category.PHASE)));
        LearnLogger logger = LearnLogger.getLogger(LearnLoggerTest.class.getName());
        TestHandler th = new TestHandler(new CategoryFilter(EnumSet.of(Category.PHASE)));
        logger.addHandler(th);
        logger.logQuery("test query");
        logger.removeHandler(th);
        Assert.assertNull(th.getLastMessage());
    }
}
