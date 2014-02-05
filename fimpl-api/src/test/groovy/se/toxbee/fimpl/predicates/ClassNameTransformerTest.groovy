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
import spock.lang.Specification

class ClassNameTransformerTest extends Specification {
	def "TransformForPredicate"() {
		given:
			def transformer = new ClassNameTransformer()
			ImplementationInformation info = Mock()
			1 * info.getImplementorClass() >> clazz.getName()
		expect:
			transformer.transformForPredicate( info, null ) == clazz.getName()
		where:
			clazz << [Object.class, String.class, Iterator.class]
	}
}
