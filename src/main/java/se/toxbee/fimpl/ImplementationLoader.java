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
 * ImplementationLoader loads implementation classes.
 *
 * @author Centril<twingoow@gmail.com> / Mazdak Farrokhzad.
 * @version 1.0
 * @since Jan, 17, 2014
 */
public interface ImplementationLoader {
	/**
	 * Loads an implementation class of targetType given the implementation info and a base package.
	 *
	 * @param info the implementation info to use to instantiate implementation.
	 * @param targetType the target type, the interface that is.
	 * @param <T> the type of the targetType
	 * @return the implementation class, or null on error.
	 */
	public <I> Class<? extends I> loadImplementation( ImplementationInformation info, Class<I> targetType );
}
