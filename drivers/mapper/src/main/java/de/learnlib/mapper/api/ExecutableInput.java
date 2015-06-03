/* Copyright (C) 2013 TU Dortmund
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
package de.learnlib.mapper.api;

import de.learnlib.api.SULException;


/**
 * An executable input is a concrete input produced by a data mapper
 * and can be executed directly. 
 * 
 * @author falkhowar
 * 
 * @param <CO> concrete output 
 */
public interface ExecutableInput<CO> {
   
    /**
     * executes the input.
     * 
     * @return concrete output for this input 
     */
    public CO execute() throws SULException, Exception;
    
}
