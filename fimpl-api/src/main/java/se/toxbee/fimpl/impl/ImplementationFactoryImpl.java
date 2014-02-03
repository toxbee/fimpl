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

import se.toxbee.fimpl.ImplementationReader;
import se.toxbee.fimpl.ImplementationFactory;
import se.toxbee.fimpl.ImplementationLoader;

import static se.toxbee.fimpl.Util.guardNull;

/**
 * ImplementationFactoryImpl a field backed {@link se.toxbee.fimpl.ImplementationFactory}.
 *
 * @author Centril<twingoow@gmail.com> / Mazdak Farrokhzad.
 * @version 1.0
 * @since Jan, 24, 2014
 */
public class ImplementationFactoryImpl implements ImplementationFactory {
	protected ImplementationLoader loader;
	protected ImplementationReader reader;

	public ImplementationFactoryImpl( ImplementationLoader loader, ImplementationReader reader ) {
		this.loader = guardNull( loader );
		this.reader = guardNull( reader );
	}

	@Override
	public ImplementationLoader loader() {
		return this.loader;
	}

	@Override
	public ImplementationReader reader() {
		return this.reader;
	}
}