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

import se.toxbee.fimpl.ImplementationResultSet;
import se.toxbee.fimpl.common.ImplementationInformation;

import static se.toxbee.fimpl.common.Util.guardNull;

/**
 * RegexPredicate uses a regex pattern to match against something.
 *
 * @param <I> the interface type of the set.
 * @author Centril<twingoow@gmail.com> / Mazdak Farrokhzad.
 * @version 1.0
 * @since Jan, 24, 2014
 */
public class RegexPredicate<I> implements Predicate<I> {
	protected final PredicateInputTransformer<I, ? extends CharSequence> transformer;
	protected final Pattern pattern;

	/**
	 * Constructs the predicates using the given pattern.
	 *
	 * @param transformer the transformer to use for equals.
	 * @param pattern the pattern.
	 */
	public RegexPredicate( PredicateInputTransformer<I, ? extends CharSequence> transformer, Pattern pattern ) {
		this.pattern = guardNull( pattern );
		this.transformer = guardNull( transformer );
	}

	/**
	 * Returns the pattern the predicates is using.
	 *
	 * @return the pattern.
	 */
	public Pattern pattern() {
		return this.pattern;
	}

	@Override
	public boolean match( ImplementationInformation info, ImplementationResultSet<I, ?> set, boolean anyMode ) {
		CharSequence input = this.transformer.transformForPredicate( info, set );
		return input != null && this.pattern.matcher( input ).matches();
	}
}
