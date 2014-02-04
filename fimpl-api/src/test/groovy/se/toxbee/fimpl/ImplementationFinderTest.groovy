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

import se.toxbee.fimpl.common.ImplementationInformation
import se.toxbee.fimpl.impl.ImplementationFactoryImpl
import spock.lang.Specification

class ImplementationFinderTest extends Specification {
	def "Ctor"() {
		when:
			new ImplementationFinder( null )
		then:
			thrown( NullPointerException )
	}

	def "FindRaw"() {
		given:
			def nullFactory = new ImplementationFactory() {
				@Override
				ImplementationLoader loader() {
					return null
				}

				@Override
				ImplementationReader reader() {
					return null
				}
			}
			def nullLoader = new ImplementationLoader() {
				@Override
				def <I> Class<? extends I> loadImplementation( ImplementationInformation info, Class<I> targetType ) {
					return null
				}
			}
			def nullFinder = new ImplementationFinder( nullFactory )
			def dummyInfo = new ImplementationInformation.Impl( "com.example.MyInterface", 0 )
			def dummyIter = Arrays.asList( dummyInfo,dummyInfo,dummyInfo ).iterator()
			def dummyFactory = new ImplementationFactoryImpl( nullLoader, new ImplementationReader() {
				@Override
				def <I> Iterator<ImplementationInformation> readImplementationCollection( Class<I> interfase ) {
					return dummyIter;
				}
			} )
			def dummyFinder = new ImplementationFinder( dummyFactory )

		when:
			nullFinder.findRaw( null )
		then:
			thrown( NullPointerException )
		expect:
			dummyFinder.findRaw( null ) == dummyIter
	}
}
