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
package de.learnlib.oracle.emptiness;

import de.learnlib.api.query.DefaultQuery;
import de.learnlib.oracle.AbstractBreadthFirstOracleTest;
import net.automatalib.modelchecking.Lasso;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Jeroen Meijer
 */
public abstract class AbstractLassoAutomatonEmptinessTest<L extends Lasso<?, ?, String, D>, D> extends
                                                                                               AbstractBreadthFirstOracleTest<D> {

    public static final Alphabet<String> ALPHABET = Alphabets.fromArray("a");

    private AbstractLassoAutomatonEmptinessOracle<L, ?, String, ?> laeo;

    private L lasso;

    private DefaultQuery<String, ?> query;

    protected abstract AbstractLassoAutomatonEmptinessOracle<L, ?, String, ?> createLassoAutomatonEmptinessOracle();

    protected abstract L createLasso();

    protected abstract DefaultQuery<String, ?> createQuery();

    @BeforeMethod
    public void setUp() {
        super.setUp();
        laeo = createLassoAutomatonEmptinessOracle();
        lasso = createLasso();
        query = createQuery();
    }

    @Test
    public void testFindCounterExample() {
        final DefaultQuery cex = laeo.findCounterExample(lasso, ALPHABET);
        Assert.assertEquals(query, cex);
    }

}
