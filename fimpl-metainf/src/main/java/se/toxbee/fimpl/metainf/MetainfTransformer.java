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
package se.toxbee.fimpl.metainf;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import se.toxbee.fimpl.common.ImplementationInformation;
import se.toxbee.fimpl.common.ImplementationInformation.Impl;
import se.toxbee.fimpl.impl.CollectionIndexTransformer;

import static se.toxbee.fimpl.common.Util.CHARSET;
import static se.toxbee.fimpl.common.Util.close;

/**
 * MetainfTransformer transforms InputStream:s to ImplementationInformation:s.
 *
 * @author Centril<twingoow@gmail.com> / Mazdak Farrokhzad.
 * @version 1.0
 * @since Jan, 25, 2014
 */
public class MetainfTransformer implements CollectionIndexTransformer {
	private static final int BUF_SIZE = 100;

	private static char[] LINE_SEPARATORS = new char[] { '\n', '\r' };
	private static char[] PIECE_SEPARATOR = new char[] { '\t' };

	public static void setPieceSeparators( char[] pieceSeparators ) {
		PIECE_SEPARATOR = pieceSeparators;
	}

	public static void setLineSeparators( char[] lineSeparators ) {
		LINE_SEPARATORS = lineSeparators;
	}

	@Override
	public Iterator<ImplementationInformation> readImplementationCollection( Iterator<InputStream> in ) {
		if ( in == null || !in.hasNext() ) {
			return null;
		}

		StringBuilder builder = new StringBuilder( BUF_SIZE );
		List<ImplementationInformation> list = new ArrayList<ImplementationInformation>();

		while ( in.hasNext() ) {
			// Open stream & reader.
			Reader r = new InputStreamReader( in.next(), CHARSET );
			BufferedReader reader = new BufferedReader( r );
			while ( readInfo( list, builder, reader ) );
			close( reader );
		}

		return list.iterator();
	}

	static boolean readInfo( List<ImplementationInformation> list, StringBuilder buf, Reader reader ) {
		// Read implementation class.
		int retr = readToTab( buf, reader );
		String  clazz = buf.toString();
		if ( isLineFinished( retr ) ) {
			return buf.length() <= 0 || addInfo( list, retr, clazz, 0, null, null );
		} else if ( retr == -1 ) {
			return addInfo( list, retr, clazz, 0, null, null );
		}

		// Read priority.
		retr = readToTab( buf, reader );
		int prio = buf.length() == 0 ? 0 : Integer.parseInt( buf.toString() );
		if ( isComplete( retr ) ) {
			return addInfo( list, retr, clazz, prio, null, null );
		}

		// Read type.
		retr = readToTab( buf, reader );
		String type = buf.toString();
		if ( isComplete( retr ) ) {
			return addInfo( list, retr, clazz, prio, type, null );
		}

		// Read extras.
		retr = readToTab( buf, reader );
		String extras = buf.toString();
		if ( isComplete( retr ) ) {
			return addInfo( list, retr, clazz, prio, type, extras );
		}

		// Eat anything left before newline.
		while ( true ) {
			if ( isComplete( retr = read( reader ) ) ) {
				return addInfo( list, retr, clazz, prio, type, extras );
			}
		}
	}

	static boolean addInfo( List<ImplementationInformation> list, int r, String clazz, int prio, String type, Object extras  ) {
		if ( !clazz.isEmpty() ) {
			list.add( new Impl( clazz, prio, type, extras ) );
		}
		return r != -1;
	}

	static int readToTab( StringBuilder builder, Reader reader ) {
		if ( builder.length() > 0 ) {
			builder.delete( 0, builder.length() );
		}

		while ( true ) {
			int c = read( reader );

			if ( isComplete( c ) || isPieceFinished( c ) ) {
				return c;
			}

			builder.append( (char) c );
		}
	}

	static boolean isComplete( int r ) {
		return isLineFinished( r ) || r == -1;
	}

	static boolean isPieceFinished( int r ) {
		return charsContains( PIECE_SEPARATOR, r );
	}

	static boolean isLineFinished( int r ) {
		return charsContains( LINE_SEPARATORS, r );
	}

	static boolean charsContains( char[] haystack, int needle ) {
		for ( char elem : haystack ) {
			if ( elem == needle ) {
				return true;
			}
		}

		return false;
	}

	static int read( Reader reader ) {
		try {
			int i = reader.read();
			return i;
		} catch ( IOException e ) {
			close( reader );
			throw new RuntimeException( e );
		}
	}
}
