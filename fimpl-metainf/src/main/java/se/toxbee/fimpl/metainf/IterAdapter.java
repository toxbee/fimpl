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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Iterator;

/**
* IterAdapter wraps {@link java.util.Enumeration} of {@link java.net.URL}s<br/>
* to {@link java.util.Iterator} of {@link java.io.InputStream}s.
*
* @author Centril<twingoow@gmail.com> / Mazdak Farrokhzad.
* @version 1.0
* @since Feb, 11, 2014
*/
class IterAdapter implements Iterator<InputStream> {
	private final Enumeration<URL> urls;

	/**
	 * Constructs the wrapper.
	 *
	 * @param urls the urls to wrap.
	 */
	IterAdapter( Enumeration<URL> urls ) {
		this.urls = urls;
	}

	@Override
	public boolean hasNext() {
		return this.urls.hasMoreElements();
	}

	@Override
	public InputStream next() {
		try {
			return this.urls.nextElement().openStream();
		} catch ( IOException e ) {
			throw new RuntimeException( e );
		}
	}

	@Override
	public void remove() {
	}
}
