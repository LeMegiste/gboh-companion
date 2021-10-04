package ch.megiste.gboh.util;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Logger;

import com.google.common.base.Preconditions;



/**
 * Util to locate classes from the classpath.
 */
public class ClassLocator {
	public static final String CLASS_EXTENSION = ".class";
	public static final char POINT = '.';
	public static final char SLASH = '/';
	private static final Logger LOGGER = Logger.getLogger(ClassLocator.class.toString());
	/**
	 * Are the package names the found classes' package must start with.
	 */
	protected final String[] packageNames;

	/**
	 * Is the used classloader to lookup classes.
	 */
	protected final ClassLoader classLoader;

	private Boolean rebelDetected;

	//private static final Log LOGGER = LogFactory.getLog(ClassLocator.class);

	/**
	 * Locates all classes that are in one of the given packages (inclusive sub-packages).
	 *
	 * @param packageNames
	 *            Are the package names the found classes' package must start with.
	 * @throws ClassNotFoundException
	 *             If the context classloader of the current thread could not be fetched.
	 */
	public ClassLocator(String... packageNames) throws ClassNotFoundException {
		this(Thread.currentThread().getContextClassLoader(), packageNames);
	}

	/**
	 * Locates all classes that are in one of the given packages (inclusive sub-packages).
	 *
	 * @param classLoader
	 *            Is the used classloader to lookup classes.
	 * @param packageNames
	 *            Are the package names the found classes' package must start with.
	 */
	public ClassLocator(ClassLoader classLoader, String... packageNames) {
		Preconditions.checkNotNull(classLoader);
		Preconditions.checkNotNull(packageNames);
		this.classLoader = classLoader;
		this.packageNames = packageNames;
	}

	/**
	 * Returns all located classes where the found classes' package starts with one of the given package names.
	 *
	 * @return Returns the found class locations.
	 * @throws ClassNotFoundException
	 *             If no class could be found for one of the given packages.
	 * @throws IOException
	 *             If there was a general IO problem.
	 */
	public Set<ClassLocation> getAllClassLocations() throws ClassNotFoundException, IOException {
		Set<ClassLocation> classLocations = new HashSet<>();

		for (String packageName : packageNames) {
			String path = packageName.replace(POINT, SLASH);
			Enumeration<URL> resources = classLoader.getResources(path);
			if (null == resources || !resources.hasMoreElements()) {
				throw new ClassNotFoundException("No resource for " + path);
			}

			while (resources.hasMoreElements()) {
				URL resource = resources.nextElement();
				if (resource.getProtocol().equalsIgnoreCase("FILE")) {
					loadDirectory(packageName, resource, classLocations);
				} else if (resource.getProtocol().equalsIgnoreCase("JAR")) {
					loadJar(packageName, resource, classLocations);
				} else {
					throw new ClassNotFoundException("Unknown protocol on class resource: " + resource.toExternalForm());
				}
			}
		}
		return classLocations;
	}

	/**
	 * Returns the located class from a class reference.
	 *
	 * @param classReference the class name reference
	 * @return Returns the found class locations.
	 * @throws ClassNotFoundException If no class could be found for one of the given packages.
	 */
	public ClassLocation getClassLocation(String classReference) throws ClassNotFoundException {
		String path = classReference.replace(POINT, SLASH) + CLASS_EXTENSION;
		URL resource = classLoader.getResource(path);
		if (null == resource) {
			throw new ClassNotFoundException("No resource for " + path);
		}
		return new ClassLocation(classLoader, classReference, resource);
	}

	/**
	 * Tries to fill the given class location list with classes from the given package that are saved in a jar file.
	 *
	 * @param packageName
	 *            Is the name of the package the class's package has to start with.
	 * @param resource
	 *            Is the real location of the given package name.
	 * @param classLocations
	 *            Are the already found class locations.
	 * @throws IOException
	 *             If there was a general IO problem.
	 */
	private void loadJar(String packageName, URL resource, Set<ClassLocation> classLocations) throws IOException {
		JarURLConnection conn = (JarURLConnection) resource.openConnection();
		try (JarFile jarFile = conn.getJarFile()) {
			Enumeration<JarEntry> entries = jarFile.entries();
			String packagePath = packageName.replace(POINT, SLASH);

			while (entries.hasMoreElements()) {
				JarEntry entry = entries.nextElement();
				if ((entry.getName().startsWith(packagePath)
						|| entry.getName().startsWith("WEB-INF/classes/" + packagePath))
						&& entry.getName().endsWith(CLASS_EXTENSION)) {
					URL url = new URL("jar:" + new URL("file", null, jarFile.getName()).toExternalForm() + "!/"
							+ entry.getName());

					final String className = getClassName(entry);
					ClassLocation classLocation = new ClassLocation(classLoader, className, url);
					addClassLocation(classLocation, classLocations);
				}
			}
		}
	}

	private String getClassName(final JarEntry entry) {
		String className = entry.getName();
		if (className.startsWith("/")) {
			className = className.substring(1);
		}
		className = className.replace(SLASH, POINT);

		className = className.substring(0, className.length() - CLASS_EXTENSION.length());
		return className;
	}

	private void loadDirectory(String packageName, URL resource, Set<ClassLocation> classLocations) throws IOException {
		loadDirectory(packageName, resource.getFile(), classLocations);

	}

	private void loadDirectory(String packageName, String fullPath, Set<ClassLocation> classLocations)
			throws IOException {
		File directory = new File(fullPath.replace("%20", " ")); // NOSONAR

		if (!directory.isDirectory()) {
			throw new IOException("Invalid directory " + directory.getAbsolutePath());
		}

		File[] files = directory.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				loadDirectory(packageName + POINT + file.getName(), file.getAbsolutePath(), classLocations);
			} else if (file.getName().endsWith(CLASS_EXTENSION)) {
				String simpleName = file.getName();
				simpleName = simpleName.substring(0, simpleName.length() - CLASS_EXTENSION.length());
				String className = String.format("%s.%s", packageName, simpleName);
				ClassLocation location =
						new ClassLocation(classLoader, className, new URL("file", null, file.getAbsolutePath()));
				addClassLocation(location, classLocations);
			}
		}
	}

	/**
	 * Adds the given class location to the given class location list if this list does not already contains it.
	 *
	 * @param classLocation
	 *            Is the class location to add.
	 * @param classLocations
	 *            Are the already found class locations.
	 * @throws IOException
	 *             If the given class location is already in the given list.
	 */
	private void addClassLocation(ClassLocation classLocation, Set<ClassLocation> classLocations) throws IOException {
		if (classLocations.contains(classLocation)) {

			if (isUnderJrebel()) {
				LOGGER.warning("Duplicate location found for: " + classLocation.getClassName()
						+ "; this is expected if it concerns entities as you are using JRebel!");
			} else {
				throw new IOException("Duplicate location found for: " + classLocation.getClassName());
			}
		}
		classLocations.add(classLocation);
	}

	private boolean isUnderJrebel() {
		if (rebelDetected == null) {
			final String javaOpts = System.getenv("JAVA_OPTS");
			if (javaOpts != null && javaOpts.contains("jrebel.jar")) {
				rebelDetected = Boolean.TRUE;
			} else {
				rebelDetected = Boolean.FALSE;
			}
		}
		return rebelDetected.booleanValue();
	}
}