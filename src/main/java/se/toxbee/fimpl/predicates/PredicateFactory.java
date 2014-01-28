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

import java.util.regex.Pattern;

/**
 * PredicateFactory provides some commonly used predicates.
 *
 * @author Centril<twingoow@gmail.com> / Mazdak Farrokhzad.
 * @version 1.0
 * @since Jan, 24, 2014
 */
public class PredicateFactory {
	/* -----------------------------
	 * Predicates: Interface & Class
	 * -----------------------------
	 */

	/**
	 * Creates a predicate for given "interface".
	 *
	 * @param interfase the interface object class.
	 * @param <I>
	 * @return the predicate.
	 */
	public static <I> Predicate<I> forInterface( Class<?> interfase ) {
		return new InterfacePredicate<I>( interfase );
	}

	/**
	 * Creates a predicate for a class name.
	 *
	 * @param name the name to match.
	 * @param <I>
	 * @return the predicate.
	 */
	public static <I> Predicate<I> forClassName( String name ) {
		return new EqualsPredicate<I, String>( new ClassNameTransformer<I>(), name );
	}

	/**
	 * Creates a predicate for class names.
	 *
	 * @param names the names to match.
	 * @param <I>
	 * @return the predicate.
	 */
	public static <I> Predicate<I> forClassName( String... names ) {
		return new EqualsCompositePredicate<I, String>( new ClassNameTransformer<I>(), names );
	}

	/**
	 * Creates a predicate for class names.
	 *
	 * @param pattern the class names regex pattern to match with.
	 * @param <I>
	 * @return the predicate.
	 */
	public static <I> Predicate<I> forClassName( Pattern pattern ) {
		return new RegexPredicate<I>( new ClassNameTransformer<I>(), pattern );
	}

	/* ------------------------
	 * Predicates: Type
	 * ------------------------
	 */

	/**
	 * Creates a predicate for a type.
	 *
	 * @param type the type to match.
	 * @param <I>
	 * @return the predicate.
	 */
	public static <I> Predicate<I> forType( String type ) {
		return new EqualsPredicate<I, String>( new TypeTransformer<I>(), type );
	}

	/**
	 * Creates a predicate for types.
	 *
	 * @param types the types to match.
	 * @param <I>
	 * @return the predicate.
	 */
	public static <I> Predicate<I> forType( String... types ) {
		return new EqualsCompositePredicate<I, String>( new TypeTransformer<I>(), types );
	}

	/**
	 * Creates a predicate for types.
	 *
	 * @param pattern the type regex pattern to match with.
	 * @param <I>
	 * @return the predicate.
	 */
	public static <I> Predicate<I> forType( Pattern pattern ) {
		return new RegexPredicate<I>( new TypeTransformer<I>(), pattern );
	}
}
