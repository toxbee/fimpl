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
import se.toxbee.fimpl.common.ImplementationInformation
import spock.lang.Specification

import javax.annotation.processing.Filer
import javax.annotation.processing.Messager
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.util.Elements
import javax.lang.model.util.Types

class ProvidedImplementationProcessorTest extends Specification {
	def "void-definition-test"() {
		expect:
			void.class == Void.TYPE
			Void.TYPE != Void.class
	}

	def "Ctor"() {
		given:
			def processor = new ProvidedImplementationProcessor()
			def pattern = processor.tabSplitter
		expect:
			pattern.split( "a\tb\tc" ).collect() == ["a", "b", "c"]
			pattern.split( "a\tb\tc\td\te", 4 ).collect() == ["a", "b", "c", "d\te"]
	}

	def "GetSupportedAnnotationTypes"() {
		when:
			def processor = new ProvidedImplementationProcessor()
			def types = processor.getSupportedAnnotationTypes()
		then:
			types.size() == 1 && types.contains( ProvidedImplementation.class.getName() )
	}

	class PE implements ProcessingEnvironment {
		Map<String, String> opts

		PE( Map<String, String> o ) {
			opts = o
		}

		@Override
		Map<String, String> getOptions() {
			return opts
		}

		@Override
		Messager getMessager() {
			return null
		}

		@Override
		Filer getFiler() {
			return null
		}

		@Override
		Elements getElementUtils() {
			return null
		}

		@Override
		Types getTypeUtils() {
			return null
		}

		@Override
		SourceVersion getSourceVersion() {
			return null
		}

		@Override
		Locale getLocale() {
			return null
		}
	}

	def "Init"() {
		given: "A ProvidedImplementationProcessor"
			def p = new ProvidedImplementationProcessor()
			def e = new PE( opt )
		when: "Testing init() for processor"
			p.init( e )
		then: "Check that the given or default option values are used."
			p.metaLocation == loc
			p.metaInfOnly == only
			p.isInitialized()
		where:
			opt     <<  [Collections.emptyMap(), new HashMap<String, String>() {{
				put(ProvidedImplementationProcessor.OPTION_META_LOCATION, "l")
				put(ProvidedImplementationProcessor.OPTION_METAINF_ONLY, "true")
			}}]
			loc     <<  [ProvidedImplementationProcessor.OPTION_DEFAULT_META_LOCATION, "l"]
			only    <<  [ProvidedImplementationProcessor.OPTION_DEFAULT_METAINF_ONLY, true]
	}

	def "formatImplementationMetadata"() {
		given:
			def p = new ProvidedImplementationProcessor()
		expect:
			p.formatImplementationMetadata( info ) == formatted
		where:
			formatted    | info
			"c\t0\tt\te" | new ImplementationInformation.Impl( "c", 0, "t", "e" )
			"c\t0\tt"    | new ImplementationInformation.Impl( "c", 0, "t" )
			"c\t1"       | new ImplementationInformation.Impl( "c", 1 )
			"c"          | new ImplementationInformation.Impl( "c" )
	}

	def "close"() {
		given:
			def c1 = Mock(Closeable)
			def c2 = Mock(Closeable)
			c2.close() >> { throw new IOException() }
			1 * _.close()
		when:
			ProvidedImplementationProcessor.close( null )
		then:
			notThrown( RuntimeException )
		when:
			ProvidedImplementationProcessor.close( c1 )
		then:
			notThrown( RuntimeException )
		when:
			ProvidedImplementationProcessor.close( c2 )
		then:
			RuntimeException e = thrown()
			e.cause instanceof IOException
	}
}
