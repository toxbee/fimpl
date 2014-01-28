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
package se.toxbee.fimpl.impl;

import java.io.InputStream;

import se.toxbee.fimpl.util.ClosableIterator;

/**
 * <p>InterfaceLookupProvider provides InputStreams that contain<br/>
 * implementation lookup files/indexes for a ImplementationReader.</p>
 *
 * @author Centril<twingoow@gmail.com> / Mazdak Farrokhzad.
 * @version 1.0
 * @since Jan, 23, 2014
 */
public interface InterfaceLookupProvider {
	/**
	 * Returns an InputStream for the given
	 *
	 * @param interfase the interface class to find iterator of InputStream(s) with implementations listing in it.
	 * @param <I> the type of the interface.
	 * @return the iterator that contains implementations listings, or null if there's none.
	 */
	public <I> ClosableIterator<InputStream> interfaceLookupStream( Class<I> interfase );
}
