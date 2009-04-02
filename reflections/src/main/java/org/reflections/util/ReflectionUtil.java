/*
 * User: ophir
 * Date: Mar 29, 2009
 * Time: 12:38:42 AM
 */
package org.reflections.util;

import org.reflections.ReflectionsException;

public class ReflectionUtil {

	public static Class<?> resolveClass(String className) {
		try {
			return Class.forName(className);
		} catch (ClassNotFoundException e) {
			throw new ReflectionsException("Can't reslve class name: " + className, e);
		}
	}
}
