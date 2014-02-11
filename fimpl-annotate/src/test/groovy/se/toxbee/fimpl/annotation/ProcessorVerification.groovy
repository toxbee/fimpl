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

import spock.lang.Shared
import spock.lang.Specification

import javax.tools.*
import java.nio.charset.Charset

public class ProcessorVerification extends Specification {
	static String BASE_PATH = "fimpl-annotate/"
	static String OUTPUT_PATH = System.getProperty("user.dir") + '/' + BASE_PATH + "build/tmp/test"
	static String INPUT_PATH = BASE_PATH + "src/test/java/"

	class TestCase implements CompilerTestCase {
		@Override
		Iterable<String> getClassesToCompile() {
			println clazz( ZeInterface )
			return [clazz( ZeInterface ), clazz( AnnotatedClass_1 ), clazz( AnnotatedClass_2 )];
		}

		@Override
		void test( List<Diagnostic<? extends JavaFileObject>> diagnostics, String stdoutS, Boolean result ) {
			// No mandatory warnings or compilation errors should be found.
			for ( Diagnostic<? extends JavaFileObject> diagnostic : diagnostics ) {
				def k = diagnostic.getKind()
				assert k != Diagnostic.Kind.MANDATORY_WARNING
				assert k != Diagnostic.Kind.ERROR
			}

			assert result

			def f = new File( OUTPUT_PATH + "/META-INF/services/" + ZeInterface.getName() )
			assert f.exists()
			assert f.isFile()

			BufferedReader reader = new BufferedReader( new FileReader( f ) )
			assert reader.readLine() == "se.toxbee.fimpl.annotation.AnnotatedClass_1\t1337\ttype\textras"
			assert reader.readLine() == "se.toxbee.fimpl.annotation.AnnotatedClass_2"
		}
	}

	def "TheTest"() {
		given:
			test( new TestCase() )
	}

	interface CompilerTestCase {
		Iterable<String> getClassesToCompile()
		void test( List<Diagnostic<? extends JavaFileObject>> diagnostics, String stdoutS, Boolean result );
	}

	String clazz( Class<?> clazz ) {
		def pkg = clazz.getPackage().getName().replace( '.', '/' )
		return INPUT_PATH + pkg + '/' + clazz.getSimpleName() + '.java'
	}

	def test( CompilerTestCase currentTestCase ) {
		// The files to be compiled.
		String[] files = currentTestCase.getClassesToCompile();

		// Streams.
		ByteArrayOutputStream stdoutStream = new ByteArrayOutputStream();
		OutputStreamWriter stdout = new OutputStreamWriter( stdoutStream );

		def f = new File( OUTPUT_PATH )
		f.mkdirs()
		fileManager.setLocation( StandardLocation.CLASS_OUTPUT, [ f ] );

		// Compile, etc.
		Boolean result = compiler.getTask( stdout, fileManager, collector, null, null, fileManager.getJavaFileObjects( files ) ).call();
		String stdoutS = new String( stdoutStream.toByteArray() );

		// Perform the verifications.
		currentTestCase.test( collector.getDiagnostics(), stdoutS, result );
	}

	def @Shared JavaCompiler compiler
	StandardJavaFileManager fileManager
	DiagnosticCollector<JavaFileObject> collector

	def setupSpec() {
		compiler = ToolProvider.getSystemJavaCompiler()
	}

	def setup() {
		//configure the diagnostics collector.
		collector = new DiagnosticCollector<JavaFileObject>()
		fileManager = compiler.getStandardFileManager(collector, Locale.US, Charset.forName("UTF-8"))
	}
}