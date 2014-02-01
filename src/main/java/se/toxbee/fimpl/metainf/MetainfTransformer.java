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
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import se.toxbee.fimpl.ImplementationInformation;
import se.toxbee.fimpl.Util;
import se.toxbee.fimpl.impl.CollectionIndexTransformer;
import se.toxbee.fimpl.impl.ImplementationInformationImpl;

/**
 *
 * @author Centril<twingoow@gmail.com> / Mazdak Farrokhzad.
 * @version 1.0
 * @since Jan, 25, 2014
 */
public class MetainfTransformer implements CollectionIndexTransformer {
	private static final int BUF_SIZE = 100;

	@Override
	public Iterator<ImplementationInformation> readImplementationCollection( Iterator<InputStream> in ) {
		if ( !in.hasNext() ) {
			return null;
		}

		StringBuilder builder = new StringBuilder( BUF_SIZE );
		List<ImplementationInformation> list = new ArrayList<ImplementationInformation>();

		while ( in.hasNext() ) {
			// Open stream & reader.
			BufferedReader reader = new BufferedReader( new InputStreamReader( in.next(), Util.CHARSET ) );
			while ( readInfo( list, builder, reader ) );
			close( reader );
		}

		return list.iterator();
	}

	private static boolean readInfo( List<ImplementationInformation> l, StringBuilder builder, Reader reader ) {
		Object[] d = new Object[4];
		int r;

		// Read implementation class.
		r = readToTab( builder, reader );
		d[0] = builder.toString();
		if ( r == '\n' || r == -1 ) {
			return addInfo( r, d, l );
		}

		// Read priority.
		r = readToTab( builder, reader );
		String data = builder.toString();
		if ( data.equals( "min" ) ) {
			d[1] = Integer.MIN_VALUE;
		} else if ( data.equals( "max" ) ) {
			d[1] = Integer.MIN_VALUE;
		} else {
			d[1] = Integer.parseInt( data );
		}
		if ( r == '\n' || r == -1 ) {
			return addInfo( r, d, l );
		}

		// Read type.
		r = readToTab( builder, reader );
		d[2] = builder.toString();
		if ( r == '\n' || r == -1 ) {
			return addInfo( r, d, l );
		}

		// Read extras.
		r = readToTab( builder, reader );
		d[3] = builder.toString();
		if ( r == '\n' || r == -1 ) {
			return addInfo( r, d, l );
		}

		// Eat anything left before newline.
		while ( true ) {
			switch ( read( reader ) ) {
				case -1:
					return true;

				case '\n':
					return false;
			}
		}
	}

	private static boolean addInfo( int r, Object[] data, List<ImplementationInformation> list ) {
		list.add( new ImplementationInformationImpl( (String) data[0], (Integer) data[1], (String) data[2], data[3] ) );
		return r != -1;
	}

	private static int readToTab( StringBuilder builder, Reader reader ) {
		if ( builder.length() > 0 ) {
			builder.delete( 0, builder.length() );
		}

		while ( true ) {
			int c = read( reader );

			if ( c == -1 || c == '\n' || c == '\t' ) {
				return c;
			}

			builder.append( (char) c );
		}
	}

	private static int read( Reader reader ) {
		try {
			return reader.read();
		} catch ( IOException e ) {
			close( reader );
			throw new RuntimeException( e );
		}
	}

	private static void close( Closeable c ) {
		try {
			c.close();
		} catch ( IOException e ) {
			throw new RuntimeException( e );
		}
	}
}
