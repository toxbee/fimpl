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
package se.toxbee.fimpl.common;

import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * Util provides common utilities for objects of any kind.
 *
 * @author Centril<twingoow@gmail.com> / Mazdak Farrokhzad.
 * @version 1.0
 * @since Jan, 23, 2014
 */
public class Util {
	public static Charset CHARSET = Charset.forName( "UTF-8" );

	/**
	 * Checks if two possibly null valued objects are equal.
	 *
	 * @param l the left object.
	 * @param r the right object.
	 * @return true if equal.
	 */
	public static boolean equal( Object l, Object r ) {
		return l == r || (l != null && l.equals( r ));
	}

	/**
	 * Guards against null, throwing NullPointerException if val is null.
	 *
	 * @param val val to test for null.
	 * @param <T> the type of val.
	 * @return val.
	 */
	public static <T> T guardNull( T val ) {
		if ( val == null ) {
			throw new NullPointerException( "Null not allowed." );
		}

		return val;
	}

	/**
	 * Closes a {@link java.io.Closeable}.
	 *
	 * @param c the closeable to close.
	 */
	public static void close( Closeable c ) {
		if ( c == null ) {
			return;
		}

		try {
			c.close();
		} catch ( IOException e ) {
			throw new RuntimeException( e );
		}
	}
}
