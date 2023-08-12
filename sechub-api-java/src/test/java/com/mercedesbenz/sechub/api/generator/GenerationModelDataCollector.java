// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.api.generator;

import static com.mercedesbenz.sechub.api.generator.BeanGeneratorUtil.*;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GenerationModelDataCollector {

    private static final Logger LOG = LoggerFactory.getLogger(GenerationModelDataCollector.class);

    private ApiWrapperGenerationContext context;

    GenerationModelDataCollector(ApiWrapperGenerationContext context) {
        this.context = context;
    }

    public void collect() {
        collectReferencedGeneratedClassesAndAddMissingPublicParts();
        checkMissingMappingsAndMakeSuggestionInLog();
    }

    private void collectReferencedGeneratedClassesAndAddMissingPublicParts() {
        boolean addedMissingParts = false;
        int round = 0;
        do {
            LOG.info("Collecting missing parts and references, round:{}", round);

            List<MapGenInfo> initialized = new ArrayList<>(context.getInfoList());
            addedMissingParts = false;
            for (MapGenInfo initializedInfo : initialized) {
                if (initializedInfo.ignored) {
                    continue;
                }

                boolean addedParts = collectReferencedGeneratedClassesAndAddMissingPublicParts(initializedInfo, true);
                addedMissingParts = addedMissingParts || addedParts;
            }
            LOG.info("Added missing parts: {}", addedMissingParts);
            round++;
            if (round > 1000) {
                throw new IllegalStateException("Infinite loop detected - should not happen.");
            }
        } while (addedMissingParts);

    }

    private boolean collectReferencedGeneratedClassesAndAddMissingPublicParts(MapGenInfo infoToGenerate, boolean createNonExisting) {
        LOG.info("Inspecting {}", infoToGenerate.fromGenclazz);
        if (infoToGenerate.fromGenclazz.getName().endsWith("ExecutorConfigurationSetup")) {
            LOG.info("found it");
        }
        List<Method> methods = collectGettersAndSetters(infoToGenerate.fromGenclazz);
        boolean addedPublicParts = false;
        for (Method method : methods) {

            List<Parameter> paramList = getParameters(method);
            TypeInfo methodReturnTypeInfo = new TypeInfo(method);

            String beanName = resolveBeanName(method);

            if (methodReturnTypeInfo.getInnerTypeAsString() != null && methodReturnTypeInfo.getInnerTypeAsString().contains(context.getGenModelPackage())) {

                boolean added = addPublicClassAsReferenceIfNecessary(infoToGenerate, beanName, methodReturnTypeInfo, true);
                addedPublicParts = addedPublicParts || added;
            }

            if (!paramList.isEmpty()) {
                addedPublicParts = handleFirstParameter(infoToGenerate, createNonExisting, addedPublicParts, paramList, beanName);
            }

        }
        return addedPublicParts;
    }

    private boolean handleFirstParameter(MapGenInfo infoToGenerate, boolean createNonExisting, boolean addedPublicParts, List<Parameter> paramList,
            String beanName) {
        Parameter firstParam = paramList.get(0);
        TypeInfo parameterInfo = new TypeInfo(firstParam);

        Class<?> type = parameterInfo.getInnerType();
        if (type == null) {
            LOG.warn("inner type not set!");
        }
        if (type.getPackageName().contains(context.getGenModelPackage())) {
            boolean added = addPublicClassAsReferenceIfNecessary(infoToGenerate, beanName, parameterInfo, createNonExisting);
            addedPublicParts = addedPublicParts || added;
        }
        return addedPublicParts;
    }

    /* returns true when added */
    private boolean addPublicClassAsReferenceIfNecessary(MapGenInfo infoToGenerate, String beanName, TypeInfo openApiGenClassInfo, boolean createNonExisting) {
        MapGenInfo foundMapGenInfoForType = findMapGenForGenClass(openApiGenClassInfo.getInnerType());

        if (foundMapGenInfoForType == null) {
            if (!createNonExisting) {
                return false;
            }
            try {
                LOG.info("Adding missing public part: {}", openApiGenClassInfo.getInnerTypeAsString());
                context.mapModel(openApiGenClassInfo.getInnerType().getSimpleName());

                foundMapGenInfoForType = findMapGenForGenClass(openApiGenClassInfo.getInnerType());
                if (foundMapGenInfoForType == null) {
                    throw new IllegalStateException("Auto model mapping failed for open api gen class: " + openApiGenClassInfo.getInnerType().getSimpleName());
                }

            } catch (Exception e) {
                throw new IllegalStateException("Cannot auto map model for open api gen class: " + openApiGenClassInfo.getInnerType().getSimpleName(), e);
            }
        }
        // must be always public available - mark here to ensure not forgotten
        foundMapGenInfoForType.markPublicAvailable();

        return infoToGenerate.addReference(beanName, foundMapGenInfoForType, openApiGenClassInfo);
    }

    private MapGenInfo findMapGenForGenClass(Class<?> type) {
        StringBuilder sb = new StringBuilder();
        for (MapGenInfo info : context.getInfoList()) {
            sb.append(info.toString()).append("\n");
            if (type.equals(info.fromGenclazz)) {
                return info;
            }
        }
        return null;
    }

    private void checkMissingMappingsAndMakeSuggestionInLog() {
        File modelRootFolder = new File("./gen/src/main/java/" + context.getGenModelPackage().replace('.', '/'));
        File[] files = modelRootFolder.listFiles();

        Set<String> suggestions = new TreeSet<>();
        for (File file : files) {
            String baseName = FilenameUtils.getBaseName(file.getName());
            if (baseName.startsWith("Abstract")) {
                continue;
            }
            boolean found = false;
            for (MapGenInfo info : context.getInfoList()) {
                if (info.fromGenclazz.getSimpleName().equals(baseName)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                suggestions.add("mapModel(\"" + baseName + "\");\n");
            }
        }
        if (!suggestions.isEmpty()) {
            StringBuilder missingCodeSb = new StringBuilder();
            for (String suggestion : suggestions) {
                missingCodeSb.append(suggestion);
            }

            String missingCode = missingCodeSb.toString();
            if (!missingCode.isEmpty()) {
                LOG.info("Some generated parts are not marked to generate inside `ApiWrapperObjectsGenerator.java`. Here is a code snippet to add them:\n{}",
                        missingCode);
            }
        }
    }

}
