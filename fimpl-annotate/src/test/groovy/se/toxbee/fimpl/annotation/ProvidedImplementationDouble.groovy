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

package se.toxbee.fimpl.annotation
import java.lang.annotation.Documented
import java.lang.annotation.Retention
import java.lang.annotation.Target

import static java.lang.annotation.ElementType.TYPE
import static java.lang.annotation.RetentionPolicy.RUNTIME
/**
 * Test double for {@link se.toxbee.fimpl.annotation.ProvidedImplementation}.<br/>
 * Using {@link java.lang.annotation.RetentionPolicy#RUNTIME} instead.
 *
 * @author Centril<twingoow@gmail.com> / Mazdak Farrokhzad.
 * @version 1.0
 * @since Feb, 5, 2014
 */
@Retention(RUNTIME)
@Documented
@Target(TYPE)
public @interface ProvidedImplementationDouble {
	/**
	 * <p>The "class/interface" (henceforth "interface")<br/>
	 * that the annotated class is an implementation for.</p>
	 *
	 * <p>If {@link #implementing()} == {@link Void#TYPE},<br/>
	 * then the class for which this annotation is placed only<br/>
	 * has one base class or interface then that is the {interface}.</p>
	 *
	 * @return the "interface" that the annotated is an implementation of.
	 */
	Class implementing() default void.class;

	/**
	 * The priority of the implementation in the set of implementations.<br/>
	 * The {@link Integer#MAX_VALUE}, {@link Integer#MIN_VALUE} has the highest
	 * and lowest priorities respectively.
	 *
	 * @return the priority.
	 */
	int priority() default 0;

	/**
	 * The type of the implementation - not guaranteed to be a unique value.<br/>
	 * An empty value should be understood as "null" type.
	 *
	 * @return the type.
	 */
	String type() default "";
}
