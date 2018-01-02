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
package de.learnlib.drivers.reflect;

/**
 * Unobserved indicates that the corresponding input was not executed on the system. This usually happens after an
 * exception occurred.
 *
 * @author falkhowar
 */
public final class Unobserved extends AbstractMethodOutput {

    public static final Unobserved INSTANCE = new Unobserved();

    private Unobserved() {
    }

    @Override
    public String toString() {
        return "unobserved";
    }

}
