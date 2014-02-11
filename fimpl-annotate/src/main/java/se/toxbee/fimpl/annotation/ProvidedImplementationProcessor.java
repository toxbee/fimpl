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

/*
* The MIT License
*
* Copyright (c) 2009-, Kohsuke Kawaguchi
*
* Permission is hereby granted, free of charge, to any person obtaining a copy
* of this software and associated documentation files (the "Software"), to deal
* in the Software without restriction, including without limitation the rights
* to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
* copies of the Software, and to permit persons to whom the Software is
* furnished to do so, subject to the following conditions:
*
* The above copyright notice and this permission notice shall be included in
* all copies or substantial portions of the Software.
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
* IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
* FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
* AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
* LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
* OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
* THE SOFTWARE.
*/

package se.toxbee.fimpl.annotation;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic.Kind;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

import se.toxbee.fimpl.common.ImplementationInformation;
import se.toxbee.fimpl.common.ImplementationInformation.Impl;

/**
 * ProvidedImplementationProcessor processes concrete types annotated<br/>
 * with {@link se.toxbee.fimpl.annotation.ProvidedImplementation}
 *
 * @author Centril<twingoow@gmail.com> / Mazdak Farrokhzad.
 * @version 1.0.1
 * @since Feb, 05, 2014
 */
@SupportedOptions({ ProvidedImplementationProcessor.OPTION_META_LOCATION,
					ProvidedImplementationProcessor.OPTION_METAINF_ONLY })
@SupportedSourceVersion( SourceVersion.RELEASE_7 )
public class ProvidedImplementationProcessor extends AbstractProcessor {
	/* ----------------------------------------------
	 * Public API: Configurable.
	 * ----------------------------------------------
	 */

	public static final String OPTION_META_LOCATION = "meta.location";
	public static final String OPTION_METAINF_ONLY = "meta.inf.only";

	/* ----------------------------------------------
	 * Private Config.
	 * ----------------------------------------------
	 */

	public static String OPTION_DEFAULT_META_LOCATION = "META-INF/services/";
	public static boolean OPTION_DEFAULT_METAINF_ONLY = false;

	private static final Class<ProvidedImplementation> ANNOTATION_CLAZZ = ProvidedImplementation.class;
	private final String ANNOTATION_TYPE = ANNOTATION_CLAZZ.getName();

	/* ----------------------------------------------
	 * Tools of Implementation.
	 * ----------------------------------------------
	 */

	String metaLocation;
	boolean metaInfOnly;

	final Pattern tabSplitter;
	private Types util;
	private Elements elements;
	private static final Charset CHARSET = Charset.forName( "UTF-8" );

	@Override
	public Set<String> getSupportedAnnotationTypes() {
		return Collections.singleton( ANNOTATION_TYPE );
	}

	/* ----------------------------------------------
	 * Processor Implementation.
	 * ----------------------------------------------
	 */

	/**
	 * Constructs the processor.
	 */
	public ProvidedImplementationProcessor() {
		super();

		this.tabSplitter = Pattern.compile( "\t", Pattern.LITERAL );
	}

	@Override
	public synchronized void init( ProcessingEnvironment processingEnv ) {
		super.init( processingEnv );

		this.util = this.processingEnv.getTypeUtils();
		this.elements = this.processingEnv.getElementUtils();

		this.readOptions();
	}

	private void readOptions() {
		Map<String, String> opts = this.processingEnv.getOptions();

		String metaLocation = opts.get( OPTION_META_LOCATION );
		this.metaLocation = metaLocation == null ? OPTION_DEFAULT_META_LOCATION : metaLocation;

		String metaOnly = opts.get( OPTION_METAINF_ONLY );
		this.metaInfOnly = metaOnly == null ? OPTION_DEFAULT_METAINF_ONLY : Boolean.parseBoolean( metaOnly );
	}

	@Override
	public boolean process( Set<? extends TypeElement> annotations, RoundEnvironment roundEnv ) {
		if ( roundEnv.processingOver() ) {
			return false;
		}

		Map<String, Set<ImplementationInformation>> store = new HashMap<String, Set<ImplementationInformation>>();

		// Discover services from the current compilation sources
		this.discoverImplementations( store, roundEnv );

		// Also load up any existing values, since this compilation may be partial
		Filer filer = this.processingEnv.getFiler();
		this.readExistingData( store, filer );

		// Now write them back out
		this.writeMetaData( store, filer );

		return true;
	}

	private String interfaseFile( String interfase ) {
		return this.metaLocation + interfase;
	}

	private void readExistingData( Map<String, Set<ImplementationInformation>> store, Filer filer ) {
		for ( Map.Entry<String, Set<ImplementationInformation>> e : store.entrySet() ) {
			Set<ImplementationInformation> set = e.getValue();

			BufferedReader reader = null;

			try {
				FileObject f = filer.getResource( StandardLocation.CLASS_OUTPUT, "", this.interfaseFile( e.getKey() ) );
				reader = new BufferedReader( new InputStreamReader( f.openInputStream(), CHARSET ) );

				String line;
				while ( (line = reader.readLine()) != null ) {
					set.add( this.metaInfOnly ? new Impl( line ) : Impl.from( tabSplitter.split( line, 4 ) ) );
				}
			} catch ( FileNotFoundException x ) {
				// doesn't exist
			} catch ( IOException x ) {
				error( "Failed to load existing service definition files: " + x );
			} finally {
				close( reader );
			}
		}
	}

	private void writeMetaData( Map<String, Set<ImplementationInformation>> store, Filer filer ) {
		for ( Map.Entry<String, Set<ImplementationInformation>> e : store.entrySet() ) {
			PrintWriter writer = null;

			try {
				String interfaseFile = this.interfaseFile( e.getKey() );

				note( "Writing " + interfaseFile );

				// Open writer for interfaseFile.
				FileObject f = filer.createResource( StandardLocation.CLASS_OUTPUT, "", interfaseFile );
				writer = new PrintWriter( new OutputStreamWriter( f.openOutputStream(), CHARSET ) );

				if ( this.metaInfOnly ) {
					// Writing using the SPI format.
					for ( ImplementationInformation info : e.getValue() ) {
						writer.println( info.getImplementorClass() );
					}
				} else {
					for ( ImplementationInformation info : e.getValue() ) {
						// Writing using our own meta-data format.
						String data = this.formatImplementationMetadata( info );
						note( "Writing implementation meta-data: " + data );
						writer.println( data );
					}
				}
			} catch ( IOException x ) {
				error( "Failed to write implementation definition files: " + x );
			} finally {
				close( writer );
			}
		}
	}

	String formatImplementationMetadata( ImplementationInformation info ) {
		// Format using our own meta-data format.
		StringBuilder buf = new StringBuilder();
		buf.append( info.getImplementorClass() );

		int len = info.getExtras() == null ? (info.getType() == null ? (info.getPriority() == 0 ? 0 : 1) : 2) : 3;

		for ( int i = 1; i <= len; ++i ) {
			buf.append( '\t' );

			switch ( i ) {
				case 1:
					buf.append( info.getPriority() );
					break;

				case 2:
					buf.append( info.getType() );
					break;

				case 3:
					buf.append( info.getExtras() );
					break;
			}
		}

		return buf.toString();
	}

	private void discoverImplementations( Map<String, Set<ImplementationInformation>> store, RoundEnvironment roundEnv ) {
		for ( Element e : roundEnv.getElementsAnnotatedWith( ProvidedImplementation.class ) ) {
			ProvidedImplementation pi = e.getAnnotation( ProvidedImplementation.class );
			TypeElement type = (TypeElement) e,
						implemented = getContract( type, pi );

			if ( implemented != null ) {
				ImplementationInformation info = new Impl( typeName( type ), pi.priority(), pi.type(), pi.extras() );
				Set<ImplementationInformation> set = getSet( store, typeName( implemented ) );
				set.add( info );
			}
		}
	}

	private Set<ImplementationInformation> getSet( Map<String, Set<ImplementationInformation>> store, String interfase ) {
		Set<ImplementationInformation> v = store.get( interfase );
		if ( v == null ) {
			store.put( interfase, v = new HashSet<ImplementationInformation>() );
		}
		return v;
	}

	/* ----------------------------------------------
	 * Contract processing.
	 * ----------------------------------------------
	 */

	private TypeElement getContract( TypeElement type, ProvidedImplementation pi ) {
		if ( isConcrete( type ) ) {
			try {
				pi.of();
				throw new AssertionError( "ShouldNeverHappenException" );
			} catch ( MirroredTypeException e ) {
				return readContract( type, e.getTypeMirror() );
			}
		}

		error( type, "Type annotated with: " + ANNOTATION_TYPE + ", isn't but must be concrete." );
		return null;
	}

	private TypeElement readContract( TypeElement type, TypeMirror typeMirror ) {
		if ( typeMirror.getKind() == TypeKind.VOID ) {
			return interpretImplicit( type );
		}

		if ( !(typeMirror instanceof DeclaredType) ) {
			explicitError( type, " has wrong type" );
			return null;
		}

		TypeElement implemented = type( typeMirror );
		return isExplicitValid( type, implemented ) ? implemented : null;
	}

	private TypeElement interpretImplicit( TypeElement type ) {
		List<? extends TypeMirror> interfases = type.getInterfaces();
		TypeMirror superClass = type.getSuperclass();

		boolean hasBaseClass = isBaseClass( superClass );
		boolean hasInterface = interfases.size() == 1;

		if ( hasBaseClass ^ hasInterface ) {
			return type( hasBaseClass ? superClass : interfases.get( 0 ) );
		}

		error( type, "Implicit implementedFor definition could not be inferred. Define it explicitly?" );
		return null;
	}

	private boolean isExplicitValid( TypeElement type, TypeElement implemented ) {
		ElementKind kind = implemented.getKind();
		switch ( implemented.getKind() ) {
			default:
				return explicitError( type, implemented, "a " + kind );

			case CLASS:
				if ( isFinal( implemented ) ) {
					return explicitError( type, implemented, "final" );
				}

				if ( isObject( implemented ) ) {
					return explicitError( type, implemented, "not a really a base class" );
				}

				return isSubtype( type, implemented ) ||
				       explicitError( type, implemented, "not a base class of the annotated type" );

			case INTERFACE:
				return isAssignable( type, implemented ) ||
				       explicitError( type, implemented, "is not implemented by the annotated type" );
		}
	}

	/* ----------------------------------------------
	 * Utils: Type logic.
	 * ----------------------------------------------
	 */

	private boolean isSubtype( TypeElement sub, TypeElement base ) {
		return this.util.isSubtype( sub.asType(), base.asType() );
	}

	private boolean isAssignable( TypeElement from, TypeElement to ) {
		return this.util.isAssignable( from.asType(), to.asType() );
	}

	private static boolean isConcrete( Element type ) {
		return type.getKind().isClass() && !hasModifier( type, Modifier.ABSTRACT );
	}

	private static boolean isFinal( Element type ) {
		return hasModifier( type, Modifier.FINAL );
	}

	private static boolean hasModifier( Element type, Modifier modifier ) {
		return type.getModifiers().contains( modifier );
	}

	private static boolean isBaseClass( TypeMirror baseClass ) {
		return baseClass.getKind() != TypeKind.NONE && !isObject( type( baseClass ) );
	}

	static boolean isObject( TypeElement type ) {
		return type.getQualifiedName().toString().equals( "java.lang.Object" );
	}

	private static TypeElement type( TypeMirror m ) {
		return (TypeElement) ((DeclaredType) m).asElement();
	}

	/* ----------------------------------------------
	 * Utils: etc.
	 * ----------------------------------------------
	 */

	private String typeName( TypeElement type ) {
		return this.elements.getBinaryName( type ).toString();
	}

	static void close( Closeable c ) {
		if ( c == null ) {
			return;
		}

		try {
			c.close();
		} catch ( IOException e ) {
			throw new RuntimeException( e );
		}
	}

	/* ----------------------------------------------
	 * Utils: Error reporting.
	 * ----------------------------------------------
	 */

	private boolean explicitError( Element source, TypeElement implemented, String msg ) {
		explicitError( source, implemented.getQualifiedName() + " is  " + msg );
		return false;
	}

	private void explicitError( Element source, String msg ) {
		error( source, "Explicit implementedFor definition is invalid: " + msg );
	}

	private void error( Element source, String msg ) {
		msg().printMessage( Kind.ERROR, msg, source );
	}

	private void error( String msg ) {
		msg().printMessage( Kind.ERROR, msg );
	}

	private void note( String msg ) {
		msg().printMessage( Kind.NOTE, msg );
	}

	private Messager msg() {
		return processingEnv.getMessager();
	}
}
