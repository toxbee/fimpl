/*
 * Copyright 2014 toxbee.se
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.toxbee.fimpl.predicates;

import se.toxbee.fimpl.ImplementationResultSet;
import se.toxbee.fimpl.ImplementationInformation;

/**
 * TypeTransformer is a PredicateInputTransformer for types.
 *
 * @param <I> the interface type of the set.
 * @author Centril<twingoow@gmail.com> / Mazdak Farrokhzad.
 * @version 1.0
 * @since Jan, 24, 2014
 */
public class TypeTransformer<I> implements PredicateInputTransformer<I, String> {
	@Override
	public String transformForPredicate( ImplementationInformation info, ImplementationResultSet<I, ?> set ) {
		return info.getType();
	}
}