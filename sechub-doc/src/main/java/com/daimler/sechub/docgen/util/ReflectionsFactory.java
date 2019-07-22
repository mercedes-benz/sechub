// SPDX-License-Identifier: MIT
package com.daimler.sechub.docgen.util;

import java.net.URL;
import java.util.Collection;

import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

public class ReflectionsFactory {
	
	private ReflectionsFactory() {
		
	}
/* @formatter:off */
	public static Reflections create() {
		Collection<URL> forClassLoader = ClasspathHelper.forClassLoader();
		return new Reflections(
					new ConfigurationBuilder().
						filterInputsBy(new FilterBuilder().
									includePackage("com.daimler.sechub.*").
									/* exclude JAVA itself*/
									excludePackage("java.*").
									excludePackage("javax.*").
									excludePackage("org.*").
									/* exclude code2doc itself*/
									excludePackage("com.daimler.sechub.docgen")
									).
						setUrls(forClassLoader).
						setScanners(
									new SubTypesScanner(), 
									new TypeAnnotationsScanner(), 
									new MethodAnnotationsScanner(), 
									new FieldAnnotationsScanner()));
	}
	/* @formatter:on */
}
