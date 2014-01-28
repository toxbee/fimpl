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
 * CompositePredicate provides "composite" predicates of type A.
 *
 * @param <I> the interface type of the set.
 * @param <A> the type of the composite indices.
 * @author Centril<twingoow@gmail.com> / Mazdak Farrokhzad.
 * @version 1.0
 * @since Jan, 24, 2014
 */
public abstract class CompositePredicate<I, A> implements Predicate<I> {
	protected final A[] matchers;

	/**
	 * Constructs the predicates given the array of matcher objects.
	 *
	 * @param matchers the objects to match against.
	 */
	public CompositePredicate( A... matchers ) {
		this.matchers = matchers;
	}

	/**
	 * Returns the array of matchers.
	 * Does not clone the array.
	 *
	 * @return the matchers.
	 */
	public A[] matchers() {
		return this.matchers;
	}

	@Override
	public boolean match( ImplementationInformation info, ImplementationResultSet<I, ?> set, boolean anyMode ) {
		if ( anyMode ) {
			for ( A m : this.matchers ) {
				if ( this.match( m, info, set ) ) {
					return true;
				}
			}
			return false;
		} else {
			for ( A m : this.matchers ) {
				if ( !this.match( m, info, set ) ) {
					return false;
				}
			}
			return true;
		}
	}

	/**
	 * Returns true if a match has occurred.
	 *
	 * @param matcher the matcher that is being evaluated currently.
	 * @param info the information to match on.
	 * @param set the entire set.
	 * @return true if a match occured.
	 */
	protected abstract boolean match( A matcher, ImplementationInformation info, ImplementationResultSet<I, ?> set );
}
