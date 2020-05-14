// SPDX-License-Identifier: MIT
package com.daimler.sechub.docgen.reflections;

/**
 * This class is for testing only - because sechub-doc.jar is never part of delivery it doesn't matter.
 * Why inside /src/main/java and not inside /src/test/java ? Because our Reflections class will
 * only lookup for java source code inside main/java and we do not want to have a configuration here
 * which changes for testing.  
 * @author Albert Tregnaghi
 *
 */
@ReflectionsExampleUsageAnnotation("class2")
public class ReflectionsExampleClass2 implements ReflectionsExampleInterface{

    @ReflectionsExampleUsageAnnotation("field1")
    private String field1;
    
    @ReflectionsExampleUsageAnnotation("field2")
    private String field2;
    
    @ReflectionsExampleUsageAnnotation("method1")
    public void method1() {
        System.out.println("avoid empty method");
    }
    
    @ReflectionsExampleUsageAnnotation("method2")
    public void method2() {
        System.out.println("avoid empty method");
    }
    
}
