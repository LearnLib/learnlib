/* Copyright (C) 2017 TU Dortmund
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
package de.learnlib.algorithms.discriminationtree.hypothesis.vpda;

/**
 * @param <I> input symbol type
 *
 * @author Malte Isberner
 */
public class DTree<I> {

	private final DTNode<I> root = new DTNode<>(null, false);

	public DTNode<I> getRoot() {
		return root;
	}

	public DTNode<I> computeLCA(DTNode<I> a, DTNode<I> b) {
		if (a.getDepth() > b.getDepth()) {
			DTNode<I> tmp = a;
			a = b;
			b = tmp;
		}

		while (b.getDepth() > a.getDepth()) {
			b = b.getParent();
		}

		while (a != b) {
			a = a.getParent();
			b = b.getParent();
		}

		return a;
	}

}
