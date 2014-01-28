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

import se.toxbee.fimpl.ImplementationInformation;

/**
 * Basic field based implementation of {@link ImplementationInformation}.
 *
 * @author Centril<twingoow@gmail.com> / Mazdak Farrokhzad.
 * @version 1.0
 * @since Jan, 24, 2014
 */
public class ImplementationInformationImpl extends ImplementationInformation {
	protected final String implementorClass;
	protected final int priority;
	protected final String type;
	protected final Object extras;

	/**
	 * Constructs the information entity with implementorClass, priority and no type or extras.
	 *
	 * @param implementorClass the relative or fully qualified name of the implementing class.
	 * @param priority the priority in chain of information entity.
	 */
	public ImplementationInformationImpl( String implementorClass, int priority ) {
		this( implementorClass, priority, null );
	}

	/**
	 * Constructs the information entity with implementorClass, priority and optional type but no extras.
	 *
	 * @param implementorClass the relative or fully qualified name of the implementing class.
	 * @param priority the priority in chain of information entity.
	 * @param type optional type.
	 */
	public ImplementationInformationImpl( String implementorClass, int priority, String type ) {
		this( implementorClass, priority, type, null );
	}

	/**
	 * Constructs the information entity with implementorClass, priority and optional type and extras.
	 *
	 * @param implementorClass the relative or fully qualified name of the implementing class.
	 * @param priority the priority in chain of information entity.
	 * @param type optional type.
	 * @param extras optional extra data.
	 */
	public ImplementationInformationImpl( String implementorClass, int priority, String type, Object extras ) {
		this.implementorClass = implementorClass;
		this.priority = priority;
		this.type = type;
		this.extras = extras;
	}

	/**
	 * Copy constructor.
	 *
	 * @param cpy the copy constructor.
	 */
	public ImplementationInformationImpl( ImplementationInformation cpy ) {
		this( cpy.getImplementorClass(), cpy.getPriority(), cpy.getType(), cpy.getExtras() );
	}

	@Override
	public String getImplementorClass() {
		return this.implementorClass;
	}

	@Override
	public int getPriority() {
		return this.priority;
	}

	@Override
	public String getType() {
		return this.type;
	}

	public Object getExtras() {
		return this.extras;
	}
}