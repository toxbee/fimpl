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

package se.toxbee.fimpl.predicates

import se.toxbee.fimpl.ImplementationResultSet
import se.toxbee.fimpl.common.ImplementationInformation
import spock.lang.Specification
/**
 *
 * @author Centril < twingoow @ gmail.com >  / Mazdak Farrokhzad.
 * @version 1.0
 * @since Feb , 03, 2014
 */
class CompositePredicateTest extends Specification {
	static class cp<I> extends CompositePredicate<I, Boolean> {
		cp( Boolean... matchers ) {
			super( matchers )
		}

		@Override
		protected boolean match( Boolean matcher, ImplementationInformation info, ImplementationResultSet<I, ?> set ) {
			return matcher
		}
	}

	def "Match"() {
		expect:
			(pred as Predicate<?>).match( null, null, true ) == any
			(pred as Predicate<?>).match( null, null, false ) == all

		where:
			pred << [new cp( true, true, true ), new cp( true, true, false ), new cp( false, false, false )]
			any << [true, true, false]
			all << [true, false, false]
	}
}
