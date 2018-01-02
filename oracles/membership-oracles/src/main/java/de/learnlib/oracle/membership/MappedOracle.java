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
package de.learnlib.oracle.membership;

import de.learnlib.api.Mapper.AsynchronousMapper;
import de.learnlib.api.oracle.QueryAnswerer;
import de.learnlib.api.oracle.SingleQueryOracle;
import net.automatalib.words.Word;

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
public class MappedOracle<AI, AO, CI, CO> implements SingleQueryOracle<AI, AO> {

    private final QueryAnswerer<CI, CO> delegate;

    private final AsynchronousMapper<AI, AO, CI, CO> mapper;

    public MappedOracle(QueryAnswerer<CI, CO> delegate, AsynchronousMapper<AI, AO, CI, CO> mapper) {
        this.delegate = delegate;
        this.mapper = mapper;
    }

    @Override
    public AO answerQuery(Word<AI> prefix, Word<AI> suffix) {
        mapper.pre();

        final CO output = delegate.answerQuery(prefix.transform(mapper::mapInput), suffix.transform(mapper::mapInput));
        final AO result = mapper.mapOutput(output);

        mapper.post();

        return result;
    }
}
