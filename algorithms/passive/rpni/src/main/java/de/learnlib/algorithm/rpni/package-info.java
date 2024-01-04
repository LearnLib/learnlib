/* Copyright (C) 2013-2024 TU Dortmund University
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

/**
 * This package provides the implementation of (a blue-fringe version of) the "regular positive negative inference"
 * (RPNI) learning algorithm as presented in the paper <a href="https://dx.doi.org/10.1142/9789812797902_0004">Inferring
 * regular languages in polynomial update time</a> by Jose Oncina and Pedro García, including merging heuristics such as
 * the "evidence-driven state merging" (EDSM) and "minimum description length" (MDL) strategies.
 * <p>
 * More details on these implementations can be found in the book <a
 * href="https://doi.org/10.1017/CBO9781139194655">Grammatical Inference</a> by Colin de la Higuera.
 */
package de.learnlib.algorithm.rpni;
