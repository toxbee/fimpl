/*
 * Copyright (c) 2014. See AUTHORS file.
 *
 * This file is part of SleepFighter.
 *
 * SleepFighter is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SleepFighter is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SleepFighter. If not, see <http://www.gnu.org/licenses/>.
 */
package org.apache.xbean.finder;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author Centril<twingoow@gmail.com> / Mazdak Farrokhzad.
 */
public class ImplementationLoader {

	private final URL[] urls;
	private final String path;
	private final ClassLoader classLoader;

	public ImplementationLoader( URL... urls ) {
		this(null, Thread.currentThread().getContextClassLoader(), urls);
	}

	public ImplementationLoader( String path ) {
		this(path, Thread.currentThread().getContextClassLoader(), (URL[]) null);
	}

	public ImplementationLoader( String path, URL... urls ) {
		this(path, Thread.currentThread().getContextClassLoader(), urls);
	}

	public ImplementationLoader( String path, ClassLoader classLoader ) {
		this(path, classLoader, (URL[]) null);
	}

	public ImplementationLoader( String path, ClassLoader classLoader, URL... urls ) {
		if (path == null){
			path = "";
		} else if (path.length() > 0 && !path.endsWith("/")) {
			path += "/";
		}
		this.path = path;

		if (classLoader == null) {
			classLoader = Thread.currentThread().getContextClassLoader();
		}
		this.classLoader = classLoader;

		for (int i = 0; urls != null && i < urls.length; i++) {
			URL url = urls[i];
			if (url == null || isDirectory(url) || url.getProtocol().equals("jar")) {
				continue;
			}
			try {
				urls[i] = new URL("jar", "", -1, url.toString() + "!/");
			} catch (MalformedURLException e) {
			}
		}
		this.urls = (urls == null || urls.length == 0)? null : urls;
	}

	private static boolean isDirectory(URL url) {
		String file = url.getFile();
		return (file.length() > 0 && file.charAt(file.length() - 1) == '/');
	}

	// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	//
	//   Find String
	//
	// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

	/**
	 * Reads the contents of the URL as a {@link String}'s and returns it.
	 *
	 * @param uri
	 * @return a stringified content of a resource
	 * @throws java.io.IOException if a resource pointed out by the uri param could not be find
	 * @see ClassLoader#getResource(String)
	 */
	public String findString(String uri) throws IOException {
		String fullUri = path + uri;

		URL resource = getResource(fullUri);
		if (resource == null) {
			throw new IOException("Could not find a resource in : " + fullUri);
		}

		return readContents(resource);
	}

	/**
	 * Reads the contents of the found URLs as a list of {@link String}'s and returns them.
	 *
	 * @param uri
	 * @return a list of the content of each resource URL found
	 * @throws java.io.IOException if any of the found URLs are unable to be read.
	 */
	public List<String> findAllStrings(String uri) throws IOException {
		String fulluri = path + uri;

		List<String> strings = new ArrayList<String>();

		Enumeration<URL> resources = getResources(fulluri);
		while (resources.hasMoreElements()) {
			URL url = resources.nextElement();
			String string = readContents(url);
			strings.add(string);
		}
		return strings;
	}

	/**
	 * Reads the contents of the found URLs as a Strings and returns them.
	 * Individual URLs that cannot be read are skipped and added to the
	 * list of 'resourcesNotLoaded'
	 *
	 * @param uri
	 * @return a list of the content of each resource URL found
	 * @throws java.io.IOException if classLoader.getResources throws an exception
	 */
	public List<String> findAvailableStrings(String uri) throws IOException {
		String fulluri = path + uri;

		List<String> strings = new ArrayList<String>();

		Enumeration<URL> resources = getResources(fulluri);
		while (resources.hasMoreElements()) {
			URL url = resources.nextElement();
			try {
				String string = readContents(url);
				strings.add(string);
			} catch (IOException notAvailable) {
			}
		}
		return strings;
	}

	/**
	 * Reads the contents of all non-directory URLs immediately under the specified
	 * location and returns them in a map keyed by the file name.
	 * <p/>
	 * Any URLs that cannot be read will cause an exception to be thrown.
	 * <p/>
	 * Example classpath:
	 * <p/>
	 * META-INF/serializables/one
	 * META-INF/serializables/two
	 * META-INF/serializables/three
	 * META-INF/serializables/four/foo.txt
	 * <p/>
	 * ResourceFinder finder = new ResourceFinder("META-INF/");
	 * Map map = finder.mapAvailableStrings("serializables");
	 * map.contains("one");  // true
	 * map.contains("two");  // true
	 * map.contains("three");  // true
	 * map.contains("four");  // false
	 *
	 * @param uri
	 * @return a list of the content of each resource URL found
	 * @throws java.io.IOException if any of the urls cannot be read
	 */
	public Map<String, String> mapAllStrings(String uri) throws IOException {
		Map<String, String> strings = new HashMap<String, String>();
		Map<String, URL> resourcesMap = getResourcesMap( uri );
		for (Iterator iterator = resourcesMap.entrySet().iterator(); iterator.hasNext();) {
			Map.Entry entry = (Map.Entry) iterator.next();
			String name = (String) entry.getKey();
			URL url = (URL) entry.getValue();
			String value = readContents(url);
			strings.put(name, value);
		}
		return strings;
	}

	/**
	 * Reads the contents of all non-directory URLs immediately under the specified
	 * location and returns them in a map keyed by the file name.
	 * <p/>
	 * Individual URLs that cannot be read are skipped and added to the
	 * list of 'resourcesNotLoaded'
	 * <p/>
	 * Example classpath:
	 * <p/>
	 * META-INF/serializables/one
	 * META-INF/serializables/two      # not readable
	 * META-INF/serializables/three
	 * META-INF/serializables/four/foo.txt
	 * <p/>
	 * ResourceFinder finder = new ResourceFinder("META-INF/");
	 * Map map = finder.mapAvailableStrings("serializables");
	 * map.contains("one");  // true
	 * map.contains("two");  // false
	 * map.contains("three");  // true
	 * map.contains("four");  // false
	 *
	 * @param uri
	 * @return a list of the content of each resource URL found
	 * @throws java.io.IOException if classLoader.getResources throws an exception
	 */
	public Map<String, String> mapAvailableStrings(String uri) throws IOException {
		Map<String, String> strings = new HashMap<String, String>();
		Map<String, URL> resourcesMap = getResourcesMap(uri);
		for (Iterator iterator = resourcesMap.entrySet().iterator(); iterator.hasNext();) {
			Map.Entry entry = (Map.Entry) iterator.next();
			String name = (String) entry.getKey();
			URL url = (URL) entry.getValue();
			try {
				String value = readContents(url);
				strings.put(name, value);
			} catch (IOException notAvailable) {
			}
		}
		return strings;
	}

	// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	//
	//   Find Class
	//
	// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

	/**
	 * Executes {@link #findString(String)} assuming the contents URL found is the name of
	 * a class that should be loaded and returned.
	 *
	 * @param uri
	 * @return
	 * @throws java.io.IOException
	 * @throws ClassNotFoundException
	 */
	public Class findClass(String uri) throws IOException, ClassNotFoundException {
		String className = findString(uri);
		return (Class) classLoader.loadClass(className);
	}

	/**
	 * Executes findAllStrings assuming the strings are
	 * the names of a classes that should be loaded and returned.
	 * <p/>
	 * Any URL or class that cannot be loaded will cause an exception to be thrown.
	 *
	 * @param uri
	 * @return
	 * @throws java.io.IOException
	 * @throws ClassNotFoundException
	 */
	public List<Class> findAllClasses(String uri) throws IOException, ClassNotFoundException {
		List<Class> classes = new ArrayList<Class>();
		List<String> strings = findAllStrings(uri);
		for (String className : strings) {
			Class clazz = classLoader.loadClass(className);
			classes.add(clazz);
		}
		return classes;
	}

	/**
	 * Executes findAvailableStrings assuming the strings are
	 * the names of a classes that should be loaded and returned.
	 * <p/>
	 * Any class that cannot be loaded will be skipped and placed in the
	 * 'resourcesNotLoaded' collection.
	 *
	 * @param uri
	 * @return
	 * @throws java.io.IOException if classLoader.getResources throws an exception
	 */
	public List<Class> findAvailableClasses(String uri) throws IOException {
		List<Class> classes = new ArrayList<Class>();
		List<String> strings = findAvailableStrings(uri);
		for (String className : strings) {
			try {
				Class clazz = classLoader.loadClass(className);
				classes.add(clazz);
			} catch (Exception notAvailable) {
			}
		}
		return classes;
	}

	/**
	 * Executes mapAllStrings assuming the value of each entry in the
	 * map is the name of a class that should be loaded.
	 * <p/>
	 * Any class that cannot be loaded will be cause an exception to be thrown.
	 * <p/>
	 * Example classpath:
	 * <p/>
	 * META-INF/xmlparsers/xerces
	 * META-INF/xmlparsers/crimson
	 * <p/>
	 * ResourceFinder finder = new ResourceFinder("META-INF/");
	 * Map map = finder.mapAvailableStrings("xmlparsers");
	 * map.contains("xerces");  // true
	 * map.contains("crimson");  // true
	 * Class xercesClass = map.get("xerces");
	 * Class crimsonClass = map.get("crimson");
	 *
	 * @param uri
	 * @return
	 * @throws java.io.IOException
	 * @throws ClassNotFoundException
	 */
	public Map<String, Class> mapAllClasses(String uri) throws IOException, ClassNotFoundException {
		Map<String, Class> classes = new HashMap<String, Class>();
		Map<String, String> map = mapAllStrings(uri);
		for (Iterator iterator = map.entrySet().iterator(); iterator.hasNext();) {
			Map.Entry entry = (Map.Entry) iterator.next();
			String string = (String) entry.getKey();
			String className = (String) entry.getValue();
			Class clazz = classLoader.loadClass(className);
			classes.put(string, clazz);
		}
		return classes;
	}

	/**
	 * Executes mapAvailableStrings assuming the value of each entry in the
	 * map is the name of a class that should be loaded.
	 * <p/>
	 * Any class that cannot be loaded will be skipped and placed in the
	 * 'resourcesNotLoaded' collection.
	 * <p/>
	 * Example classpath:
	 * <p/>
	 * META-INF/xmlparsers/xerces
	 * META-INF/xmlparsers/crimson
	 * <p/>
	 * ResourceFinder finder = new ResourceFinder("META-INF/");
	 * Map map = finder.mapAvailableStrings("xmlparsers");
	 * map.contains("xerces");  // true
	 * map.contains("crimson");  // true
	 * Class xercesClass = map.get("xerces");
	 * Class crimsonClass = map.get("crimson");
	 *
	 * @param uri
	 * @return
	 * @throws java.io.IOException if classLoader.getResources throws an exception
	 */
	public Map<String, Class> mapAvailableClasses(String uri) throws IOException {
		Map<String, Class> classes = new HashMap<String, Class>();
		Map<String, String> map = mapAvailableStrings(uri);
		for (Iterator iterator = map.entrySet().iterator(); iterator.hasNext();) {
			Map.Entry entry = (Map.Entry) iterator.next();
			String string = (String) entry.getKey();
			String className = (String) entry.getValue();
			try {
				Class clazz = classLoader.loadClass(className);
				classes.put( string, clazz );
			} catch (Exception notAvailable) {
			}
		}
		return classes;
	}

	// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	//
	//   Find Implementation
	//
	// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

	/**
	 * Assumes the class specified points to a file in the classpath that contains
	 * the name of a class that implements or is a subclass of the specfied class.
	 * <p/>
	 * Any class that cannot be loaded will be cause an exception to be thrown.
	 * <p/>
	 * Example classpath:
	 * <p/>
	 * META-INF/java.io.InputStream    # contains the classname org.acme.AcmeInputStream
	 * META-INF/java.io.OutputStream
	 * <p/>
	 * ResourceFinder finder = new ResourceFinder("META-INF/");
	 * Class clazz = finder.findImplementation(java.io.InputStream.class);
	 * clazz.getName();  // returns "org.acme.AcmeInputStream"
	 *
	 * @param interfase a superclass or interface
	 * @return
	 * @throws java.io.IOException            if the URL cannot be read
	 * @throws ClassNotFoundException if the class found is not loadable
	 * @throws ClassCastException     if the class found is not assignable to the specified superclass or interface
	 */
	public Class findImplementation(Class interfase) throws IOException, ClassNotFoundException {
		String className = findString(interfase.getName());
		Class impl = classLoader.loadClass( className );
		if (!interfase.isAssignableFrom(impl)) {
			throw new ClassCastException("Class not of type: " + interfase.getName());
		}
		return impl;
	}

	/**
	 * Assumes the class specified points to a file in the classpath that contains
	 * the name of a class that implements or is a subclass of the specfied class.
	 * <p/>
	 * Any class that cannot be loaded or are not assignable to the specified class will be
	 * skipped and placed in the 'resourcesNotLoaded' collection.
	 * <p/>
	 * Example classpath:
	 * <p/>
	 * META-INF/java.io.InputStream    # contains the classname org.acme.AcmeInputStream
	 * META-INF/java.io.InputStream    # contains the classname org.widget.NeatoInputStream
	 * META-INF/java.io.InputStream    # contains the classname com.foo.BarInputStream
	 * <p/>
	 * ResourceFinder finder = new ResourceFinder("META-INF/");
	 * List classes = finder.findAllImplementations(java.io.InputStream.class);
	 * classes.contains("org.acme.AcmeInputStream");  // true
	 * classes.contains("org.widget.NeatoInputStream");  // true
	 * classes.contains("com.foo.BarInputStream");  // true
	 *
	 * @param interfase a superclass or interface
	 * @return
	 * @throws java.io.IOException if classLoader.getResources throws an exception
	 */
	public List<Class> findAvailableImplementations(Class interfase) throws IOException {
		List<Class> implementations = new ArrayList<Class>();
		List<String> strings = findAvailableStrings(interfase.getName());
		for (String className : strings) {
			try {
				Class impl = classLoader.loadClass(className);
				if (interfase.isAssignableFrom(impl)) {
					implementations.add(impl);
				}
			} catch (Exception notAvailable) {
			}
		}
		return implementations;
	}

	/**
	 * Assumes the class specified points to a directory in the classpath that holds files
	 * containing the name of a class that implements or is a subclass of the specfied class.
	 * <p/>
	 * Any class that cannot be loaded or are not assignable to the specified class will be
	 * skipped and placed in the 'resourcesNotLoaded' collection.
	 * <p/>
	 * Example classpath:
	 * <p/>
	 * META-INF/java.net.URLStreamHandler/jar
	 * META-INF/java.net.URLStreamHandler/file
	 * META-INF/java.net.URLStreamHandler/http
	 * <p/>
	 * ResourceFinder finder = new ResourceFinder("META-INF/");
	 * Map map = finder.mapAllImplementations(java.net.URLStreamHandler.class);
	 * Class jarUrlHandler = map.get("jar");
	 * Class fileUrlHandler = map.get("file");
	 * Class httpUrlHandler = map.get("http");
	 *
	 * @param interfase a superclass or interface
	 * @return
	 * @throws java.io.IOException if classLoader.getResources throws an exception
	 */
	public Map<String, Class> mapAvailableImplementations(Class interfase) throws IOException {
		Map<String, Class> implementations = new HashMap<String, Class>();
		Map<String, String> map = mapAvailableStrings( interfase.getName() );
		for (Iterator iterator = map.entrySet().iterator(); iterator.hasNext();) {
			Map.Entry entry = (Map.Entry) iterator.next();
			String string = (String) entry.getKey();
			String className = (String) entry.getValue();
			try {
				Class impl = classLoader.loadClass( className );
				if (interfase.isAssignableFrom(impl)) {
					implementations.put( string, impl );
				}
			} catch (Exception notAvailable) {
			}
		}
		return implementations;
	}

	// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	//
	//   Map Resources
	//
	// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

	public Map<String, URL> getResourcesMap(String uri) throws IOException {
		String basePath = path + uri;

		Map<String, URL> resources = new HashMap<String, URL>();
		if (!basePath.endsWith("/")) {
			basePath += "/";
		}
		Enumeration<URL> urls = getResources(basePath);

		while (urls.hasMoreElements()) {
			URL location = urls.nextElement();

			try {
				if (location.getProtocol().equals("jar")) {

					readJarEntries(location, basePath, resources);

				} else if (location.getProtocol().equals("file")) {

					readDirectoryEntries(location, resources);

				}
			} catch (Exception e) {
			}
		}

		return resources;
	}

	private static void readDirectoryEntries(URL location, Map<String, URL> resources) throws MalformedURLException {
		File dir = new File(URLDecoder.decode(location.getPath()));
		if (dir.isDirectory()) {
			File[] files = dir.listFiles();
			for (File file : files) {
				if (!file.isDirectory()) {
					String name = file.getName();
					URL url = file.toURI().toURL();
					resources.put(name, url);
				}
			}
		}
	}

	private static void readJarEntries(URL location, String basePath, Map<String, URL> resources) throws IOException {
		JarURLConnection conn = (JarURLConnection) location.openConnection();
		JarFile jarfile = null;
		jarfile = conn.getJarFile();

		Enumeration<JarEntry> entries = jarfile.entries();
		while (entries != null && entries.hasMoreElements()) {
			JarEntry entry = entries.nextElement();
			String name = entry.getName();

			if (entry.isDirectory() || !name.startsWith(basePath) || name.length() == basePath.length()) {
				continue;
			}

			name = name.substring(basePath.length());

			if (name.contains("/")) {
				continue;
			}

			URL resource = new URL(location, name);
			resources.put(name, resource);
		}
	}

	private Properties loadProperties(URL resource) throws IOException {
		InputStream in = resource.openStream();

		BufferedInputStream reader = null;
		try {
			reader = new BufferedInputStream(in);
			Properties properties = new Properties();
			properties.load(reader);

			return properties;
		} finally {
			try {
				in.close();
				reader.close();
			} catch (Exception e) {
			}
		}
	}

	private String readContents(URL resource) throws IOException {
		InputStream in = resource.openStream();
		BufferedInputStream reader = null;
		StringBuffer sb = new StringBuffer();

		try {
			reader = new BufferedInputStream(in);

			int b = reader.read();
			while (b != -1) {
				sb.append((char) b);
				b = reader.read();
			}

			return sb.toString().trim();
		} finally {
			try {
				in.close();
				reader.close();
			} catch (Exception e) {
			}
		}
	}

	private URL getResource(String fullUri) {
		if (urls == null){
			return classLoader.getResource(fullUri);
		}
		return findResource(fullUri, urls);
	}

	private Enumeration<URL> getResources(String fulluri) throws IOException {
		if (urls == null) {
			return classLoader.getResources(fulluri);
		}
		Vector<URL> resources = new Vector();
		for (URL url : urls) {
			URL resource = findResource(fulluri, url);
			if (resource != null){
				resources.add(resource);
			}
		}
		return resources.elements();
	}

	private URL findResource(String resourceName, URL... search) {
		for (int i = 0; i < search.length; i++) {
			URL currentUrl = search[i];
			if (currentUrl == null) {
				continue;
			}

			try {
				String protocol = currentUrl.getProtocol();
				if (protocol.equals("jar")) {
                    /*
                    * If the connection for currentUrl or resURL is
                    * used, getJarFile() will throw an exception if the
                    * entry doesn't exist.
                    */
					URL jarURL = ((JarURLConnection) currentUrl.openConnection()).getJarFileURL();
					JarFile jarFile;
					JarURLConnection juc;
					try {
						juc = (JarURLConnection) new URL("jar", "", jarURL.toExternalForm() + "!/").openConnection();
						jarFile = juc.getJarFile();
					} catch (IOException e) {
						// Don't look for this jar file again
						search[i] = null;
						throw e;
					}

					try {
						juc = (JarURLConnection) new URL("jar", "", jarURL.toExternalForm() + "!/").openConnection();
						jarFile = juc.getJarFile();
						String entryName;
						if (currentUrl.getFile().endsWith("!/")) {
							entryName = resourceName;
						} else {
							String file = currentUrl.getFile();
							int sepIdx = file.lastIndexOf("!/");
							if (sepIdx == -1) {
								// Invalid URL, don't look here again
								search[i] = null;
								continue;
							}
							sepIdx += 2;
							StringBuffer sb = new StringBuffer(file.length() - sepIdx + resourceName.length());
							sb.append(file.substring(sepIdx));
							sb.append(resourceName);
							entryName = sb.toString();
						}
						if (entryName.equals("META-INF/") && jarFile.getEntry("META-INF/MANIFEST.MF") != null) {
							return targetURL(currentUrl, "META-INF/MANIFEST.MF");
						}
						if (jarFile.getEntry(entryName) != null) {
							return targetURL(currentUrl, resourceName);
						}
					} finally {
						if (!juc.getUseCaches()) {
							try {
								jarFile.close();
							} catch (Exception e) {
							}
						}
					}

				} else if (protocol.equals("file")) {
					String baseFile = currentUrl.getFile();
					String host = currentUrl.getHost();
					int hostLength = 0;
					if (host != null) {
						hostLength = host.length();
					}
					StringBuffer buf = new StringBuffer(2 + hostLength + baseFile.length() + resourceName.length());

					if (hostLength > 0) {
						buf.append("//").append(host);
					}
					// baseFile always ends with '/'
					buf.append(baseFile);
					String fixedResName = resourceName;
					// Do not create a UNC path, i.e. \\host
					while (fixedResName.startsWith("/") || fixedResName.startsWith("\\")) {
						fixedResName = fixedResName.substring(1);
					}
					buf.append(fixedResName);
					String filename = buf.toString();
					File file = new File(filename);
					File file2 = new File(URLDecoder.decode(filename));

					if (file.exists() || file2.exists()) {
						return targetURL(currentUrl, fixedResName);
					}
				} else {
					URL resourceURL = targetURL(currentUrl, resourceName);
					URLConnection urlConnection = resourceURL.openConnection();

					try {
						urlConnection.getInputStream().close();
					} catch (SecurityException e) {
						return null;
					}
					// HTTP can return a stream on a non-existent file
					// So check for the return code;
					if (!resourceURL.getProtocol().equals("http")) {
						return resourceURL;
					}

					int code = ((HttpURLConnection) urlConnection).getResponseCode();
					if (code >= 200 && code < 300) {
						return resourceURL;
					}
				}
			} catch (MalformedURLException e) {
				// Keep iterating through the URL list
			} catch (IOException e) {
			} catch (SecurityException e) {
			}
		}
		return null;
	}

	private URL targetURL(URL base, String name) throws MalformedURLException {
		StringBuffer sb = new StringBuffer(base.getFile().length() + name.length());
		sb.append(base.getFile());
		sb.append(name);
		String file = sb.toString();
		return new URL(base.getProtocol(), base.getHost(), base.getPort(), file, null);
	}
}
