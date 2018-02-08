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
package de.learnlib.datastructure.pta.pta;

import java.text.MessageFormat;

/**
 * Exception to signal that two (state or transition) properties in a {@link BasePTA PTA} cannot be merged since they
 * are incompatible.
 * <p>
 *
 * @author Malte Isberner
 */
public class PropertyConflictException extends IllegalArgumentException {

    private static final long serialVersionUID = 1L;

    public PropertyConflictException(Object oldProperty, Object newProperty) {
        super(MessageFormat.format("Cannot merge incompatible properties {0} and {1}", oldProperty, newProperty));
    }

}
