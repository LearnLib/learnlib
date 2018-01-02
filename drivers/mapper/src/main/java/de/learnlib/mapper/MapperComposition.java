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
 * Utility class for the nested the application of two {@link Mapper mappers}.
 * <p>
 * This class implements both {@link SynchronousMapper} and {@link AsynchronousMapper} since for the nesting semantics
 * is the same for both processing contracts.
 *
 * @param <AI>
 *         abstract (outer) input type
 * @param <AO>
 *         abstract (outer) output type
 * @param <ACI>
 *         intermediate input type
 * @param <CAO>
 *         intermediate output type
 * @param <CI>
 *         concrete (inner) input type
 * @param <CO>
 *         concrete (inner) output output
 * @param <OUTER>
 *         type of the outer mapper
 * @param <INNER>
 *         type of the inner mapper
 *
 * @author frohme
 */
class MapperComposition<AI, AO, ACI, CAO, CI, CO, OUTER extends Mapper<? super AI, ? extends AO, ACI, CAO>, INNER extends Mapper<? super ACI, ? extends CAO, ? extends CI, ? super CO>>
        implements SynchronousMapper<AI, AO, CI, CO>, AsynchronousMapper<AI, AO, CI, CO> {

    protected final OUTER mapper1;
    protected final INNER mapper2;

    MapperComposition(OUTER outerMapper, INNER innerMapper) {
        this.mapper1 = outerMapper;
        this.mapper2 = innerMapper;
    }

    @Override
    public void pre() {
        mapper1.pre();
        mapper2.pre();
    }

    @Override
    public void post() {
        mapper2.post();
        mapper1.post();
    }

    @Override
    public CI mapInput(AI abstractInput) {
        ACI aci = mapper1.mapInput(abstractInput);
        return mapper2.mapInput(aci);
    }

    @Override
    public AO mapOutput(CO concreteOutput) {
        CAO cao = mapper2.mapOutput(concreteOutput);
        return mapper1.mapOutput(cao);
    }

}
