// SPDX-License-Identifier: MIT
package com.daimler.sechub.docgen.reflections;

import java.io.File;
import java.io.FileFilter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daimler.sechub.docgen.GeneratorConstants;
import com.daimler.sechub.sharedkernel.usecases.UseCaseRestDoc;

/**
 * A very simple replacement for `org.reflections` library. But fullfils
 * requirements for sechub documentation generation... and works with JDK11
 * 
 * @author Albert Tregnaghi
 *
 */
public class Reflections {
    private static final Logger LOG = LoggerFactory.getLogger(Reflections.class);
    private static Object MONITOR = new Object();

    private boolean scanDone;
    private File sechHubDoc;
    private List<File> sourceDirectories = new ArrayList<>();

    private Map<Class<? extends Annotation>, Set<Class<?>>> annotationClassToType = new HashMap<>();
    private Map<Class<? extends Annotation>, Set<Method>> annotationClassToMethod = new HashMap<>();
    private Map<Class<? extends Annotation>, Set<Field>> annotationClassToField = new HashMap<>();
    @SuppressWarnings("rawtypes")
    private Map<Class<?>, Set> classToSubTypes = new HashMap<>();

    @SuppressWarnings("rawtypes")
    private Set<Class> classesToInspect = new LinkedHashSet<>();

    public Reflections() {

    }

    public Set<Class<?>> getTypesAnnotatedWith(Class<? extends Annotation> annotation) {
        ensureScan();
        synchronized (MONITOR) {
            Set<Class<?>> result = annotationClassToType.get(annotation);
            if (result != null) {
                return result;
            }
            /* not tried before... */
            Set<Class<?>> newResult = new LinkedHashSet<>();

            visitEveryClass(new Visitor() {

                @Override
                public void visit(Class<?> clazz) {
                    if (clazz.getAnnotation(annotation) != null) {
                        newResult.add(clazz);
                    }
                }
            });

            annotationClassToType.put(annotation, newResult);
            return newResult;
        }
    }

    private void visitEveryClass(Visitor visitor) {
        for (@SuppressWarnings("rawtypes") Class clazz : classesToInspect) {
            visitor.visit(clazz);
        }
    }

    private interface Visitor {

        void visit(Class<?> clazz);

    }

    public Set<Method> getMethodsAnnotatedWith(Class<? extends Annotation> annotation) {
        ensureScan();
        synchronized (MONITOR) {
            Set<Method> result = annotationClassToMethod.get(annotation);
            if (result != null) {
                return result;
            }

            /* not tried before... */
            Set<Method> newResult = new LinkedHashSet<>();
            visitEveryClass(new Visitor() {

                @Override
                public void visit(Class<?> clazz) {
                    for (Method method : clazz.getDeclaredMethods()) {
                        if (method.getDeclaredAnnotation(annotation) != null) {
                            if (annotation.equals(UseCaseRestDoc.class)){
                                if (GeneratorConstants.DEBUG) {
                                    LOG.info("UsecaseRestDoc found:{}",clazz);
                                }
                            }
                            newResult.add(method);
                        }
                    }
                }
            });
            annotationClassToMethod.put(annotation, newResult);
            return newResult;
        }
    }

    public Set<Field> getFieldsAnnotatedWith(Class<? extends Annotation> annotation) {
        ensureScan();
        synchronized (MONITOR) {
            Set<Field> result = annotationClassToField.get(annotation);
            if (result != null) {
                return result;
            }
            /* not tried before... */
            Set<Field> newResult = new LinkedHashSet<>();
            visitEveryClass(new Visitor() {

                @Override
                public void visit(Class<?> clazz) {
                    for (Field field : clazz.getDeclaredFields()) {
                        if (field.getAnnotation(annotation) != null) {
                            newResult.add(field);
                        }
                    }
                }
            });
            annotationClassToField.put(annotation, newResult);
            return newResult;
        }
    }

    public <T> Set<Class<? extends T>> getSubTypesOf(Class<T> clazz) {
        ensureScan();
        synchronized (MONITOR) {
            @SuppressWarnings("rawtypes") // necessary for generic conversion. but safe here
            Set x = classToSubTypes.get(clazz);
            @SuppressWarnings("unchecked")
            Set<Class<? extends T>> result = x;
            if (result != null) {
                return result;
            }
            /* not tried before... */
            Set<Class<? extends T>> newresult = new LinkedHashSet<>();
            visitEveryClass(new Visitor() {

                @SuppressWarnings("unchecked")
                @Override
                public void visit(Class<?> clazzToInspect) {
                    if (clazzToInspect==clazz) {
                        return;
                    }
                    if (clazz.isAssignableFrom(clazzToInspect)) {
                        newresult.add((Class<? extends T>) clazzToInspect);
                    }

                }
            });
            classToSubTypes.put(clazz, newresult);
            return newresult;
        }
    }

    void ensureScan() {
        synchronized (MONITOR) {
            if (!scanDone) {
                scan();
                scanDone = true;
            }
        }
    }

    private void scan() {
        fetchGradleSoureDirectories();
        fetchJavaClassNamesToInspect();
    }

    void fetchJavaClassNamesToInspect() {
        JavaContentFilter javaSourceFileFilter = new JavaContentFilter();
        for (File sourceDirectory : sourceDirectories) {
            fetchJavaClassNamesToInspect(sourceDirectory, sourceDirectory, javaSourceFileFilter);
        }

    }

    void fetchJavaClassNamesToInspect(File sourceDirectory, File file, JavaContentFilter javaSourceFileFilter) {
        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                fetchJavaClassNamesToInspect(sourceDirectory, child, javaSourceFileFilter);
            }
        } else if (file.isFile()) {
            String className = extractJavaFileName(sourceDirectory, file);
            if (isIgnoredClassName(className)) {
                return;
            }

            Class<?> clazz;
            try {
                clazz = Class.forName(className);
                classesToInspect.add(clazz);
            } catch (ClassNotFoundException e) {
                LOG.warn("Ignored class:{}", className);
            }
        } else {
            LOG.error("Was not able to handle file:{}", file);
        }

    }

    private boolean isIgnoredClassName(String className) {
        if (className.startsWith("com.daimler.sechub.developertools")) {
            return true;
        }
        if (className.startsWith("com.daimler.sechub.integrationtest")) {
            return true;
        }
        if (className.equals("com.daimler.sechub.domain.scan.config.DeveloperToolsScanConfigService")) {
            return true;
        }
        return false;
    }

    String extractJavaFileName(File sourceDirectory, File file) {
        String diff = file.getAbsolutePath().substring(sourceDirectory.getAbsolutePath().length());
        String className = FilenameUtils.separatorsToUnix(diff).replace('/', '.');
        if (className.startsWith(".")) {
            className = className.substring(1);
        }
        if (className.endsWith(".java")) {
            className = className.substring(0, className.length() - ".java".length());
        }
        return className;
    }

    private void fetchGradleSoureDirectories() {
        File file = new File("sechub-doc");
        File rootFolder = null;
        if (file.exists()) {
            rootFolder = file.getParentFile();
        } else {
            rootFolder = new File("./..");
        }
        sechHubDoc = new File(rootFolder, "sechub-doc");
        if (!sechHubDoc.exists()) {
            throw new IllegalStateException(
                    "docgen corrupt - did not found sechub-doc folder, cannot determine root folder, i am at :" + new File(".").getAbsolutePath());
        }
        File[] subDirs = rootFolder.listFiles(new FileFilter() {

            @Override
            public boolean accept(File file) {
                return file.isDirectory();
            }
        });
        for (File subDir : subDirs) {
            File deepSubDir = new File(subDir, "src/main/java");
            if (!deepSubDir.exists()) {
                continue;
            }
            LOG.info("Add source directory:{}", deepSubDir);
            this.sourceDirectories.add(deepSubDir);
        }
        this.sourceDirectories.add(new File(sechHubDoc,"src/test/java")); // we need this to be able to execute restdoc gen tests + ReflectionsTest.java
    }

    private class JavaContentFilter implements FileFilter {

        public boolean accept(File file) {
            if (!file.isDirectory()) {
                return true;
            }
            return FilenameUtils.wildcardMatch(file.getName(), "*.java");
        }
    }

    public boolean isInspecting(Class<?> clazz) {
        ensureScan();
        return classesToInspect.contains(clazz);
    }
    
    public int getAmountOfInspectedClasses() {
        return classesToInspect.size();
    }

}
