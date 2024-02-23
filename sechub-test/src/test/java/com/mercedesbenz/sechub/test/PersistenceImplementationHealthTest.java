// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.test;

import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;

import com.mercedesbenz.sechub.docgen.reflections.Reflections;
import com.mercedesbenz.sechub.docgen.util.ReflectionsFactory;

import jakarta.persistence.Entity;
import jakarta.persistence.Version;

public class PersistenceImplementationHealthTest {
    private static Reflections reflections;
    private static Set<Class<?>> entityClasses;

    @BeforeClass
    public static void beforeClass() {
        reflections = ReflectionsFactory.create(); // reuse setup from sechub-doc
        entityClasses = reflections.getTypesAnnotatedWith(Entity.class);

    }

    @Test
    public void ensure_JPAEntities_haveOptimisticLockingImplemented() {
        StringBuilder sbCorrect = new StringBuilder();
        sbCorrect.append("\nCorrect implemented entities are:");
        List<String> problems = new ArrayList<>();
        for (Class<?> entityClass : entityClasses) {
            if (entityClass.isAnonymousClass()) {
                /*
                 * anonymous classes are ignored - cannot be real hibernate parts... so only
                 * test parts
                 */
                continue;
            }
            boolean foundVersionField = false;
            for (Field m : entityClass.getDeclaredFields()) {
                foundVersionField = m.isAnnotationPresent(Version.class);
                if (foundVersionField) {
                    break;
                }
            }
            if (!foundVersionField) {
                problems.add(entityClass.getName() + " has no field with @Version - so no optimistic locking");
            } else {
                sbCorrect.append("\n- entity:" + entityClass.getSimpleName() + " ok");
            }
        }
        System.out.println(sbCorrect);

        if (problems.isEmpty()) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Found following entities which does not have hashCode() and/or equals(..) implemented:\n");
        for (String r : problems) {
            sb.append(" - ");
            sb.append(r.toString());
            sb.append("\n");
        }
        sb.append(sbCorrect);
        fail(sb.toString());
    }

    @Test
    public void ensure_JPAEntities_haveEqualsAndHashCodeImplemented() {
        List<Result> problems = new ArrayList<>();
        for (Class<?> entityClass : entityClasses) {
            if (entityClass.isAnonymousClass()) {
                /*
                 * anonymous classes are ignored - cannot be real hibernate parts... so only
                 * test parts
                 */
                continue;
            }
            Result r = new Result();
            r.clazz = entityClass;
            for (Method m : entityClass.getDeclaredMethods()) {
                if (r.allFound()) {
                    break;
                }
                if (m.getName().equals("equals") && m.getReturnType().isPrimitive() && m.getParameterCount() == 1
                        && m.getParameterTypes()[0].equals(Object.class)) {
                    r.equalsOverriden = true;
                } else if (m.getName().equals("hashCode") && m.getReturnType().isPrimitive() && m.getParameterCount() == 0) {
                    r.hashCodeOverridden = true;
                }
            }
            if (!r.allFound()) {
                problems.add(r);
            }
        }
        if (problems.isEmpty()) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Found following entities which does not have hashCode() and/or equals(..) implemented:\n");
        for (Result r : problems) {
            sb.append(" - ");
            sb.append(r.toString());
            sb.append("\n");
        }
        fail(sb.toString());
    }

    private class Result {
        public Class<?> clazz;
        private boolean equalsOverriden;
        private boolean hashCodeOverridden;

        public boolean allFound() {
            return equalsOverriden && hashCodeOverridden;
        }

        @Override
        public String toString() {
            return clazz.getName() + ", equals overriden:" + equalsOverriden + ", hashCode overriden:" + hashCodeOverridden;
        }
    }

}
