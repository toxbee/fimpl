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

package se.toxbee.fimpl

import se.toxbee.fimpl.impl.ImplementationFactoryImpl
import spock.lang.Specification

class ImplementationFinderTest extends Specification {
	def "Ctor"() {
		when:
			new ImplementationFinder( null )
		then:
			thrown( NullPointerException )
	}

	def "Provider"() {
		given:
			ImplementationFactory factory = Mock(ImplementationFactory)
		expect:
			new ImplementationFinder( factory ).provider() == factory
	}

	def "FindRaw"() {
		given:
			def nullFinder = new ImplementationFinder( Mock(ImplementationFactory) )
			def iter = [].iterator()
			def factory = new ImplementationFactoryImpl( Mock(ImplementationLoader), { iter } as ImplementationReader )
			def finder = new ImplementationFinder( factory )
		when:
			nullFinder.findRaw( null )
		then:
			0 * nullFinder.findImplementationCollection( _ )
			thrown( NullPointerException )
		expect:
			finder.findRaw( null ) == iter
	}
}
