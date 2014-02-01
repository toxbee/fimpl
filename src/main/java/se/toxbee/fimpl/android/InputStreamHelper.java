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
package se.toxbee.fimpl.android;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.CharBuffer;

import se.toxbee.fimpl.Util;

/**
 * InputStreamHelper helps with reading an InputStream to a String.
 * Most of the code is from guava.
 *
 * @author Centril<twingoow@gmail.com> / Mazdak Farrokhzad.
 * @version 1.0
 * @since Jan, 17, 2014
 */
public class InputStreamHelper {
	private static final int BUF_SIZE = 0x800; // 2K chars (4K bytes)

	public static String toString( InputStream in ) throws IOException {
		InputStreamReader reader = new InputStreamReader( in, Util.CHARSET );
		try {
			return toString( reader );
		} finally {
			in.close();
		}
	}

	/**
	 * Reads all characters from a {@link Readable} object into a {@link String}.
	 * Does not close the {@code Readable}.
	 *
	 * @param r the object to read from
	 * @return a string containing all the characters
	 * @throws IOException if an I/O error occurs
	 */
	public static String toString( Readable r ) throws IOException {
		return toStringBuilder( r ).toString();
	}

	private static StringBuilder toStringBuilder( Readable r ) throws IOException {
		StringBuilder sb = new StringBuilder();
		copy( r, sb );
		return sb;
	}

	private static long copy( Readable from, Appendable to ) throws IOException {
		CharBuffer buf = CharBuffer.allocate( BUF_SIZE );
		long total = 0;
		while ( from.read( buf ) != -1 ) {
			buf.flip();
			to.append( buf );
			total += buf.remaining();
			buf.clear();
		}
		return total;
	}
}
