// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.docgen.reflections;

import static org.junit.Assert.*;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Before;
import org.junit.Test;

import com.mercedesbenz.sechub.docgen.util.ReflectionsFactory;

public class ReflectionsTest {

    private Reflections reflectionsToTest;

    @Before
    public void before() {
        reflectionsToTest = ReflectionsFactory.create();
    }

    @Test
    public void extractJavaFileName_class_TestMe_in_subpackage_com_daimler_sechub_reflections() {
        /* prepare */
        File file = new File(".");
        File sourceFolder = new File(file, "subproject1/src/main/java");
        File classFile = new File(sourceFolder, "com/daimler/sechub/reflections/TestMe.java");

        /* execute */
        String result = reflectionsToTest.extractJavaFileName(sourceFolder, classFile);

        /* test */
        assertEquals("com.mercedesbenz.sechub.reflections.TestMe", result);
    }

    @Test
    public void extractJavaFileName_class_TestMe_in_default_package() {
        /* prepare */
        File file = new File(".");
        File sourceFolder = new File(file, "subproject1/src/main/java");
        File classFile = new File(sourceFolder, "TestMe.java");

        /* execute */
        String result = reflectionsToTest.extractJavaFileName(sourceFolder, classFile);

        /* test */
        assertEquals("TestMe", result);
    }

    @Test
    public void sanity_check() {
        assertTrue(reflectionsToTest.isInspecting(ReflectionsExampleInterface.class));
        assertTrue(reflectionsToTest.isInspecting(ReflectionsExampleUsageAnnotation.class));
        assertTrue(reflectionsToTest.isInspecting(ReflectionsExampleClass1.class));
        assertTrue(reflectionsToTest.isInspecting(ReflectionsExampleClass2.class));

        /*
         * next lines should never fail, because SecHub has currently 667 classes inside
         * inspected source folder and is growing. So this just checks that not only the
         * sechub-doc folder is scanned...
         */
        int amountOfInspectedClasses = reflectionsToTest.getAmountOfInspectedClasses();
        assertTrue("SecHub must have more than 400 classes", amountOfInspectedClasses > 400);
    }

    @Test
    public void getFieldsAnnotatedWith() {

        /* execute */
        Set<Field> found = reflectionsToTest.getFieldsAnnotatedWith(ReflectionsExampleUsageAnnotation.class);

        /* test */
        assertNotNull(found);
        assertEquals(4, found.size());

        Set<String> sortedSet = new TreeSet<>();
        for (Field field : found) {
            sortedSet.add(field.toString());
        }

        assertFound("private java.lang.String com.mercedesbenz.sechub.docgen.reflections.ReflectionsExampleClass1.field1", sortedSet);
        assertFound("private java.lang.String com.mercedesbenz.sechub.docgen.reflections.ReflectionsExampleClass1.field2", sortedSet);
        assertFound("private java.lang.String com.mercedesbenz.sechub.docgen.reflections.ReflectionsExampleClass2.field1", sortedSet);
        assertFound("private java.lang.String com.mercedesbenz.sechub.docgen.reflections.ReflectionsExampleClass2.field2", sortedSet);

    }

    @Test
    public void getMethodsAnnotatedWith() {

        /* execute */
        Set<Method> found = reflectionsToTest.getMethodsAnnotatedWith(ReflectionsExampleUsageAnnotation.class);

        /* test */
        assertNotNull(found);
        assertEquals(4, found.size());

        Set<String> sortedSet = new TreeSet<>();
        for (Method method : found) {
            sortedSet.add(method.toString());
        }

        assertFound("public void com.mercedesbenz.sechub.docgen.reflections.ReflectionsExampleClass1.method1()", sortedSet);
        assertFound("public void com.mercedesbenz.sechub.docgen.reflections.ReflectionsExampleClass1.method2()", sortedSet);
        assertFound("public void com.mercedesbenz.sechub.docgen.reflections.ReflectionsExampleClass2.method1()", sortedSet);
        assertFound("public void com.mercedesbenz.sechub.docgen.reflections.ReflectionsExampleClass2.method2()", sortedSet);

    }

    @Test
    public void getTypesAnnotatedWith() {

        /* execute */
        Set<Class<?>> found = reflectionsToTest.getTypesAnnotatedWith(ReflectionsExampleUsageAnnotation.class);

        /* test */
        assertNotNull(found);
        assertEquals(2, found.size());

        Set<String> sortedSet = new TreeSet<>();
        for (Class<?> clazz : found) {
            sortedSet.add(clazz.toString());
        }

        assertFound("class com.mercedesbenz.sechub.docgen.reflections.ReflectionsExampleClass1", sortedSet);
        assertFound("class com.mercedesbenz.sechub.docgen.reflections.ReflectionsExampleClass2", sortedSet);

    }

    @Test
    public void getSubTypesOf_interface() {

        /* execute */
        Set<Class<? extends ReflectionsExampleInterface>> found = reflectionsToTest.getSubTypesOf(ReflectionsExampleInterface.class);

        /* test */
        assertNotNull(found);
        assertEquals(2, found.size());

        assertTrue(found.contains(ReflectionsExampleClass1.class));
        assertTrue(found.contains(ReflectionsExampleClass2.class));
    }

    private void assertFound(String data, Set<String> set) {
        if (!set.contains(data)) {
            StringBuilder sb = new StringBuilder();
            sb.append("Did not find: ").append(data).append(" but:\n");
            for (String str : set) {
                sb.append(" - ").append(str).append("\n");
            }
            System.out.println(sb.toString());
            fail(sb.toString());
        }
    }

}
