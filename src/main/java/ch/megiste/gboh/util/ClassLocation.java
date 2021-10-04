package ch.megiste.gboh.util;

import java.net.URL;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;



/**
 * Describes the location of a class, accessable through the given classloader. The url points to
 * the real class file location.
 */
public class ClassLocation {

	private String className;

	private URL url;

	private ClassLoader classLoader;

	public ClassLocation(ClassLoader classLoader, String className, URL url) {
		Preconditions.checkNotNull(classLoader);
		Preconditions.checkArgument(!Strings.isNullOrEmpty(className));
		Preconditions.checkNotNull(url);
		this.className = className;
		this.url = url;
		this.classLoader = classLoader;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public URL getUrl() {
		return url;
	}

	public void setUrl(URL url) {
		this.url = url;
	}

	public ClassLoader getClassLoader() {
		return classLoader;
	}

	public void setClassLoader(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getClassName() == null) ? 0 : getClassName().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if ((obj == null) || (obj.getClass() != this.getClass())) {
			return false;
		}
		final ClassLocation other = (ClassLocation) obj;
		if (getClassName() == null && other.getClassName() != null) {
			return false;
		}
		return getClassName().equals(other.getClassName());
	}
}