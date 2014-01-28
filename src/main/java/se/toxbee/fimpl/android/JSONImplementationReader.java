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
package se.toxbee.fimpl.android;

import java.io.IOException;
import java.io.InputStream;

import org.json.JSONException;
import org.json.JSONArray;
import org.json.JSONObject;

import se.toxbee.fimpl.ImplementationReader;

/**
 *
 * @author Centril<twingoow@gmail.com> / Mazdak Farrokhzad.
 * @version 1.0
 * @since Jan, 17, 2014
 */
public class JSONImplementationReader implements ImplementationReader {
	@Override
	public ImplementationCollection readImplementationCollection( InputStream in ) {
		try {
			String data = InputStreamHelper.toString( in );
			return this.read( data );
		} catch ( JSONException e ) {
			return null;
		} catch ( IOException e) {
			return null;
		}
	}

	private ImplementationCollection read( String in ) throws JSONException {
		JSONArray root = new JSONArray( in );
		return null;
	}
}
