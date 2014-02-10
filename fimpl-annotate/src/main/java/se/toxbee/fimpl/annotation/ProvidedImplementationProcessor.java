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
@SupportedOptions({"meta.location", "meta.inf.only"})
@SupportedSourceVersion( SourceVersion.RELEASE_7 )
public class ProvidedImplementationProcessor extends AbstractProcessor {
	/* ----------------------------------------------
	 * Public API: Configurable.
	 * ----------------------------------------------
	 */

	public static String META_LOCATION = "META-INF/services/";
	public static boolean METAINF_ONLY = false;

	/* ----------------------------------------------
	 * Private Config.
	 * ----------------------------------------------
	 */

	private static final Class<ProvidedImplementation> ANNOTATION_CLAZZ = ProvidedImplementation.class;
	private static final String ANNOTATION_TYPE = ANNOTATION_CLAZZ.getName();
	private static final Charset CHARSET = Charset.forName( "UTF-8" );
	private String metaLocation;
	private boolean metaInfOnly;

	@Override
	public Set<String> getSupportedAnnotationTypes() {
		return Collections.singleton( ANNOTATION_TYPE );
	}

	/* ----------------------------------------------
	 * Processor Implementation.
	 * ----------------------------------------------
	 */

	private Pattern tabSplitter;
	private Types util;
	private Elements elements;

	@Override
	public synchronized void init( ProcessingEnvironment processingEnv ) {
		super.init( processingEnv );

		this.readOptions();

		this.tabSplitter = Pattern.compile( "\t", Pattern.LITERAL );
		this.util = this.processingEnv.getTypeUtils();
		this.elements = this.processingEnv.getElementUtils();
	}

	private void readOptions() {
		Map<String, String> opts = this.processingEnv.getOptions();

		this.metaLocation = opts.get( "meta.location" );
		if ( this.metaLocation == null ) {
			this.metaLocation = META_LOCATION;
		}

		String metaOnly = opts.get( "meta.inf.only" );
		this.metaInfOnly = metaOnly == null ? METAINF_ONLY : Boolean.parseBoolean( metaOnly );
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
					set.add( this.metaInfOnly ? new Impl( line ) : new Impl( tabSplitter.split( line, 4 ) ) );
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
				msg().printMessage( Kind.NOTE, "Writing " + interfaseFile );
				FileObject f = filer.createResource( StandardLocation.CLASS_OUTPUT, "", interfaseFile );
				writer = new PrintWriter( new OutputStreamWriter( f.openOutputStream(), CHARSET ) );

				if ( this.metaInfOnly ) {
					for ( ImplementationInformation info : e.getValue() ) {
						writer.println( info.getImplementorClass() );
					}
				} else {
					for ( ImplementationInformation info : e.getValue() ) {
						writer.print( info.getImplementorClass() );
						int len = info.getType() != null ? 2 : (info.getPriority() != 0 ? 1 : 0);
						for ( int i = 1; i <= len; ++i ) {
							writer.print( '\t' );

							switch ( i ) {
								case 1:
									writer.print( info.getPriority() );
									break;

								case 2:
									writer.print( info.getType() );
									break;
							}
						}
						writer.println();
					}
				}
			} catch ( IOException x ) {
				error( "Failed to write implementation definition files: " + x );
			} finally {
				close( writer );
			}
		}
	}

	private void discoverImplementations( Map<String, Set<ImplementationInformation>> store, RoundEnvironment roundEnv ) {
		for ( Element e : roundEnv.getElementsAnnotatedWith( ProvidedImplementation.class ) ) {
			ProvidedImplementation pi = e.getAnnotation( ProvidedImplementation.class );
			TypeElement type = (TypeElement) e,
						implemented = getContract( type, pi );

			if ( implemented != null ) {
				getSet( store, typeName( implemented ) ).add( new Impl( typeName( type ), pi.priority(), pi.type() ) );
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

		if ( isBaseClass( superClass ) ^ hasInterface ) {
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
		return baseClass.getKind() != TypeKind.NONE;
	}

	private static boolean isObject( TypeElement type ) {
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

	private static void close( Closeable c ) {
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

	private Messager msg() {
		return processingEnv.getMessager();
	}
}
