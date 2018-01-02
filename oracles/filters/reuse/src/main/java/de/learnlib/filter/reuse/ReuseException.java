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
package de.learnlib.filter.reuse;

/**
 * This exception will be thrown whenever some nondeterministic behavior in the reuse tree is detected when inserting
 * new queries.
 *
 * @author Oliver Bauer
 */
public class ReuseException extends IllegalArgumentException {

    private static final long serialVersionUID = 3661716306694750282L;

    public ReuseException(String string) {
        super(string);
    }
}
