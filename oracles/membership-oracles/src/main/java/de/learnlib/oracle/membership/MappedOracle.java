/* Copyright (C) 2013-2017 TU Dortmund
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
package de.learnlib.oracle.membership;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.learnlib.api.Mapper;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.api.query.DefaultQuery;
import de.learnlib.api.query.Query;

/**
 * A utility class that allows to lift a membership oracle of concrete input/output symbols to a membership oracle of
 * abstract input/output symbols, given a corresponding mapper.
 *
 * @param <AI>
 *         abstract input symbol type.
 * @param <AO>
 *         abstract output symbol type.
 * @param <CI>
 *         concrete input symbol type.
 * @param <CO>
 *         concrete output symbol type.
 *
 * @author frohme
 */
public class MappedOracle<AI, AO, CI, CO> implements MembershipOracle<AI, AO> {

    private final MembershipOracle<CI, CO> delegate;

    private final Mapper<AI, AO, CI, CO> mapper;

    public MappedOracle(MembershipOracle<CI, CO> delegate, Mapper<AI, AO, CI, CO> mapper) {
        this.delegate = delegate;
        this.mapper = mapper;
    }

    @Override
    public void processQueries(Collection<? extends Query<AI, AO>> queries) {
        final List<Query<AI, AO>> orderedQueries = new ArrayList<>(queries);
        final List<DefaultQuery<CI, CO>> mappedQueries = new ArrayList<>(queries.size());

        for (final Query<AI, AO> q : orderedQueries) {
            mappedQueries.add(new DefaultQuery<>(q.getPrefix().transform(mapper::mapInput),
                                                 q.getSuffix().transform(mapper::mapInput)));
        }

        this.delegate.processQueries(mappedQueries);

        for (int i = 0; i < orderedQueries.size(); i++) {
            orderedQueries.get(i).answer(mapper.mapOutput(mappedQueries.get(i).getOutput()));
        }
    }
}
