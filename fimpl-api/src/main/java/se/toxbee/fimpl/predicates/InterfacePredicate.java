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
package se.toxbee.fimpl.predicates;

import se.toxbee.fimpl.ImplementationResultSet;
import se.toxbee.fimpl.Util;
import se.toxbee.fimpl.common.ImplementationInformation;
import se.toxbee.fimpl.ImplementationLoader;

/**
 * <p>InterfacePredicate tests whether or not a class is assignable
 * from a given "interface".</p>
 *
 * <p><strong>NOTE:</strong> this has the drawback that the class must be loaded.</p>
 *
 * @author Centril<twingoow@gmail.com> / Mazdak Farrokhzad.
 * @version 1.0
 * @since Jan, 24, 2014
 */
public class InterfacePredicate<I> implements Predicate<I> {
	protected final Class<?> interfase;

	/**
	 * Constructs the predicate with the "interface" class object we're checking for assignability.
	 *
	 * @param interfase the interface class object, not null.
	 */
	public InterfacePredicate( Class<?> interfase ) {
		this.interfase = Util.guardNull( interfase );
	}

	/**
	 * Returns the "interface".
	 *
	 * @return the interface class object.
	 */
	public Class<?> interfase() {
		return this.interfase;
	}

	@Override
	public boolean match( ImplementationInformation info, ImplementationResultSet<I, ?> set, boolean anyMode ) {
		// Get the class we are testing.
		ImplementationLoader loader = set.provider().loader();
		Class<? extends I> clazz = loader.loadImplementation( info, set.interfase() );
		return this.interfase.isAssignableFrom( clazz );
	}
}
