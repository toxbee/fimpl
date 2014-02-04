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

import se.toxbee.fimpl.ImplementationReader
import se.toxbee.fimpl.ImplementationResultSet
import se.toxbee.fimpl.common.ImplementationInformation
import se.toxbee.fimpl.impl.ImplementationFactoryImpl
import se.toxbee.fimpl.impl.StandardClassLoader
import spock.lang.Specification

class InterfacePredicateTest extends Specification {
	static interface iface1 {}
	static interface iface2 {}
	static class impltrue implements iface1, iface2 {}
	static class implfalse implements iface1 {}

	def "Ctor"() {
		when:
			new InterfacePredicate( null )
		then:
			thrown( NullPointerException )
		when:
			new InterfacePredicate( iface2.class )
		then:
			notThrown( NullPointerException )
	}

	def "Match"() {
		given:
			def dummyFactory = new ImplementationFactoryImpl( new StandardClassLoader(), new ImplementationReader() {
				@Override
				def <I> Iterator<ImplementationInformation> readImplementationCollection( Class<I> interfase ) {
					return null;
				}
			} )
			def set = new ImplementationResultSet.Impl<iface1>( dummyFactory, iface1.class, null )
			def pred = new InterfacePredicate<iface1>( iface2.class )

		expect:
			pred.match( new ImplementationInformation.Impl( info.getName(), 0 ), set, false ) == ex

		where:
			info << [impltrue.class, implfalse.class ]
			ex << [true, false]
	}
}
