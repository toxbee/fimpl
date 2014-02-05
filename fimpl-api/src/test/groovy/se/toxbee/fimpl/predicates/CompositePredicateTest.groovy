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
	static class cp extends CompositePredicate<Object, Boolean> {
		cp( Boolean... matchers ) {
			super( matchers )
		}

		@Override
		protected boolean match( Boolean matcher, ImplementationInformation info, ImplementationResultSet<Object, ?> set ) {
			return matcher
		}
	}

	def "Match"() {
		given:
			def info = Mock(ImplementationInformation)
			def set = Mock(ImplementationResultSet)
			def pred = new cp( (Boolean[]) matchers );
		expect:
			pred.match( info, set, true )   == any
			pred.match( info, set, false )  == all
		where:
			matchers                |   any     |   all
			[true, true, true]      |   true    |   true
			[true, true, false]     |   true    |   false
			[false, false, false]   |   false   |   false
	}
}
