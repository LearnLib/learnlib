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
package de.learnlib.drivers.reflect;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;

import net.automatalib.words.Word;

import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import de.learnlib.api.Query;
import de.learnlib.oracles.DefaultQuery;
import de.learnlib.oracles.SULOracle;

/**
 *
 * @author falk
 */
public class ObjectTest {
    
    public ObjectTest() {
    }

     @Test
     public void testDriver() throws Exception {
     
         Constructor<?> c = Stack.class.getConstructor(int.class);
         SimplePOJOTestDriver driver = new SimplePOJOTestDriver(c, 2);
         SULOracle<AbstractMethodInput, AbstractMethodOutput> oracle = new SULOracle<>(driver);
         
         Method push = Stack.class.getMethod("push", new Class<?>[] {Object.class});
         AbstractMethodInput push_1 = driver.addInput("push_1", push, 1);
         AbstractMethodInput push_2 = driver.addInput("push_2", push, 2);
         
         Method _pop = Stack.class.getMethod("pop", new Class<?>[] {});
         AbstractMethodInput pop = driver.addInput("pop", _pop, new Object[] {});
                  
         DefaultQuery<AbstractMethodInput, Word<AbstractMethodOutput>> query1 = new DefaultQuery<>(
                Word.fromSymbols(push_1, push_2, pop, pop, pop, pop));
         DefaultQuery<AbstractMethodInput, Word<AbstractMethodOutput>> query2 = new DefaultQuery<>(
                Word.fromSymbols(push_1, push_2, push_1, pop));
         
         Collection<Query<AbstractMethodInput, Word<AbstractMethodOutput>>> queries = new ArrayList<>();
         queries.add(query1);
         queries.add(query2);
         
         oracle.processQueries(queries);

         System.out.println(query1.getInput() + "  :  " + query1.getOutput());
         System.out.println(query2.getInput() + "  :  " + query2.getOutput());
     }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }
}