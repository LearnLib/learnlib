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
package de.learnlib.filter.cache;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import de.learnlib.oracle.MembershipOracle;
import de.learnlib.oracle.MembershipOracle.MealyMembershipOracle;
import de.learnlib.oracle.MembershipOracle.MooreMembershipOracle;
import de.learnlib.query.DefaultQuery;
import de.learnlib.query.Query;
import de.learnlib.tooling.annotation.refinement.GenerateRefinement;
import de.learnlib.tooling.annotation.refinement.Generic;
import de.learnlib.tooling.annotation.refinement.Interface;
import de.learnlib.tooling.annotation.refinement.Mapping;
import net.automatalib.word.Word;

/**
 * A {@link MembershipOracle} that interns query outputs. May be used to reduce memory consumption of data structures
 * that store a lot of query responses. Typically, this oracle only makes sense for output types that are not already
 * interned by the JVM (such as {@link Boolean}s in case of {@link DFAMembershipOracle}s).
 */
@GenerateRefinement(name = "InterningMealyMembershipOracle",
                    packageName = "de.learnlib.filter.cache.mealy",
                    generics = {@Generic(value = "I", desc = "input symbol type"),
                                @Generic(value = "O", desc = "output symbol type")},
                    parentGenerics = {@Generic("I"), @Generic(clazz = Word.class, generics = "O")},
                    interfaces = @Interface(clazz = MealyMembershipOracle.class,
                                            generics = {@Generic("I"), @Generic("O")}),
                    typeMappings = @Mapping(from = MembershipOracle.class,
                                            to = MealyMembershipOracle.class,
                                            generics = {@Generic("I"), @Generic("O")}))
@GenerateRefinement(name = "InterningMooreMembershipOracle",
                    packageName = "de.learnlib.filter.cache.moore",
                    generics = {@Generic(value = "I", desc = "input symbol type"),
                                @Generic(value = "O", desc = "output symbol type")},
                    parentGenerics = {@Generic("I"), @Generic(clazz = Word.class, generics = "O")},
                    interfaces = @Interface(clazz = MooreMembershipOracle.class,
                                            generics = {@Generic("I"), @Generic("O")}),
                    typeMappings = @Mapping(from = MembershipOracle.class,
                                            to = MooreMembershipOracle.class,
                                            generics = {@Generic("I"), @Generic("O")}))
public class InterningMembershipOracle<I, D> implements MembershipOracle<I, D> {

    private final MembershipOracle<I, D> delegate;
    private final Map<D, WeakReference<D>> cache;

    public InterningMembershipOracle(MembershipOracle<I, D> delegate) {
        this.delegate = delegate;
        this.cache = new WeakHashMap<>();
    }

    @Override
    public void processQueries(Collection<? extends Query<I, D>> queries) {
        final List<DefaultQuery<I, D>> delegates = new ArrayList<>(queries.size());

        for (Query<I, D> q : queries) {
            delegates.add(new DefaultQuery<>(q));
        }

        this.delegate.processQueries(delegates);

        final Iterator<? extends Query<I, D>> origIter = queries.iterator();
        final Iterator<DefaultQuery<I, D>> delegateIter = delegates.iterator();

        while (origIter.hasNext() && delegateIter.hasNext()) {
            final Query<I, D> origNext = origIter.next();
            final DefaultQuery<I, D> delegateNext = delegateIter.next();
            final D delegateOutput = delegateNext.getOutput();

            // Since the GC may free our references during the lookup, repeat until we have a (non-null) cache hit.
            D origOutput;
            do {
                origOutput = cache.computeIfAbsent(delegateOutput, k -> new WeakReference<>(delegateOutput)).get();
            } while (origOutput == null);

            origNext.answer(origOutput);
        }

        assert !origIter.hasNext() && !delegateIter.hasNext();
    }
}
