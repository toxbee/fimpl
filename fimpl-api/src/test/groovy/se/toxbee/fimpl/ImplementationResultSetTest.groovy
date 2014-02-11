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

package se.toxbee.fimpl

import se.toxbee.fimpl.common.ImplementationInformation
import se.toxbee.fimpl.impl.ImplementationFactoryImpl
import se.toxbee.fimpl.impl.StandardClassLoader
import se.toxbee.fimpl.predicates.InterfacePredicate
import se.toxbee.fimpl.predicates.Predicate

import spock.lang.Specification

class ImplementationResultSetTest extends Specification {
	interface iface {}

	// Keep these classes!
	class dummy0 implements iface {}
	class dummy1 implements iface {}
	class dummy2 implements iface {}
	class dummy3 implements iface {}
	class dummy4 implements iface {}
	class dummy5 implements iface {}
	class dummy6 implements iface {}
	class dummy7 implements iface {}
	class dummy8 implements iface {}
	class dummy9 implements iface {}
	List<ImplementationInformation> dummyInfos() {
		def pkg = this.getClass().getName()
		(0..9).collect { new ImplementationInformation.Impl( "${pkg}\$dummy${it}", it, "type-${it}", it ) }
	}

	def ImplementationReader reader
	def ImplementationLoader loader
	def ImplementationFactory provider
	def ImplementationResultSet.Impl<iface> resultSet

	void setup() {
		reader = Mock( ImplementationReader )
		loader = new StandardClassLoader()
		provider = new ImplementationFactoryImpl( loader, reader )
	}
	ImplementationResultSet.Impl<iface> makeSet( Iterable<ImplementationInformation> iter = null ) {
		resultSet = new ImplementationResultSet.Impl<iface>( provider, iface, iter?.iterator() )
		return resultSet
	}
	ImplementationResultSet.Impl<iface> fillSet() {
		makeSet( dummyInfos() )
	}

	def "My"() {
		expect:
			makeSet().my() == resultSet
	}

	def "Copy"() {
		given:
			def copy = fillSet().copy()
		expect:
			["class", "provider", "interfase", "consumePredicatesOnFilter", "pendingPredicates"].each {
				assert copy."${it}" == resultSet."${it}"
			}
			copy.set.containsAll( resultSet.set )
	}

	def "FillSet"() {
		given:
			makeSet()
			def set = new HashSet<?>();
			def dummies = dummyInfos()
		when:
			def set1 = resultSet.fillSet( set, null )
		then:
			set1 == set
			set1.isEmpty()
		when:
			resultSet.fillSet( set, dummies.iterator() )
		then:
			set.size() == dummies.size()
	}

	def "FixListState"() {
		given:
			def dum = dummyInfos()
			makeSet( dum )

			def unique = new ImplementationInformation.Impl( "unique", Integer.MAX_VALUE )
			dum.add( unique )

			def addSet = new HashSet<ImplementationInformation>()
			addSet.addAll( dum )
		when:
			resultSet.fixListState( addSet )
		then:
			resultSet.set.size() == dum.size()
			resultSet.set.getFirst() == unique
	}

	def "Provider"() {
		expect:
			makeSet().provider() == provider
	}

	def "Interfase"() {
		expect:
			makeSet().interfase() == iface
	}

	def "First"() {
		expect:
			fillSet().first() == dummy9
	}

	def "FirstInfo"() {
		expect:
			fillSet().firstInfo().getImplementorClass() == dummy9.getName()
	}

	def "Size"() {
		expect:
			makeSet().size() == 0
			fillSet().size() == 10
	}

	def "IsEmpty"() {
		expect:
			makeSet().isEmpty()
			!fillSet().isEmpty()
	}

	def "Clear"() {
		expect:
			fillSet().clear().isEmpty()
	}

	def "iterator"() {
		given:
			def a = fillSet().set.iterator().collect()
			def b = resultSet.iterator().collect()
		expect:
			a == b
	}

	def "decendingIterator"() {
		given:
			def a = fillSet().set.descendingIterator().collect()
			def b = resultSet.decendingIterator().collect()
		expect:
			a == b
	}

	def "loadingIterable"() {
		testLoadingIterable( { it.loadingIterable() }, { it } )
	}

	def "loadingDecendingIterable"() {
		testLoadingIterable( { it.loadingDecendingIterable() }, { it.reverse() } )
	}

	def testLoadingIterable( Closure<Iterable<Class<? extends iface>>> ac, Closure<Iterable<ImplementationInformation>> bc ) {
		def s = fillSet()
		def a = ac( s ).collect()
		def b = bc( dummyInfos() ).collect { s.load( it ) }

		assert a == b
	}

	def "Join"() {
		given:
			def dum = dummyInfos()
			def unique = new ImplementationInformation.Impl( "unique" )
		expect:
			makeSet( dum ).join( makeSet() ).size() == dum.size()
			makeSet( dum ).join( resultSet.copy() ).size() == dum.size()
			makeSet( dum ).join( dum ).size() == dum.size()
			makeSet( dum ).join( [unique] )
			              .join( [unique] )
						  .join( unique )
			              .size() == dum.size() + 1
	}

	def "Interfase( Class<?> interfase )"() {
		expect:
			makeSet().interfase( iface ).pendingPredicates()[0] instanceof InterfacePredicate
	}

	def "Interfase( Class<?>... interfases )"() {
		expect:
			makeSet().interfase( iface, iface ).pendingPredicates().each {
				assert it instanceof InterfacePredicate
			}
	}

	def "Use"() {
		given:
			def predicate = Mock(Predicate)
		expect:
			makeSet().use( predicate ).pendingPredicates()[0] == predicate
	}

	def "Forget"() {
		expect:
			makeSet().use( Mock(Predicate) ).forget().pendingPredicates() == null
	}

	def T = new pred(true)

	def F = new pred(false)

	def "RetainAny"() {
		given:
			def set = fillSet()
		expect:
			!set.retainAny( arr([T, F]) ).isEmpty()
			!set.retainAny( arr([T, T]) ).isEmpty()
			set.retainAny( arr([F, F]) ).isEmpty()
	}

	def "RemoveAny"() {
		given:
			def set = fillSet()
		expect:
			!set.removeAny( arr([F, F]) ).isEmpty()
			set.removeAny( arr([T, F]) ).isEmpty()
			set.removeAny( arr([T, T]) ).isEmpty()
	}

	def "RetainAll"() {
		given:
			def set = fillSet()
		expect:
			!set.retainAll( arr([T, T]) ).isEmpty()
			set.retainAll( arr([T, F]) ).isEmpty()
	}

	def "RemoveAll"() {
		given:
			def set = fillSet()
		expect:
			!set.removeAll( arr([T, F]) ).isEmpty()
			set.removeAll( arr([T, T]) ).isEmpty()
	}

	def "ConsumePredicatesOnFilter"() {
		given:
			def set = makeSet()
		expect:
			set.isConsumePredicatesOnFilter()
			!set.consumePredicatesOnFilter( false ).isConsumePredicatesOnFilter()
			set.consumePredicatesOnFilter( true ).isConsumePredicatesOnFilter()
	}

	def "RecoverWithPending"() {
		given:
			def set = makeSet()
			def predicates = arr( [T] )
			set.use( predicates )
		expect:
			set.recoverWithPending( null ) == predicates
			set.recoverWithPending( arr( [] ) ) == predicates
			set.forget().recoverWithPending( predicates ) == predicates
	}

	def "ConsumeIf"() {
		given:
			def p = Mock( Predicate )
			def set = makeSet()
			def test = { boolean consume ->
				set.consumePredicatesOnFilter( consume ).use( p )
				   .consumeIf().pendingPredicates()
			}
		expect:
			test( true ) == null
			test( false )[0] == p
	}

	def arr( Collection<Predicate> predicates ) {
		(Predicate[]) predicates.toArray()
	}

	class pred implements Predicate<iface> {
		def boolean val

		pred( boolean val ) {
			this.val = val
		}

		@Override
		boolean match( ImplementationInformation info, ImplementationResultSet<iface, ?> set, boolean anyMode ) {
			return val
		}
	}
}
