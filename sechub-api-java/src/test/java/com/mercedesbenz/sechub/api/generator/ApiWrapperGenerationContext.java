// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.api.generator;

import java.util.ArrayList;
import java.util.List;

import com.mercedesbenz.sechub.commons.TextFileWriter;

public class ApiWrapperGenerationContext {
    private TextFileWriter textFileWriter = new TextFileWriter();
    private String basePackage = "com.mercedesbenz.sechub.api";
    private String genModelPackage = basePackage + ".internal.gen.model";

    private String targetAbstractModelPackage = basePackage + ".internal.model";
    private String targetModelPackage = basePackage;
    private String abstractModelTargetPath = "src/main/java/" + targetAbstractModelPackage.replace('.', '/');
    private String modelTargetPath = "src/main/java/" + targetModelPackage.replace('.', '/');

    private List<MapGenInfo> infoList = new ArrayList<>();
    private GenerationModelDataCollector collector;
    private SetterGetterGenerationSupport setterGetterSupport;

    public List<MapGenInfo> getInfoList() {
        return infoList;
    }

    public ApiWrapperGenerationContext() {
        collector = new GenerationModelDataCollector(this);
        setterGetterSupport = new SetterGetterGenerationSupport(this);
    }

    public GenerationModelDataCollector getCollector() {
        return collector;
    }

    public SetterGetterGenerationSupport getSetterGetterSupport() {
        return setterGetterSupport;
    }

    public void ignoreModel(String generationName) throws Exception {
        mapModel(generationName).ignored = true;
    }

    public MapGenInfo mapModel(String generationName) throws Exception {
        return mapModel(generationName, generationName.substring("OpenApi".length()));
    }

    public MapGenInfo mapModel(String generationName, String targetClassName) throws Exception {
        if (!generationName.startsWith("OpenApi")) {
            throw new IllegalArgumentException("Wrong argument, mapped parts must start with OpenAPI, but class was:" + generationName);
        }
        MapGenInfo info = new MapGenInfo();
        getInfoList().add(info);

        String className = genModelPackage + "." + generationName;
        try {
            info.fromGenclazz = Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Was not able to map " + generationName + " to " + targetClassName + "\nClass not found:" + className, e);
        }
        info.targetInternalAccessClassName = "InternalAccess" + targetClassName;
        info.targetClassName = targetClassName;
        return info;

    }

    public TextFileWriter getTextFileWriter() {
        return textFileWriter;
    }

    public String getBasePackage() {
        return basePackage;
    }

    public String getGenModelPackage() {
        return genModelPackage;
    }

    public String getTargetAbstractModelPackage() {
        return targetAbstractModelPackage;
    }

    public String getTargetModelPackage() {
        return targetModelPackage;
    }

    public String getAbstractModelTargetPath() {
        return abstractModelTargetPath;
    }

    public String getModelTargetPath() {
        return modelTargetPath;
    }
}
