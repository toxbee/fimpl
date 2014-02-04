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

package se.toxbee.fimpl.common

import spock.lang.Shared
import spock.lang.Specification

class ImplTest extends Specification {
	def  @Shared interfase = "com.example.MyInterface"

	def "GetImplementorClass"() {
		when:
			new ImplementationInformation.Impl(d, 0)
		then:
			thrown(IllegalArgumentException)
		expect:
			interfase == new ImplementationInformation.Impl(interfase, 0).getImplementorClass()
		where:
			d << [null, ""]
	}

	def "GetPriority"() {
		given:
			def info = new ImplementationInformation.Impl(interfase, d)
		expect:
			info.getPriority() == e
		where:
			d << [-1, 1, 0, Integer.MAX_VALUE, Integer.MIN_VALUE]
			e << [-1, 1, 0, Integer.MAX_VALUE, Integer.MIN_VALUE]
	}

	def "GetType"() {
		given:
			def info = new ImplementationInformation.Impl(interfase, 0)
			def info2 = new ImplementationInformation.Impl(interfase, 0, null)
			def info3 = new ImplementationInformation.Impl(interfase, 0, d)
		expect:
			info.getType() == info2.getType()
			info3.getType() == d
		where:
			d << ["typeA", "typeB"]
	}

	def "GetExtras"() {
		given:
			def info = new ImplementationInformation.Impl(interfase, 0, null)
			def info2 = new ImplementationInformation.Impl(interfase, 0, null, null)
			def info3 = new ImplementationInformation.Impl(interfase, 0, null, d)
		expect:
			info.getExtras() == info2.getExtras()
			info3.getExtras() == d
		where:
			d << ["obj", 1]
	}

	def "HashCode"() {
		expect:
			interfase.hashCode() == new ImplementationInformation.Impl( interfase, 0 ).hashCode()
	}

	def "Equals"() {
		given:
			def info1 = new ImplementationInformation.Impl( interfase, 0 )
			def info2 = new ImplementationInformation.Impl( interfase, 0 )
			def info3 = new ImplementationInformation.Impl( interfase, 0 ) {}
		expect:
			info1.equals( info1 )
			!info1.equals( interfase )
			!info1.equals( null )
			info1.equals( info2  )
			info2.equals( info1  )
			info1.equals( info3  )
	}

	def "CompareTo"() {
		given:
			def a = new ImplementationInformation.Impl( interfase, p1 )
			def b = new ImplementationInformation.Impl( interfase, p2 )
		expect:
			a.compareTo( b ) == e

		where:
			p1 | p2 | e
			1  | 0  | -1
			2  | 1  | -1
			0  | 0  | 0
			1  | 2  | 1
			0  | 1  | 1
	}
}
