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

import se.toxbee.fimpl.ImplementationLoader;
import se.toxbee.fimpl.common.ImplementationInformation;

/**
 * StandardClassLoader uses a ClassLoader to load a class.
 * This is the default implementation.
 *
 * @author Centril<twingoow@gmail.com> / Mazdak Farrokhzad.
 * @version 1.0
 * @since Jan, 17, 2014
 */
public class StandardClassLoader implements ImplementationLoader {
	private ClassLoader classLoader;

	/**
	 * Constructs the loader with the context class loader of the current thread.
	 */
	public StandardClassLoader() {
		this( null );
	}

	/**
	 * Constructs the loader with a ClassLoader.
	 *
	 * @param classLoader the ClassLoader to use.
	 */
	public StandardClassLoader( ClassLoader classLoader ) {
		this.setClassloader( classLoader );
	}

	/**
	 * Sets the ClassLoader to use.
	 *
	 * @param cl the loader, if null, the context class loader of the current thread is used.
	 */
	public void setClassloader( ClassLoader cl ) {
		this.classLoader = cl == null ? Thread.currentThread().getContextClassLoader() : cl;
	}

	/**
	 * Returns the currently used ClassLoader.
	 *
	 * @return the ClassLoader.
	 */
	public ClassLoader getClassLoader() {
		return this.classLoader;
	}

	@Override
	public <T> Class<? extends T> loadImplementation( ImplementationInformation info, Class<T> targetType ) {
		// Load the class, return null if class ain't found or of wrong type.
		try {
			return this.classLoader.loadClass( info.getImplementorClass() ).asSubclass( targetType );
		} catch ( ClassNotFoundException e ) {
		} catch ( ClassCastException e ) {
		}

		return null;
	}
}
