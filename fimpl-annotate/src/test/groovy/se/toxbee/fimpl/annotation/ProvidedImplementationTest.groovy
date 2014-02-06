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

package se.toxbee.fimpl.annotation
import spock.lang.Specification

class ProvidedImplementationTest extends Specification {
	interface iface_1 {}

	@ProvidedImplementationDouble
	class default_annotated implements iface_1 {}

	def "void-definition-test"() {
		expect:
			void.class == Void.TYPE
			Void.TYPE != Void.class
	}

	ProvidedImplementationDouble getPI( Class<?> aClass ) {
		aClass.getAnnotation( ProvidedImplementationDouble )
	}

	def "hasAnnotation"() {
		given:
			ProvidedImplementationDouble pi = getPI( default_annotated )
		expect:
			pi != null
			pi.of() == void.class
			pi.priority() == 0
			pi.type() == ""
	}
}
