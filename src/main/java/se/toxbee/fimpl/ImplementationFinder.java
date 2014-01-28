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
package se.toxbee.fimpl;

import se.toxbee.fimpl.ImplementationResultSet.Impl;
import se.toxbee.fimpl.util.ImmutableIterator;

import static se.toxbee.fimpl.util.Util.guardNull;

/**
 * <p>ImplementationFinder is the entry/front class API for fImpl.</p>
 *
 * @author Centril<twingoow@gmail.com> / Mazdak Farrokhzad.
 * @version 1.0
 * @since Jan, 17, 2014
 */
public class ImplementationFinder {
	protected final ImplementationFactory provider;

	/**
	 * Constructs the finder.
	 *
	 * @param provider the provider implementation.
	 */
	public ImplementationFinder( ImplementationFactory provider ) {
		this.provider = guardNull( provider );
	}

	/**
	 * Finds the implementations for the given "interface".
	 *
	 * @param interfase the interface class object.
	 * @param <I> the interface type.
	 * @return the result set.
	 */
	public <I> Impl<I> find( Class<I> interfase ) {
		return new Impl<I>( this.provider, interfase, this.findRaw( interfase ) );
	}

	/**
	 * Finds the implementations for the given "interface".<br/>
	 * Returns a "raw" ImplementationCollection object.
	 *
	 * @param interfase the interface class object.
	 * @param <I> the interface type.
	 * @return the "collection".
	 */
	public <I> ImmutableIterator<ImplementationInformation> findRaw( Class<I> interfase ) {
		return this.findImplementationCollection( interfase );
	}

	protected <I> ImmutableIterator<ImplementationInformation> findImplementationCollection( Class<I> interfase ) {
		return this.provider.reader().readImplementationCollection( interfase );
	}
}
