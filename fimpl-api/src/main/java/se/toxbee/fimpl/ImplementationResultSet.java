package se.toxbee.fimpl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.regex.Pattern;

import se.toxbee.fimpl.common.ImplementationInformation;
import se.toxbee.fimpl.predicates.Predicate;
import se.toxbee.fimpl.predicates.PredicateFactory;

import static se.toxbee.fimpl.Util.guardNull;

/**
 * <p>ImplementationResultSet is the main API for fImpl.</p>
 *
 * <p>It is the class that allows access to the found implementations,<br/>
 * and allows filtering of them.</p>
 *
 * @author Centril<twingoow@gmail.com> / Mazdak Farrokhzad.
 * @version 1.0
 * @since Jan, 23, 2014
 */
public abstract class ImplementationResultSet<I, R extends ImplementationResultSet<I, R>> implements Iterable<ImplementationInformation> {
	protected final static class Impl<I> extends ImplementationResultSet<I, Impl<I>> {
		protected Impl( ImplementationFactory p, Class<I> i, Iterator<ImplementationInformation> d ) {
			super( p, i, d );
		}

		protected Impl( Impl<I> from ) {
			super( from );
		}

		@Override
		public Impl<I> my() {
			return this;
		}

		@Override
		public Impl<I> copy() {
			return new Impl<I>( this );
		}
	}

	/* ----------------------------------
	 * Protected: Fields
	 * ----------------------------------
	 */

	protected final ImplementationFactory provider;
	protected final Class<I> interfase;

	protected boolean consumePredicatesOnFilter = true;
	protected Predicate<I>[] pendingPredicates = null;

	/*
	 * The "set" is actually a LinkedList.
	 *
	 * Motivation:
	 *  - insertion is VERY rare.
	 *  - O(1) removal during iteration needed.
	 *  - the "set" must be sortable by natural order (LinkedHashSet: insertion order)
	 *      -> requires List interface.
	 *  - the "set" must at least provide getFirst() in Deque interface.
	 */
	protected final LinkedList<ImplementationInformation> set;

	/* ----------------------
	 * Constructors & related
	 * ----------------------
	 */

	/**
	 * Constructor.
	 *
	 * @param provider the provider.
	 * @param interfase the interface class.
	 * @param initData the data to initalize set with, obviously not null.
	 */
	protected ImplementationResultSet( ImplementationFactory provider, Class<I> interfase, Iterator<ImplementationInformation> initData ) {
		// Set provider.
		this.provider = guardNull( provider );

		// Set the interface class object.
		this.interfase = guardNull( interfase );

		// Init the set.
		this.set = new LinkedList<ImplementationInformation>();

		// Init the set data.
		this.fixListState( this.fillSet( new HashSet<ImplementationInformation>(), initData ) );
	}

	/**
	 * Fills the given set with an iterator and returns it.
	 *
	 * @param set the set to add to.
	 * @param iter the iterator add to list, doesn't actually check for null when adding.
	 * @return the given set.
	 */
	protected <T> Set<T> fillSet( Set<T> set, Iterator<T> iter ) {
		if ( iter != null ) {
			while ( iter.hasNext() ) {
				set.add( iter.next() );
			}
		}

		return set;
	}

	/**
	 * Fixes the state of the set, sorts the set.
	 *
	 * @param initSet set will be filled with initSet.
	 */
	protected void fixListState( Set<ImplementationInformation> initSet ) {
		this.set.clear();
		this.set.addAll( initSet );

		// Sort set.
		Collections.sort( this.set );
	}

	/**
	 * <p>Copy constructor, should always be overridden.</p>
	 *
	 * <p>Performs a shallow copy with:</p>
	 * <ul>
	 *     <li>the provider</li>
	 *     <li>the interface class</li>
	 *     <li>the prending predicates</li>
	 *     <li>the "consumePredicatesOnFilter" flag</li>
	 * </ul>
	 *
	 * <p>Performs a deep copy with:</p>
	 * <ul>
	 *     <li>the set, not copy of the actual elements in it.</li>
	 * </ul>
	 *
	 * @param from the set to copy from.
	 */
	protected ImplementationResultSet( ImplementationResultSet<I, ?> from ) {
		// Shallow copy with general fields.
		this.provider = from.provider;
		this.interfase = from.interfase;
		this.consumePredicatesOnFilter = from.consumePredicatesOnFilter;
		this.pendingPredicates = from.pendingPredicates;

		// Deep copy of set itself (not elements).
		this.set = new LinkedList<ImplementationInformation>( from.set );
	}

	/**
	 * <p>Returns a copy of the set, should always be overridden.</p>
	 *
	 * The set is not immutable, thus any modifying operation is destructive.
	 * If you want to avoid the destructiveness, make a copy before altering.
	 *
	 * <p>Performs a shallow copy with:</p>
	 * <ul>
	 *     <li>the provider</li>
	 *     <li>the interface class</li>
	 *     <li>the prending predicates</li>
	 *     <li>the "consumePredicatesOnFilter" flag</li>
	 * </ul>
	 *
	 * <p>Performs a deep copy with:</p>
	 * <ul>
	 *     <li>the set, not copy of the actual elements in it.</li>
	 * </ul>
	 *
	 * @return the shallow copy.
	 */
	abstract public R copy();

	/**
	 * Returns this.
	 *
	 * @return this.
	 */
	abstract public R my();

	/* --------------------------------------
	 * Public API: Accessors for dependencies
	 * --------------------------------------
	 */

	/**
	 * Returns the provider the set is using.
	 *
	 * @return the provider.
	 */
	public ImplementationFactory provider() {
		return this.provider;
	}

	/**
	 * Returns the class of interface that we're finding implementations for.
	 *
	 * @return the class.
	 */
	public Class<I> interfase() {
		return this.interfase;
	}

	/* -----------------------------------------------
	 * Public API: Accessing the front implementation.
	 * -----------------------------------------------
	 */

	/**
	 * Returns the first class object.
	 * It's the one with the highest priority.
	 * Note: the class will not have been initialized.
	 *
	 * @return the loaded class object.
	 */
	public Class<? extends I> first() {
		return this.provider.loader().loadImplementation( this.firstInfo(), this.interfase() );
	}

	/**
	 * Returns the first ImplementationInformation object.
	 * It's the one with the highest priority.
	 *
	 * @return the first info.
	 */
	public ImplementationInformation firstInfo() {
		return this.set.getFirst();
	}

	/* ---------------------------------
	 * Public API: Backend set forwarded
	 * ---------------------------------
	 */

	/**
	 * Returns the size of the set.
	 *
	 * @return the size.
	 */
	public int size() {
		return this.set.size();
	}

	/**
	 * Returns true if the set is empty.
	 *
	 * @return true if empty.
	 */
	public boolean isEmpty() {
		return this.size() == 0;
	}

	/**
	 * Clears the set.
	 * {@link #size()} will be 0 after.
	 *
	 * @return my()
	 */
	public R clear() {
		this.set.clear();
		return my();
	}

	public Iterator<ImplementationInformation> iterator() {
		return this.set.iterator();
	}

	/**
	 * Returns a descending iterator for set.
	 *
	 * @return the iterator.
	 */
	public Iterator<ImplementationInformation> decendingIterator() {
		return this.set.descendingIterator();
	}

	/* -------------------
	 * Public API: Merging
	 * -------------------
	 */

	/**
	 * Joins this set with another one.
	 *
	 * @param rhs the other set.
	 * @return my()
	 */
	public R join( R rhs ) {
		return this.join( rhs.iterator() );
	}

	/**
	 * Joins this set with rhs.
	 *
	 * @param rhs the info.
	 * @return my()
	 */
	public R join( ImplementationInformation rhs ) {
		return this.join( Collections.singletonList( rhs ) );
	}

	/**
	 * Joins this set with another one.
	 *
	 * @param rhs the other set.
	 * @return my()
	 */
	public R join( Iterable<ImplementationInformation> rhs ) {
		return this.join( rhs.iterator() );
	}

	/**
	 * Joins this set with another one.
	 *
	 * @param rhs the other "set".
	 * @return my()
	 */
	public R join( Iterator<ImplementationInformation> rhs ) {
		// Reserve capacity: rhs + this.
		Set<ImplementationInformation> init = new HashSet<ImplementationInformation>();

		// Add this set to init.
		init.addAll( this.set );

		// Add "rhs" to init & Fix the state:
		this.fixListState( this.fillSet( init, rhs ) );

		return my();
	}

	/* ----------------------------------------------
	 * Public API, Predicate Logic: Interface & Class
	 * ----------------------------------------------
	 */

	/**
	 * Equivalent of {@link #use(Predicate[])}
	 * with {@link se.toxbee.fimpl.predicates.PredicateFactory#forInterface(Class)}.
	 *
	 * @param interfase the interface class object.
	 * @return my()
	 */
	@SuppressWarnings("unchecked")
	public R interfase( Class<?> interfase ) {
		return this.use( PredicateFactory.<I>forInterface( interfase ) );
	}

	/**
	 * See {@link #interfase(Class)}, works like that, but for many interfaces.
	 *
	 * @param interfases the interfaces.
	 * @return my()
	 */
	@SuppressWarnings("unchecked")
	public R interfase( Class<?>... interfases ) {
		// Create array of predicates.
		Predicate<I>[] ps = new Predicate[interfases.length];
		for ( int i = 0; i < interfases.length; ++i ) {
			ps[i] = PredicateFactory.forInterface( interfases[i] );
		}

		return this.use( ps );
	}

	/**
	 * Equivalent of {@link #use(Predicate[])}
	 * with {@link se.toxbee.fimpl.predicates.PredicateFactory#forClassName(String)}.
	 *
	 * @param name the class name.
	 * @return my()
	 */
	@SuppressWarnings("unchecked")
	public R className( String name ) {
		return this.use( PredicateFactory.<I>forClassName( name ) );
	}

	/**
	 * Equivalent of {@link #use(Predicate[])}
	 * with {@link se.toxbee.fimpl.predicates.PredicateFactory#forClassName(String...)}.
	 *
	 * @param names the class names.
	 * @return my()
	 */
	@SuppressWarnings("unchecked")
	public R className( String... names ) {
		return this.use( PredicateFactory.<I>forClassName( names ) );
	}

	/**
	 * Equivalent of {@link #use(Predicate[])}
	 * with {@link se.toxbee.fimpl.predicates.PredicateFactory#forClassName(java.util.regex.Pattern)}.
	 *
	 * @param pattern the class name regex pattern matcher.
	 * @return my()
	 */
	@SuppressWarnings("unchecked")
	public R className( Pattern pattern ) {
		return this.use( PredicateFactory.<I>forClassName( pattern ) );
	}

	/* ---------------------------------
	 * Public API, Predicate Logic: Type
	 * ---------------------------------
	 */

	/**
	 * Equivalent of {@link #use(Predicate[])}
	 * with {@link se.toxbee.fimpl.predicates.PredicateFactory#forType(String)}.
	 *
	 * @param type the type.
	 * @return my()
	 */
	@SuppressWarnings("unchecked")
	public R type( String type ) {
		return this.use( PredicateFactory.<I>forType( type ) );
	}

	/**
	 * Equivalent of {@link #use(Predicate[])}
	 * with {@link se.toxbee.fimpl.predicates.PredicateFactory#forType(String...)}.
	 *
	 * @param types the types.
	 * @return my()
	 */
	@SuppressWarnings("unchecked")
	public R type( String... types ) {
		return this.use( PredicateFactory.<I>forType( types ) );
	}

	/**
	 * Equivalent of {@link #use(Predicate[])}
	 * with {@link se.toxbee.fimpl.predicates.PredicateFactory#forType(java.util.regex.Pattern)}.
	 *
	 * @param pattern the type regex pattern matcher.
	 * @return my()
	 */
	@SuppressWarnings("unchecked")
	public R type( Pattern pattern ) {
		return this.use( PredicateFactory.<I>forType( pattern ) );
	}

	/* ----------------------------------------
	 * Public API, Predicate Logic: General API
	 * ----------------------------------------
	 */

	/**
	 * Sets the list of pending predicates.
	 *
	 * @param predicates the predicates.
	 * @return my()
	 */
	public R use( Predicate<I>... predicates ) {
		this.pendingPredicates = predicates;
		return my();
	}

	/**
	 * Forgets pending predicates if any.
	 *
	 * @return my()
	 */
	@SuppressWarnings("unchecked")
	public R forget() {
		return this.use( (Predicate<I>[]) null );
	}

	/**
	 * Filters the set with a given set of matcher predicates.
	 * All elements that match ANY of the given predicates are RETAINED.
	 *
	 * @param predicates the predicates.
	 * @return my()
	 */
	public R retainAny( Predicate<I>... predicates ) {
		return this.filter( false, false, predicates );
	}

	/**
	 * Filters the set with a given set of matcher predicates.
	 * All elements that match ANY of the given predicates are REMOVED.
	 *
	 * @param predicates the predicates.
	 * @return my()
	 */
	public R removeAny( Predicate<I>... predicates ) {
		return this.filter( true, false, predicates );
	}

	/**
	 * Filters the set with a given set of matcher predicates.
	 * All elements that match ALL of the given predicates are RETAINED.
	 *
	 * @param predicates the predicates.
	 * @return my()
	 */
	public R retainAll( Predicate<I>... predicates ) {
		return this.filter( false, true, predicates );
	}

	/**
	 * Filters the set with a given set of matcher predicates.
	 * All elements that match ALL of the given predicates are REMOVED.
	 *
	 * @param predicates the predicates.
	 * @return my()
	 */
	public R removeAll( Predicate<I>... predicates ) {
		return this.filter( true, true, predicates );
	}

	/**
	 * Filters the set with a given set of matcher predicates.
	 * Core filter method.
	 *
	 * @param removeOn for which matcher result to keep, true = remove, false = retain
	 * @param allMode using all mode, or any mode.
	 * @param predicates the predicates.
	 * @return my()
	 */
	protected R filter( boolean removeOn, boolean allMode, Predicate<I>... predicates ) {
		// Try to recover with last predicates.
		predicates = this.recoverWithPending( predicates );

		if ( predicates.length > 0 ) {
			this.filterInner( removeOn, allMode, predicates );
		}

		return this.consumeIf();
	}

	protected void filterInner( boolean removeOn, boolean allMode, Predicate<I>[] predicates ) {
		boolean anyMode = !allMode;
		Iterator<ImplementationInformation> iter = this.set.iterator();

		while( iter.hasNext() ) {
			ImplementationInformation info = iter.next();

			boolean remove = allMode;
			for ( Predicate<I> p : predicates ) {
				if ( this.isEmpty() ) {
					return;
				}

				if ( p.match( info, this, anyMode ) == anyMode )  {
					remove = anyMode;
					break;
				}
			}

			if ( remove == removeOn ) {
				iter.remove();
			}
		}
	}

	/* --------------------------------------------
	 * Public API, Predicate Logic: General Utility
	 * --------------------------------------------
	 */

	/**
	 * Returns the list of pending predicates, if any.
	 *
	 * @return the predicates, or null.
	 */
	public Predicate<I>[] pendingPredicates() {
		return this.pendingPredicates;
	}

	/**
	 * Sets whether or not consuming predicates on filter is allowed.
	 *
	 * @param allowed true if it is allowed.
	 * @return my()
	 */
	public R consumePredicatesOnFilter( boolean allowed ) {
		this.consumePredicatesOnFilter = allowed;
		return my();
	}

	/**
	 * Returns whether or not we are consuming predicates on filter.
	 *
	 * @return true if we are.
	 */
	public boolean isConsumePredicatesOnFilter() {
		return this.consumePredicatesOnFilter;
	}

	/* ---------------------------------
	 * Private utility, Predicate Logic:
	 * ---------------------------------
	 */

	protected Predicate<I>[] recoverWithPending( Predicate<I>[] predicates ) {
		if ( predicates == null || predicates.length == 0 ) {
			predicates = this.pendingPredicates();
		} else {
			this.use( predicates );
		}

		return predicates;
	}

	protected R consumeIf() {
		return this.isConsumePredicatesOnFilter() ? this.forget() : my();
	}
}
