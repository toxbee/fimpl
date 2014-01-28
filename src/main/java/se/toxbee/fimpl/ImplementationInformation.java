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
}
