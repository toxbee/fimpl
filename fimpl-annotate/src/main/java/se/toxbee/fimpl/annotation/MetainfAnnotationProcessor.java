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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic.Kind;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

import se.toxbee.fimpl.common.ImplementationInformation;
import se.toxbee.fimpl.common.ImplementationInformation.Impl;

/**
 *
 */
@SuppressWarnings( { "Since15" } )
@SupportedAnnotationTypes( "se.toxbee.fimpl.annotation.ProvidedImplementation" )
@SupportedSourceVersion( SourceVersion.RELEASE_7 )
public class MetainfAnnotationProcessor extends AbstractProcessor {
	private static final String LOCATION = "META-INF/services/";

	private static final String CHARSET = "UTF-8";

	@Override
	public boolean process( Set<? extends TypeElement> annotations, RoundEnvironment roundEnv ) {
		if ( roundEnv.processingOver() ) {
			return false;
		}

		Map<String, Set<ImplementationInformation>> services = new HashMap<String, Set<ImplementationInformation>>();

		Elements elements = processingEnv.getElementUtils();

		// Discover services from the current compilation sources
		for ( Element e : roundEnv.getElementsAnnotatedWith( ProvidedImplementation.class ) ) {
			ProvidedImplementation a = e.getAnnotation( ProvidedImplementation.class );

			if ( this.isMalformed( e, a ) ) {
				continue;
			}

			TypeElement type = (TypeElement) e;
			TypeElement contract = getContract( type, a );
			if ( contract == null ) {
				continue; // error should have already been reported
			}

			String cn = elements.getBinaryName( contract ).toString();
			Set<ImplementationInformation> v = services.get( cn );
			if ( v == null ) {
				services.put( cn, v = new HashSet<ImplementationInformation>() );
			}
			String clazzName = elements.getBinaryName( type ).toString();

			ImplementationInformation info = new Impl( clazzName, a.priority(), a.type() );
			v.add( info );
		}

		// Also load up any existing values, since this compilation may be partial
		Filer filer = processingEnv.getFiler();
		Pattern tabSplitter = Pattern.compile( "\t", Pattern.LITERAL );
		for ( Map.Entry<String, Set<ImplementationInformation>> e : services.entrySet() ) {
			Set<ImplementationInformation> set = e.getValue();

			try {
				String contract = e.getKey();
				FileObject f = filer.getResource( StandardLocation.CLASS_OUTPUT, "", LOCATION + contract );
				BufferedReader r = new BufferedReader( new InputStreamReader( f.openInputStream(), CHARSET ) );

				String line;
				while ( (line = r.readLine()) != null ) {
					String[] data = tabSplitter.split( line, 4 );
					set.add( new Impl( data[0], Integer.parseInt( data[1] ), data[2] ) );
				}

				r.close();
			} catch ( FileNotFoundException x ) {
				// doesn't exist
			} catch ( IOException x ) {
				error( "Failed to load existing service definition files: " + x );
			}
		}

		// Now write them back out
		for ( Map.Entry<String, Set<ImplementationInformation>> e : services.entrySet() ) {
			try {
				String contract = e.getKey();
				processingEnv.getMessager().printMessage( Kind.NOTE, "Writing " + LOCATION + contract );
				FileObject f = filer.createResource( StandardLocation.CLASS_OUTPUT, "", LOCATION + contract );
				PrintWriter pw = new PrintWriter( new OutputStreamWriter( f.openOutputStream(), CHARSET ) );

				for ( ImplementationInformation info : e.getValue() ) {
					pw.println( info.getImplementorClass() + '\t' + info.getPriority() + '\t' + info.getType() );
				}

				pw.close();
			} catch ( IOException x ) {
				error( "Failed to write service definition files: " + x );
			}
		}

		return false;
	}

	private boolean isMalformed( Element e, ProvidedImplementation a ) {
		return a == null || (!e.getKind().isClass() && !e.getKind().isInterface());
	}

	private TypeElement getContract( TypeElement type, ProvidedImplementation a ) {
		// Explicitly specified?
		try {
			a.value();
			throw new AssertionError();
		} catch ( MirroredTypeException e ) {
			TypeMirror m = e.getTypeMirror();

			if ( m.getKind() == TypeKind.VOID ) {
				// Contract inferred from the signature
				List<? extends TypeMirror> interfases = type.getInterfaces();
				TypeMirror superClass = type.getSuperclass();

				boolean hasBaseClass = superClass.getKind() != TypeKind.NONE && !isObject( superClass );
				boolean hasInterfaces = !interfases.isEmpty();

				return hasBaseClass ^ hasInterfaces ? type( hasBaseClass ? superClass : interfases.get( 0 ) ) : error( type, "Contract type was not specified, but it couldn't be inferred." );
			}

			return m instanceof DeclaredType ? type( m ) : error( type, "Invalid type specified as the contract" );
		}
	}

	private boolean isObject( TypeMirror t ) {
		return t instanceof DeclaredType && type( t ).getQualifiedName().toString().equals( "java.lang.Object" );
	}

	private TypeElement type( TypeMirror m ) {
		return (TypeElement) ((DeclaredType) m).asElement();
	}

	private TypeElement error( Element source, String msg ) {
		processingEnv.getMessager().printMessage( Kind.ERROR, msg, source );
		return null;
	}

	private void error( String msg ) {
		processingEnv.getMessager().printMessage( Kind.ERROR, msg );
	}
}
