// SPDX-License-Identifier: MIT
package com.daimler.sechub.test;

import static org.junit.Assert.*;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Objects;
import java.util.Optional;

import org.mockito.Mockito;

/**
 * A test utility class to test POJOs (plain old java objects) in a convenient
 * way - e.g. automated setter and getter testing
 * 
 * @author Albert Tregnaghi
 *
 */
public class PojoTester {
    private PojoTester() {

    }

    public static <T> void testEqualsAndHashCodeCorrectImplemented(T objectA, T objectBequalToA, T objectCnotEqualToAOrB) {
        testBothAreEqualAndHaveSameHashCode(objectA, objectBequalToA);

        testBothAreNOTEqual(objectA, objectCnotEqualToAOrB);

    }

    public static <T> void testBothAreNOTEqual(T objectA, T objectCnotEqualToAOrB) {
        assertNotNull(objectCnotEqualToAOrB);
        assertFalse("objectA is equals to objectC but must NOT be -check implementation!", objectA.equals(objectCnotEqualToAOrB));
        assertFalse("objectB is equals to objectC but must NOT be -check implementation!", objectCnotEqualToAOrB.equals(objectA));
    }

    public static <T> void testBothAreEqualAndHaveSameHashCode(T objectA, T objectBequalToA) {
        assertNotNull(objectA);
        assertNotNull(objectBequalToA);

        assertTrue("objectA is not equals not objectB but must be -check implementation!", objectA.equals(objectBequalToA));
        assertTrue("objectB is not equals not objectA but must be -check implementation!", objectBequalToA.equals(objectA));

        /* hash code check */
        assertEquals("objectA has not same hashcode as objectB but must be -check implementation!", objectA.hashCode(), objectBequalToA.hashCode());
    }

    /**
     * Every field having a getter and a setter will be automatically tested, that
     * the written value (setter) will be returned on read (getter)
     * 
     * @param objectToTest
     */
    public static void testSetterAndGetter(Object objectToTest) {
        assertNotNull("Cannot test null objects!", objectToTest);

        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(objectToTest.getClass());
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
            for (PropertyDescriptor descriptor : propertyDescriptors) {
                testPropertyDescriptor(descriptor, objectToTest);
            }

        } catch (Exception e) {
            e.printStackTrace();
            fail("Bean introspection failed - internal error in framework. See output for details - message was:" + e.getMessage());
        }
    }

    private static void testPropertyDescriptor(PropertyDescriptor descriptor, Object objectToTest)
            throws ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        /* check preconditions: setter and getter found for property */
        Method writeMethod = descriptor.getWriteMethod();
        if (writeMethod == null) {
            return;
        }
        Method readmethod = descriptor.getReadMethod();
        if (readmethod == null) {
            return;
        }
        /* fetch result type of getter */
        Object mockedArgument = createMockArgumentForProperty(descriptor, readmethod);

        /* write */
        try {
            writeMethod.invoke(objectToTest, mockedArgument);
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Was not able to set mockedArgument:" + mockedArgument + "\ninto:" + objectToTest + "\nby writeMethod:" + writeMethod);
        }
        /* read again */
        Object result = readmethod.invoke(objectToTest);

        /* test */
        if (!Objects.equals(result, mockedArgument)) {
            fail("The getter/setter implementation of " + objectToTest.getClass() + " failed!\nProperty:" + descriptor.getName() + "\nExpected:"
                    + mockedArgument + "\nGot:" + result);
        }

    }

    private static Object createMockArgumentForProperty(PropertyDescriptor descriptor, Method readmethod) throws ClassNotFoundException {
        /* property has getter and setter, so inspect type... */
        Class<?> propertyType = descriptor.getPropertyType();
        boolean optional = propertyType.equals(Optional.class);
        if (optional) {
            /* ugly but necessary, to handle optional... */
            Type generic = readmethod.getGenericReturnType();
            ParameterizedType parameterizedType = (ParameterizedType) generic;
            Type[] typeArgumentObjects = parameterizedType.getActualTypeArguments();
            String typeName = typeArgumentObjects[0].getTypeName();
            propertyType = Class.forName(typeName);
        }
        Object mockedArgument = null;
        if (propertyType.isPrimitive()) {
            if (propertyType.equals(int.class)) {
                mockedArgument = 42;
            } else if (propertyType.equals(double.class)) {
                mockedArgument = (float) 42;
            } else if (propertyType.equals(byte.class)) {
                mockedArgument = (byte) 42;
            }
        } else if (propertyType.isEnum()) {
            Object[] enumObjects = propertyType.getEnumConstants();
            mockedArgument = enumObjects[0];
        } else {
            try {
                Constructor<?> constructor = propertyType.getConstructor();
                mockedArgument = constructor.newInstance();
            } catch (Exception e) {
                /* no default constructor available - fall back to Mockito ... */
                try {
                    mockedArgument = Mockito.mock(propertyType);
                } catch (Exception e2) {
                    throw new IllegalStateException("No default constructor available for:" + propertyType + "\nat method:" + readmethod
                            + "\nDid try to create with mockito, but failed", e);
                }
            }
        }
        if (optional) {
            mockedArgument = Optional.of(mockedArgument);
        }
        return mockedArgument;
    }

    /**
     * A pojo changer is able to change a given POJO (plain java object)
     * 
     * @author Albert Tregnaghi
     *
     * @param <T>
     */
    public interface PojoChanger<T> {
        void changePojo(T pojo);
    }

    /**
     * Changes given POJO by given POJO changer. Use this method when you are
     * writing tests and you need a fluent API but the POJO does not provide this.
     * Look at the next example or look into call hierarchy for more details.<br>
     * Example:
     * 
     * <pre>
     *    testBothAreNOTEqual(createExample(), change(createExample(), (codeFlow) -> codeFlow.setMessage(new Message("other"))));
     * </pre>
     * 
     * @param <T>
     * @param pojo
     * @param changer
     * @return
     */
    public static <T> T change(T pojo, PojoChanger<T> changer) {
        changer.changePojo(pojo);
        return pojo;

    }

}
