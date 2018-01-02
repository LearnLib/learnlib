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

import de.learnlib.api.Mapper;
import de.learnlib.api.Mapper.AsynchronousMapper;
import de.learnlib.api.Mapper.SynchronousMapper;

/**
 * Utility methods for manipulating mappers.
 *
 * @author frohme
 */
public final class Mappers {

    private Mappers() {
        // prevent instantiation
    }

    public static <AI, AO, ACI, CAO, CI, CO> Mapper<AI, AO, CI, CO> compose(Mapper<? super AI, ? extends AO, ACI, CAO> outerMapper,
                                                                            Mapper<? super ACI, ? extends CAO, ? extends CI, ? super CO> innerMapper) {
        return new MapperComposition<>(outerMapper, innerMapper);
    }

    public static <AI, AO, ACI, CAO, CI, CO> AsynchronousMapper<AI, AO, CI, CO> compose(AsynchronousMapper<? super AI, ? extends AO, ACI, CAO> outerMapper,
                                                                                        AsynchronousMapper<? super ACI, ? extends CAO, ? extends CI, ? super CO> innerMapper) {
        return new MapperComposition<>(outerMapper, innerMapper);
    }

    public static <AI, AO, ACI, CAO, CI, CO> SynchronousMapper<AI, AO, CI, CO> compose(SynchronousMapper<? super AI, ? extends AO, ACI, CAO> outerMapper,
                                                                                       SynchronousMapper<? super ACI, ? extends CAO, ? extends CI, ? super CO> innerMapper) {
        return new MapperComposition<>(outerMapper, innerMapper);
    }
}
