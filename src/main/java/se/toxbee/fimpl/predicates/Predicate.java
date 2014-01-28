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

import se.toxbee.fimpl.ImplementationInformation;
import se.toxbee.fimpl.ImplementationResultSet;

/**
 * Predicate is a generic predicates class for filtering the set.
 *
 * @param <I> the interface type of the set.
 * @author Centril<twingoow@gmail.com> / Mazdak Farrokhzad.
 * @version 1.0
 * @since Jan, 24, 2014
 */
public interface Predicate<I> {
	/**
	 * Returns true if a match has occured.
	 *
	 * @param info the information to match on.
	 * @param set the entire set.
	 * @param anyMode if Predicate is a composite predicates:
	 *                [true = match when any child predicates matches, false = match when all matches].
	 * @return whether or a match has occured.
	 */
	public boolean match( ImplementationInformation info, ImplementationResultSet<I, ?> set, boolean anyMode );
}
