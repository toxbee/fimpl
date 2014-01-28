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
package se.toxbee.fimpl.util;

import java.util.Iterator;

/**
 * <p>ClosableIterator is an iterator that you can {@link #close()}.</p>
 *
 * <p>This happens automatically when {@link #hasNext()} yields false.
 * If a loop is broken early {@link #close()} must be called manually.</p>
 *
 * @author Centril<twingoow@gmail.com> / Mazdak Farrokhzad.
 * @version 1.0
 * @since Jan, 28, 2014
 */
public interface ClosableIterator<T> extends Iterator<T> {
	/**
	 * Closes the iterator.
	 */
	public void close();
}
