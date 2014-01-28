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

import static se.toxbee.fimpl.util.Util.guardNull;
import static se.toxbee.fimpl.util.Util.equal;

/**
 * EqualsCompositePredicate provides "composite" predicates of type A.<br/>
 * It matches by using {@link se.toxbee.fimpl.util.Util#equal(Object, Object)}
 *
 * @param <I> the interface type of the set.
 * @param <A> the type of the composite indices.
 * @author Centril<twingoow@gmail.com> / Mazdak Farrokhzad.
 * @version 1.0
 * @since Jan, 24, 2014
 */
public class EqualsCompositePredicate<I, A> extends CompositePredicate<I, A> {
	protected final PredicateInputTransformer<I, A> transformer;

	/**
	 * Constructs the predicates given the array of matcher objects.
	 *
	 * @param transformer the transformer to use for equals.
	 * @param matchers the objects to match against.
	 */
	public EqualsCompositePredicate( PredicateInputTransformer<I, A> transformer, A... matchers ) {
		super( matchers );
		this.transformer = guardNull( transformer );
	}

	@Override
	protected boolean match( A matcher, ImplementationInformation info, ImplementationResultSet<I, ?> set ) {
		return equal( matcher, this.transformer.transformForPredicate( info, set ) );
	}
}
