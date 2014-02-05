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

import se.toxbee.fimpl.common.ImplementationInformation
import spock.lang.Specification

class StandardClassLoaderTest extends Specification {
	def "SetClassloader"() {
		given:
			def l = new StandardClassLoader()
			def ctxcl = Thread.currentThread().getContextClassLoader()
			def mycl = Mock( ClassLoader )
		expect:
			l.getClassLoader() == ctxcl
		when:
			l.setClassloader( null )
		then:
			l.classLoader == ctxcl
		when:
			l.setClassloader( mycl )
		then:
			l.classLoader == mycl
	}

	public static interface iface {}
	public static class clazz implements iface {}

	def "LoadImplementation"() {
		given:
			def loader = new StandardClassLoader()
		expect:
			loader.loadImplementation( new ImplementationInformation.Impl( clazzName ), iface.class ) == expected
		where:
			clazzName               |   expected
			clazz.class.getName()   |   clazz.class
			Object.class.getName()  |   null
			"non-existent"          |   null
	}
}
