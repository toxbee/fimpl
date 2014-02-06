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
package se.toxbee.fimpl.common;

/**
 * ImplementationInformation provides information about classes that implement an "interface".
 *
 * @author Centril<twingoow@gmail.com> / Mazdak Farrokhzad.
 * @version 1.0
 * @since Jan, 17, 2014
 */
public abstract class ImplementationInformation implements Comparable<ImplementationInformation> {
	/* -----------------
	 * Abstract methods:
	 * -----------------
	 */

	/**
	 * Returns the relative or fully qualified class name of the implementing class.<br/>
	 * If relative, a base package must be added to it when loading the class.
	 * Is guaranteed not to be null.
	 *
	 * @return the class name.
	 */
	abstract public String getImplementorClass();

	/**
	 * Returns the priority in the chain of information entites.
	 * In interval [{@link Integer#MIN_VALUE}, {@link Integer#MAX_VALUE}.
	 *
	 * @return the priority.
	 */
	abstract public int getPriority();

	/**
	 * Returns the type - the meaning is subjective.
	 * Can be null.
	 *
	 * @return the type.
	 */
	abstract public String getType();

	/**
	 * Returns any extra data - the meaning is subjective.
	 * Will return null for most use cases.
	 *
	 * @return any extra data.
	 */
	abstract public Object getExtras();

	/* -------------------------
	 * Enforced implementations:
	 * -------------------------
	 */

	/**
	 * Constructor, enforces non-empty implementorClass.
	 *
	 * @param implementorClass the non-empty implementorClass.
	 */
	protected ImplementationInformation( String implementorClass ) {
		if ( implementorClass == null || implementorClass.isEmpty() ) {
			throw new IllegalArgumentException();
		}
	}

	/**
	 * Must return hashCode of {@link #getImplementorClass()}.
	 *
	 * @return the hashCode.
	 */
	@Override
	public final int hashCode() {
		return this.getImplementorClass().hashCode();
	}

	/**
	 * Returns true iff equals( getImplementorClass(), o.getImplementorClass() )
	 *
	 * @param o the other object.
	 * @return true iff getImplementorClass() on both sides.
	 */
	@Override
	public final boolean equals( Object o ) {
		// We are allowed to violate LSP (Liskovs Substitution Principle) here.
		return this == o || (o instanceof ImplementationInformation && this.hashCode() == o.hashCode());
	}

	@Override
	public final int compareTo( ImplementationInformation another ) {
		return -(this.getPriority() - another.getPriority());
	}

	/**
	 * Basic field based implementation of {@link se.toxbee.fimpl.common.ImplementationInformation}.
	 *
	 * @author Centril<twingoow@gmail.com> / Mazdak Farrokhzad.
	 * @version 1.0
	 * @since Jan, 24, 2014
	 */
	public static class Impl extends ImplementationInformation {
		protected final String implementorClass;
		protected final int priority;
		protected final String type;
		protected final Object extras;

		/**
		 * Constructs info from a series of strings.
		 * Any length >= 1 is allowed.
		 * Array layout: [implementorClass, (priority, (type, (extras)))]
		 *
		 * @param strings the array of string data.
		 * @return null if strings is null or empty.
		 */
		public static Impl from( String... strings ) {
			if ( strings == null && strings.length == 0 ) {
				return null;
			}

			Object[] data = new Object[4];

			switch ( strings.length ) {
				default:
				case 3:
					data[3] = strings[3];
				case 2:
					data[2] = strings[2];
				case 1:
					data[1] = Integer.parseInt( strings[1] );
				case 0:
					data[0] = strings[0];
			}

			return new Impl( data );
		}

		/**
		 * Constructs the information entity with implementorClass, 0 as priority and no type or extras.
		 *
		 * @param implementorClass the relative or fully qualified name of the implementing class.
		 */
		public Impl( String implementorClass ) {
			this( implementorClass, 0 );
		}

		/**
		 * Constructs the information entity with implementorClass, priority and no type or extras.
		 *
		 * @param implementorClass the relative or fully qualified name of the implementing class.
		 * @param priority the priority in chain of information entity.
		 */
		public Impl( String implementorClass, int priority ) {
			this( implementorClass, priority, null );
		}

		/**
		 * Constructs the information entity with implementorClass, priority and optional type but no extras.
		 *
		 * @param implementorClass the relative or fully qualified name of the implementing class.
		 * @param priority the priority in chain of information entity.
		 * @param type optional type.
		 */
		public Impl( String implementorClass, int priority, String type ) {
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
		public Impl( String implementorClass, int priority, String type, Object extras ) {
			super( implementorClass );
			this.implementorClass = implementorClass;
			this.priority = priority;
			this.type = type == null ? null : (type.equals( "" ) ? null : type);
			this.extras = extras;
		}

		/**
		 * Constructs the information entity with [implementorClass, priority, type, extras].
		 *
		 * @param data the data array to construct from.
		 */
		public Impl( Object[] data ) {
			this( (String) data[0], (Integer) data[1], (String) data[2], data[3] );
		}

		/**
		 * Copy constructor.
		 *
		 * @param cpy the copy constructor.
		 */
		public Impl( ImplementationInformation cpy ) {
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
}
