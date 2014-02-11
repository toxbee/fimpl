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
import java.util.Iterator;

import se.toxbee.fimpl.common.ImplementationInformation;
import se.toxbee.fimpl.ImplementationReader;

import static se.toxbee.fimpl.common.Util.guardNull;

/**
 * ImplementationReaderPipe is a "pipe" separating<br/>
 * the finding of interface metadata indexes (files, whatever),<br/>
 * and the translation-into-memory of the indexes.
 *
 * @author Centril<twingoow@gmail.com> / Mazdak Farrokhzad.
 * @version 1.0
 * @since Jan, 25, 2014
 */
public class ImplementationReaderPipe implements ImplementationReader {
	protected final CollectionIndexTransformer transformer;
	protected final InterfaceLookupProvider lookupProvider;

	/**
	 * Constructs the "pipe".
	 *
	 * @param transformer the transformer of index files to in-memory-metadata.
	 * @param lookupProvider the lookup provider of index files.
	 */
	public ImplementationReaderPipe( CollectionIndexTransformer transformer, InterfaceLookupProvider lookupProvider ) {
		this.transformer = guardNull( transformer );
		this.lookupProvider = guardNull( lookupProvider );
	}

	@Override
	public <I> Iterator<ImplementationInformation> readImplementationCollection( Class<I> interfase ) {
		Iterator<InputStream> in = this.lookupProvider.interfaceLookupStream( interfase );
		return this.transformer.readImplementationCollection( in );
	}
}
