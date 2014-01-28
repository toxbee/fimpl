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

import static se.toxbee.fimpl.util.Util.equal;
import static se.toxbee.fimpl.util.Util.guardNull;

/**
 * EqualsCompositePredicate provides predicates of type A.<br/>
 * It matches by using {@link se.toxbee.fimpl.util.Util#equal(Object, Object)}
 *
 * @param <I> the interface type of the set.
 * @param <A> the type of the composite indices.
 * @author Centril<twingoow@gmail.com> / Mazdak Farrokhzad.
 * @version 1.0
 * @since Jan, 24, 2014
 */
public class EqualsPredicate<I, A> implements Predicate<I> {
	protected final PredicateInputTransformer<I, A> transformer;
	protected final A matcher;

	/**
	 * Constructs the predicates given the array of matcher objects.
	 *
	 * @param transformer the transformer to use for equals.
	 * @param matcher the object to match against, null is allowed.
	 */
	public EqualsPredicate( PredicateInputTransformer<I, A> transformer, A matcher ) {
		this.matcher = matcher;
		this.transformer = guardNull( transformer );
	}

	/**
	 * Returns the matcher.
	 *
	 * @return the matcher.
	 */
	public A matcher() {
		return this.matcher;
	}

	@Override
	public boolean match( ImplementationInformation info, ImplementationResultSet<I, ?> set, boolean anyMode ) {
		return equal( this.matcher, this.transformer.transformForPredicate( info, set ) );
	}
}
