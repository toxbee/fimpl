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

package se.toxbee.fimpl.metainf

import se.toxbee.fimpl.Util
import se.toxbee.fimpl.common.ImplementationInformation
import spock.lang.Shared
import spock.lang.Specification
/**
 *
 * @author Centril < twingoow @ gmail.com >  / Mazdak Farrokhzad.
 * @version 1.0
 * @since Feb , 09, 2014
 */
class MetainfTransformerTest extends Specification {
	def @Shared i_1 = info( "clazz", 0, "type", "extra" )
	def @Shared i_2 = info( "clazz", 0, null, "extra" )
	def @Shared i_3 = info( "clazz", 1337, null, null )
	def @Shared i_4 = info( "clazz", -1337, null, null )
	def @Shared i_5 = info( "clazz", 0, null, null )
	def @Shared testText = """\
clazz\t0\ttype\textra\tignored-stuff\t\t\t
clazz\t0\ttype\textra
clazz\t\ttype\textra
clazz\t0\t\textra
clazz\t\t\textra
clazz\t1337
clazz\t-1337
clazz"""

	def "ReadImplementationCollection"() {
		given:
			def transformer = new MetainfTransformer()
			def istream = new ByteArrayInputStream( testText.getBytes( Util.CHARSET ) )
			def expected = []
			3.times { expected << i_1 }
			2.times { expected << i_2 }
			1.times { expected << i_3 }
			1.times { expected << i_4 }
			1.times { expected << i_5 }
		when:
			def r1 = transformer.readImplementationCollection( [].iterator() )
		then:
			r1 == null
		when:
			def r2 = transformer.readImplementationCollection( [].iterator() )
		then:
			r2 == null
		when:
			def r3 = transformer.readImplementationCollection( [istream].iterator() )
		then:
			r3.collect().equals( expected )
	}

	def retr;
	def StringBuilder buf = new StringBuilder()
	def Reader reader
	def Reader backReader

	def init( String data ) {
		backReader = new CharArrayReader( data.toCharArray() )
		reader = Mock( Reader );
		reader.read() >> { backReader.read() }
	}

	def "ReadInfo"() {
		given:
			init( testText )
		expect:
			3.times { testReadInfo( i_1, true ) }
			2.times { testReadInfo( i_2, true ) }
			testReadInfo( i_3, true )
			testReadInfo( i_4, true )
			testReadInfo( i_5, false )
	}

	def info( String clazz, int prio, String type, Object extras ) {
		new ImplementationInformation.Impl( clazz, prio, type, extras )
	}

	def testReadInfo( ImplementationInformation b, boolean retrVal ) {
		List<ImplementationInformation> l = new ArrayList<ImplementationInformation>(1)
		retr = MetainfTransformer.readInfo( l, buf, reader )

		assert !l.isEmpty()

		def a = l[0]

		assert a.implementorClass == b.implementorClass
		assert a.priority == b.priority
		assert a.type == b.type
		assert a.extras == b.extras

		return retr == retrVal
	}

	def "ReadToTab"() {
		given:
			buf.append( "trash data" );
			init( "l1 t1\tl1 t2\n\n\t\nl4 t1" )
		expect:
			readTab() == '\t' && buf.toString() == "l1 t1"
			readTab() == '\n' && buf.toString() == "l1 t2"
			readTab() == '\n' && buf.length() == 0
			readTab() == '\t' && buf.length() == 0
			readTab() == '\n' && buf.length() == 0
			readTab() == -1 && buf.toString() == "l4 t1"
	}

	def readTab() {
		retr = MetainfTransformer.readToTab( buf, reader )
	}

	def "Read"() {
		given:
			reader = Mock( Reader );
			1 * reader.read() >> { throw new IOException() }
			1 * reader.close()
		when:
			MetainfTransformer.read( reader )
		then:
			def e = thrown( RuntimeException )
			e.cause instanceof IOException
	}
}
