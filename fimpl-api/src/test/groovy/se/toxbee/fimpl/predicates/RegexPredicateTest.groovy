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

import se.toxbee.fimpl.common.ImplementationInformation
import spock.lang.Shared
import spock.lang.Specification

import java.util.regex.Pattern

class RegexPredicateTest extends Specification {
	def @Shared pattern = Pattern.compile( "^text/.+\$" )
	def @Shared transformer = new TypeTransformer()

	def "Ctor"() {
		when:
			new RegexPredicate( transformer, pattern )
		then:
			notThrown( NullPointerException )
		when:
			new RegexPredicate( null, pattern )
		then:
			thrown( NullPointerException )
		when:
			new RegexPredicate( transformer, null )
		then:
			thrown( NullPointerException )
	}

	def "Match"() {
		given:
			def pred = new RegexPredicate( transformer, pattern )
			def name = Object.class.getName()
		expect:
			pred.match( new ImplementationInformation.Impl( name, 0, type ), null, false ) == match
		where:
			type                |   match
			"text/javascript"   |   true
			"image/gif"         |   false
	}
}
