package se.toxbee.fimpl.predicates;

import se.toxbee.fimpl.ImplementationResultSet;
import se.toxbee.fimpl.common.ImplementationInformation;

/**
 * PredicateInputTransformer transforms input given by info or set to the way a predicates wants it.
 *
 * @author Centril<twingoow@gmail.com> / Mazdak Farrokhzad.
 * @version 1.0
 * @since Jan, 24, 2014
 */
public interface PredicateInputTransformer<I, A> {
	/**
	 * Transforms data from either info or set to the way that
	 * a {@link Predicate} understands.
	 *
	 * @param info the information.
	 * @param set the entire set.
	 * @return the transformed input.
	 */
	public A transformForPredicate( ImplementationInformation info, ImplementationResultSet<I, ?> set );
}
