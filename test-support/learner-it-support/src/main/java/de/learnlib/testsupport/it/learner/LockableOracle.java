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
package de.learnlib.testsupport.it.learner;

import java.util.Collection;

import de.learnlib.oracle.MembershipOracle;
import de.learnlib.oracle.SingleQueryOracle.SingleQueryOracleDFA;
import de.learnlib.oracle.SingleQueryOracle.SingleQueryOracleMealy;
import de.learnlib.oracle.SingleQueryOracle.SingleQueryOracleMoore;
import de.learnlib.query.Query;
import de.learnlib.tooling.annotation.refinement.GenerateRefinement;
import de.learnlib.tooling.annotation.refinement.Generic;
import de.learnlib.tooling.annotation.refinement.Interface;
import net.automatalib.word.Word;

/**
 * A lockable oracle is a {@link MembershipOracle} whose actions can be blocked by setting its {@link #lock() lock}.
 * During testing, this can be helpful for detecting whether learning algorithms are posing queries beyond hypothesis
 * construction (which they really should not).
 *
 * @param <I>
 *         input symbol type
 * @param <D>
 *         output domain
 */
@GenerateRefinement(name = "DFALockableOracle",
                    generics = @Generic(value = "I", desc = "input symbol type"),
                    parentGenerics = {@Generic("I"), @Generic(clazz = Boolean.class)},
                    interfaces = @Interface(clazz = SingleQueryOracleDFA.class, generics = @Generic("I")))
@GenerateRefinement(name = "MealyLockableOracle",
                    generics = {@Generic(value = "I", desc = "input symbol type"),
                                @Generic(value = "O", desc = "output symbol type")},
                    parentGenerics = {@Generic("I"), @Generic(clazz = Word.class, generics = "O")},
                    interfaces = @Interface(clazz = SingleQueryOracleMealy.class,
                                            generics = {@Generic("I"), @Generic("O")}))
@GenerateRefinement(name = "MooreLockableOracle",
                    generics = {@Generic(value = "I", desc = "input symbol type"),
                                @Generic(value = "O", desc = "output symbol type")},
                    parentGenerics = {@Generic("I"), @Generic(clazz = Word.class, generics = "O")},
                    interfaces = @Interface(clazz = SingleQueryOracleMoore.class,
                                            generics = {@Generic("I"), @Generic("O")}))
@GenerateRefinement(name = "SBALockableOracle",
                    generics = @Generic(value = "I", desc = "input symbol type"),
                    parentGenerics = {@Generic("I"), @Generic(clazz = Boolean.class)},
                    interfaces = @Interface(clazz = SingleQueryOracleDFA.class, generics = @Generic("I")))
@GenerateRefinement(name = "SEVPALockableOracle",
                    generics = @Generic(value = "I", desc = "input symbol type"),
                    parentGenerics = {@Generic("I"), @Generic(clazz = Boolean.class)},
                    interfaces = @Interface(clazz = SingleQueryOracleDFA.class, generics = @Generic("I")))
@GenerateRefinement(name = "SPALockableOracle",
                    generics = @Generic(value = "I", desc = "input symbol type"),
                    parentGenerics = {@Generic("I"), @Generic(clazz = Boolean.class)},
                    interfaces = @Interface(clazz = SingleQueryOracleDFA.class, generics = @Generic("I")))
@GenerateRefinement(name = "SPMMLockableOracle",
                    generics = {@Generic(value = "I", desc = "input symbol type"),
                                @Generic(value = "O", desc = "output symbol type")},
                    parentGenerics = {@Generic("I"), @Generic(clazz = Word.class, generics = "O")},
                    interfaces = @Interface(clazz = SingleQueryOracleMealy.class,
                                            generics = {@Generic("I"), @Generic("O")}))
public class LockableOracle<I, D> implements MembershipOracle<I, D> {

    private final MembershipOracle<I, D> delegate;
    private boolean lock;

    /**
     * Constructor.
     *
     * @param delegate
     *         the oracle to which operations should be delegated
     */
    public LockableOracle(MembershipOracle<I, D> delegate) {
        this.delegate = delegate;
    }

    /**
     * Locks this oracle. After this call, all operations will throw an {@link IllegalStateException}.
     */
    public void lock() {
        this.lock = true;
    }

    /**
     * Unlocks this oracle. After this call, all operations will the delegated as usual.
     */
    public void unlock() {
        this.lock = false;
    }

    protected void requireLock() {
        if (lock) {
            throw new IllegalStateException("Membership oracle should not be queried");
        }
    }

    @Override
    public D answerQuery(Word<I> input) {
        requireLock();
        return this.delegate.answerQuery(input);
    }

    @Override
    public D answerQuery(Word<I> prefix, Word<I> suffix) {
        requireLock();
        return this.delegate.answerQuery(prefix, suffix);
    }

    @Override
    public void processQuery(Query<I, D> query) {
        requireLock();
        this.delegate.processQuery(query);
    }

    @Override
    public void processQueries(Collection<? extends Query<I, D>> queries) {
        requireLock();
        this.delegate.processQueries(queries);
    }

    @Override
    public MembershipOracle<I, D> asOracle() {
        requireLock();
        return this.delegate.asOracle();
    }

    @Override
    public void processBatch(Collection<? extends Query<I, D>> batch) {
        requireLock();
        this.delegate.processBatch(batch);
    }
}
