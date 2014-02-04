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

package se.toxbee.fimpl.impl

import se.toxbee.fimpl.ImplementationLoader
import se.toxbee.fimpl.ImplementationReader
import se.toxbee.fimpl.common.ImplementationInformation
import spock.lang.Specification

class ImplementationFactoryImplTest extends Specification {
	def "Ctor"() {
		given:
			def l = new ImplementationLoader() {
				@Override
				def <I> Class<? extends I> loadImplementation( ImplementationInformation info, Class<I> targetType ) {
					return null
				}
			}
			def r = new ImplementationReader() {
				@Override
				def <I> Iterator<ImplementationInformation> readImplementationCollection( Class<I> interfase ) {
					return null
				}
			}
			def i = new ImplementationFactoryImpl(l, r)
			assert i.loader() == l
			assert i.reader() == r

		when:
			new ImplementationFactoryImpl(l, null)
		then:
			thrown( NullPointerException )
		when:
			new ImplementationFactoryImpl(null, r)
		then:
			thrown( NullPointerException )
	}
}
