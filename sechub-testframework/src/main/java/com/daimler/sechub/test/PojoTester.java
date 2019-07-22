// SPDX-License-Identifier: MIT
package com.daimler.sechub.test;

import static org.junit.Assert.*;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Optional;

import org.mockito.Mockito;

public class PojoTester {
	private PojoTester() {

	}

	public static <T> void testEqualsAndHashCodeCorrectImplemented(T objectA, T objectBequalToA,
			T objectCnotEqualToAOrB) {
		assertNotNull(objectA);
		assertNotNull(objectBequalToA);
		assertNotNull(objectCnotEqualToAOrB);

		assertTrue("objectA is not equals not objectB but must be -check implementation!",
				objectA.equals(objectBequalToA));
		assertTrue("objectB is not equals not objectA but must be -check implementation!",
				objectBequalToA.equals(objectA));

		assertEquals("objectA has not same hashcode as objectB but must be -check implementation!", objectA.hashCode(),
				objectBequalToA.hashCode());

		assertFalse("objectA is equals to objectC but must NOT be -check implementation!",
				objectA.equals(objectCnotEqualToAOrB));
		assertFalse("objectB is equals to objectC but must NOT be -check implementation!",
				objectCnotEqualToAOrB.equals(objectA));

	}

	public static void testSetterAndGetter(Object objectToTest) {
		assertNotNull("Cannot test null objects!", objectToTest);

		try {
			BeanInfo beanInfo = Introspector.getBeanInfo(objectToTest.getClass());
			PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
			for (PropertyDescriptor descriptor : propertyDescriptors) {
				Method writeMethod = descriptor.getWriteMethod();
				if (writeMethod == null) {
					continue;
				}
				Method readmethod = descriptor.getReadMethod();
				if (readmethod == null) {
					continue;
				}
				Class<?> type = descriptor.getPropertyType();
				boolean optional= type.equals(Optional.class);
				if (optional) {
					/* ugly but necessary, to handle optinonal...*/
					Type generic = readmethod.getGenericReturnType();
					ParameterizedType pm = (ParameterizedType) generic;
					Type[] args = pm.getActualTypeArguments();
					String typeName = args[0].getTypeName();
					type = Class.forName(typeName);
				}
				Object mockedArgument = null;
				try {
					Constructor<?> constructor = type.getConstructor();
					mockedArgument = constructor.newInstance();
				} catch (Exception e) {
					/* no default constructor available - fall back to Mockito ... */
					try {
						mockedArgument = Mockito.mock(type);
					}catch(Exception e2) {
						throw new IllegalStateException("No default constructor available for:"+type+"\nat method:"+readmethod+"\nDid try to creeate with mockito, but failed",e);
					}
				}
				if (optional) {
					mockedArgument=Optional.of(mockedArgument);
				}
				writeMethod.invoke(objectToTest, mockedArgument);

				Object result = readmethod.invoke(objectToTest);

				if (result != mockedArgument) {
					fail("The getter/setter implementation of " + objectToTest.getClass() + " failed on property:"
							+ descriptor.getName());
				}

			}

		} catch (Exception e) {
			e.printStackTrace();
			fail("Bean introspection failed - internal error in framework. See output for details - message was:"
					+ e.getMessage());
		}
	}

}
