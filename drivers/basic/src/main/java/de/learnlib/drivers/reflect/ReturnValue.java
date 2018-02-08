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

import java.util.Objects;

/**
 * Error output.
 *
 * @author falkhowar
 */
public class ReturnValue extends AbstractMethodOutput {

    private final Object ret;

    private final String id;

    public ReturnValue(Object ret) {
        this.ret = ret;
        this.id = String.valueOf(ret);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 19 * hash + Objects.hashCode(this.id);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ReturnValue other = (ReturnValue) obj;
        return Objects.equals(this.id, other.id);
    }

    @Override
    public String toString() {
        return this.id;
    }

    /**
     * @return the cause
     */
    public Object getValue() {
        return ret;
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }
}
