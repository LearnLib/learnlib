/* Copyright (C) 2014 TU Dortmund
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
package de.learnlib.discriminationtree;

import java.util.Map;

public class BinaryDTNode<I, D> extends DTNode<I, Boolean, D> {

	public BinaryDTNode(D data) {
		super(data);
	}

	protected BinaryDTNode(DTNode<I, Boolean, D> parent, Boolean parentOutcome,
			D data) {
		super(parent, parentOutcome, data);
	}

	@Override
	protected Map<Boolean, DTNode<I, Boolean, D>> createChildMap() {
		return new BooleanMap<>();
	}

	@Override
	protected DTNode<I, Boolean, D> createChild(Boolean outcome, D data) {
		return new BinaryDTNode<>(this, outcome, data);
	}

}
