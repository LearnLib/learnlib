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
package de.learnlib.mapper;

import de.learnlib.api.SUL;
import de.learnlib.mapper.api.SULMapper;

/**
 * Utility methods for manipulating mappers.
 *
 * @author Malte Isberner
 */
public final class SULMappers {

    private SULMappers() {
        throw new AssertionError("Constructor should not be invoked");
    }

    public static <AI, AO, ACI, CAO, CI, CO> SULMapper<AI, AO, CI, CO> compose(SULMapper<? super AI, ? extends AO, ACI, CAO> outerMapper,
                                                                               SULMapper<? super ACI, ? extends CAO, ? extends CI, ? super CO> innerMapper) {
        return new SULMapperComposition<>(outerMapper, innerMapper);
    }

    public static <AI, AO, CI, CO> SUL<AI, AO> apply(SULMapper<? super AI, ? extends AO, CI, CO> mapper,
                                                     SUL<? super CI, ? extends CO> sul) {
        return new MappedSUL<>(mapper, sul);
    }

}
