/* Copyright (C) 2013 TU Dortmund
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
