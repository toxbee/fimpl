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
package se.toxbee.fimpl.metainf;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;
import java.util.jar.JarFile;

import se.toxbee.fimpl.impl.InterfaceLookupProvider;

/**
 * MetainfLookupProvider looks up in META-INF + jars or folders like it.
 *
 * @author Centril<twingoow@gmail.com> / Mazdak Farrokhzad.
 * @version 1.0
 * @since Jan, 25, 2014
 */
public class MetainfLookupProvider implements InterfaceLookupProvider {
	public MetainfLookupProvider( ClassLoader cl ) {
		this( cl, "META-INF/services/" );
	}

	public MetainfLookupProvider( ClassLoader cl, String path, URL... urls ) {
		this.setSuggestedBase( path );
		this.setURLs( urls );

		// Set classLoader.
		if ( cl == null ) {
			cl = Thread.currentThread().getContextClassLoader();
		}
		this.classLoader = cl;
	}

	protected URL[] urls;
	protected String path;

	protected final ClassLoader classLoader;

	/**
	 * <p>Sets the suggested base path - if the given path is null,<br/>
	 * the InterfaceLookupProvider might provide some other, "natural" base path</p>
	 *
	 * <p>The provider is also free to ignore this base path.</p>
	 *
	 * @param path the path to override
	 */
	public void setSuggestedBase( String path ) {
		if ( path == null ) {
			path = "";
		} else if ( path.length() > 0 && !path.endsWith( "/" ) ) {
			path += '/';
		}

		this.path = path;
	}

	/**
	 * Returns the override base path suggestion.
	 * See {@link #setSuggestedBase(String)} for details.
	 *
	 * @return the base path suggestion.
	 */
	public String getSuggestedBase() {
		return this.path;
	}

	/**
	 * Sets the extra URLs to look in.
	 *
	 * @param urls the urls, if null, there are none.
	 */
	public void setURLs( URL... urls ) {
		if ( urls == null || urls.length == 0 ) {
			this.urls = null;
		} else {
			// Code from org.apache.xbean.finder.ResourceFinder.ImplementationLoader( String path, ClassLoader classLoader, URL... urls )
			for ( int i = 0; i < urls.length; ++i ) {
				URL url = urls[i];
				if ( url == null || isDirectory( url ) || url.getProtocol().equals( "jar" ) ) {
					continue;
				}
				try {
					urls[i] = new URL( "jar", "", -1, url.toString() + "!/" );
				} catch ( MalformedURLException e ) {
					throw new RuntimeException( e );
				}
			}

			this.urls = urls;
		}
	}

	@Override
	public <I> Iterator<InputStream> interfaceLookupStream( Class<I> interfase ) {
		String fullUri = this.path + interfase.getName();

		Enumeration<URL> res;
		try {
			res = this.getResources( fullUri );
		} catch ( IOException e ) {
			return null;
		}

		return res.hasMoreElements() ? new IterAdapter( res ) : null;
	}

	private Enumeration<URL> getResources( String fulluri ) throws IOException {
		if ( urls == null ) {
			return classLoader.getResources( fulluri );
		}
		Vector<URL> resources = new Vector<URL>();
		for ( URL url : urls ) {
			URL resource = findResource( fulluri, url );
			if ( resource != null ) {
				resources.add( resource );
			}
		}
		return resources.elements();
	}

	private static boolean isDirectory( URL url ) {
		String file = url.getFile();
		return (file.length() > 0 && file.charAt( file.length() - 1 ) == '/');
	}

	private URL findResource( String resourceName, URL... search ) {
		// Implementation from: org.apache.xbean.finder.findResource( String resourceName, URL... search )
		for ( int i = 0; i < search.length; ++i ) {
			URL currentUrl = search[i];
			if ( currentUrl == null ) {
				continue;
			}

			try {
				String protocol = currentUrl.getProtocol();
				if ( protocol.equals( "jar" ) ) {
	                /*
                    * If the connection for currentUrl or resURL is
                    * used, getJarFile() will throw an exception if the
                    * entry doesn't exist.
                    */
					URL jarURL = ((JarURLConnection) currentUrl.openConnection()).getJarFileURL();
					JarFile jarFile;
					JarURLConnection juc;
					try {
						juc = makeJUC( jarURL );
						jarFile = juc.getJarFile();
					} catch ( IOException e ) {
						// Don't look for this jar file again
						search[i] = null;
						throw e;
					}

					try {
						juc = makeJUC( jarURL );
						jarFile = juc.getJarFile();
						String entryName;
						if ( currentUrl.getFile().endsWith( "!/" ) ) {
							entryName = resourceName;
						} else {
							String file = currentUrl.getFile();
							int sepIdx = file.lastIndexOf( "!/" );
							if ( sepIdx == -1 ) {
								// Invalid URL, don't look here again
								search[i] = null;
								continue;
							}
							sepIdx += 2;
							entryName = file.substring( sepIdx ) + resourceName;
						}
						if ( entryName.equals( "META-INF/" ) && jarFile.getEntry( "META-INF/MANIFEST.MF" ) != null ) {
							return targetURL( currentUrl, "META-INF/MANIFEST.MF" );
						}
						if ( jarFile.getEntry( entryName ) != null ) {
							return targetURL( currentUrl, resourceName );
						}
					} finally {
						if ( !juc.getUseCaches() ) {
							try {
								jarFile.close();
							} catch ( Exception e ) {
							}
						}
					}
				} else if ( protocol.equals( "file" ) ) {
					String baseFile = currentUrl.getFile();
					String host = currentUrl.getHost();
					int hostLength = 0;
					if ( host != null ) {
						hostLength = host.length();
					}
					StringBuilder buf = new StringBuilder( 2 + hostLength + baseFile.length() + resourceName.length() );

					if ( hostLength > 0 ) {
						buf.append( "//" ).append( host );
					}
					// baseFile always ends with '/'
					buf.append( baseFile );
					String fixedResName = resourceName;
					// Do not create a UNC path, i.e. \\host
					while ( fixedResName.startsWith( "/" ) || fixedResName.startsWith( "\\" ) ) {
						fixedResName = fixedResName.substring( 1 );
					}
					buf.append( fixedResName );
					String filename = buf.toString();
					File file = new File( filename );
					File file2 = new File( URLDecoder.decode( filename, "UTF-8" ) );

					if ( file.exists() || file2.exists() ) {
						return targetURL( currentUrl, fixedResName );
					}
				} else {
					URL resourceURL = targetURL( currentUrl, resourceName );
					URLConnection urlConnection = resourceURL.openConnection();

					try {
						urlConnection.getInputStream().close();
					} catch ( SecurityException e ) {
						return null;
					}
					// HTTP can return a stream on a non-existent file
					// So check for the return code;
					if ( !resourceURL.getProtocol().equals( "http" ) ) {
						return resourceURL;
					}

					int code = ((HttpURLConnection) urlConnection).getResponseCode();
					if ( code >= 200 && code < 300 ) {
						return resourceURL;
					}
				}
			} catch ( MalformedURLException e ) {
				throw new RuntimeException( e );
				// Keep iterating through the URL list
			} catch ( IOException e ) {
				throw new RuntimeException( e );
			} catch ( SecurityException e ) {
				throw new RuntimeException( e );
			}
		}
		return null;
	}

	private URL targetURL( URL base, String name ) throws MalformedURLException {
		return new URL( base.getProtocol(), base.getHost(), base.getPort(), base.getFile() + name, null );
	}

	private JarURLConnection makeJUC( URL jarURL ) throws IOException {
		return (JarURLConnection) new URL( "jar", "", jarURL.toExternalForm() + "!/" ).openConnection();
	}
}
