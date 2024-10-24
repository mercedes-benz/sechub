package com.mercedesbenz.sechub.api.generator;

import static com.mercedesbenz.sechub.api.generator.BeanGeneratorUtil.*;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.api.OldDefaultSecHubClient;

public class PublicModelFileGenerator {

    private static final Logger LOG = LoggerFactory.getLogger(PublicModelFileGenerator.class);

    private ApiWrapperGenerationContext context;

    public PublicModelFileGenerator(ApiWrapperGenerationContext context) {
        this.context = context;
    }

    public void generate(boolean overwritePublicModelFiles) throws Exception {
        for (MapGenInfo info : context.getInfoList()) {
            generatetPublicModel(info, overwritePublicModelFiles);
        }
    }

    private void generatetPublicModel(MapGenInfo info, boolean overwritePublicModelFiles) throws Exception {
        if (!info.publicAvailable) {
            return;
        }
        Class<?> fromGenclazz = info.fromGenclazz;
        if (info.ignored) {
            LOG.debug("Ignored: {}", fromGenclazz.getName());
            return;
        }

        LOG.info("Generate: {}", info.targetClassName);
        LOG.debug("-".repeat(120));
        LOG.debug("From    : {}", fromGenclazz);
        File genFile = new File(context.getModelTargetPath() + "/" + info.targetClassName + ".java");
        LOG.debug("To      : {}", genFile);
        LOG.debug("");

        String internalAccessClass = context.getTargetAbstractModelPackage() + "." + info.targetInternalAccessClassName;

        CodeTemplate template = new CodeTemplate();
        template.addLine("// SPDX-License-Identifier: MIT");
        template.addLine("package " + context.getTargetModelPackage() + ";");
        template.addLine("");
        template.addLine("import java.util.ArrayList;");
        template.addLine("import java.util.List;");

        template.addLine("");
        template.addLine("/**");
        template.addLine(" * " + info.targetClassName + " is a model class for " + OldDefaultSecHubClient.class.getSimpleName()
                + ". It uses internally the generated class");
        template.addLine(" * " + fromGenclazz.getName() + ".<br>");
        template.addLine(" * <br>");
        template.addLine(" * The wrapper class itself was initial generated with");
        template.addLine(" * " + getClass().getName() + ".");
        template.addLine(" */");
        template.addLine("public class " + info.targetClassName + " {");
        template.addLine("    // only for usage by " + OldDefaultSecHubClient.class.getSimpleName());
        template.addLine("    static List<" + info.targetClassName + "> fromDelegates(List<" + info.fromGenclazz.getName() + "> delegates) {");
        template.addLine("            List<" + info.targetClassName + "> resultList = new ArrayList<>();");
        template.addLine("            if (delegates != null) {");
        template.addLine("                 for (" + info.fromGenclazz.getName() + " delegate: delegates) {");
        template.addLine("                      resultList.add(new " + info.targetClassName + "(delegate));");
        template.addLine("                 }");
        template.addLine("            }");
        template.addLine("            return resultList;");
        template.addLine("    }");
        template.addLine("");
        template.addLine("");
        template.addLine("    // only for usage by " + OldDefaultSecHubClient.class.getSimpleName());
        template.addLine("    static List<" + info.fromGenclazz.getName() + "> toDelegates(List<" + info.targetClassName + "> wrappers) {");
        template.addLine("            List<" + info.fromGenclazz.getName() + "> resultList = new ArrayList<>();");
        template.addLine("            if (wrappers != null) {");
        template.addLine("                 for (" + info.targetClassName + " wrapper: wrappers) {");
        template.addLine("                      resultList.add(wrapper.getDelegate());");
        template.addLine("                 }");
        template.addLine("            }");
        template.addLine("            return resultList;");
        template.addLine("    }");
        template.addLine("");
        template.addLine("    private " + internalAccessClass + " internalAccess;");
        generateAdditionalWrapperFields(info, template);
        /* constructors */
        template.addLine("");
        template.addLine("    public " + info.targetClassName + "() {");
        template.addLine("        this(null);");
        template.addLine("    }");
        template.addLine("");
        template.addLine("    " + info.targetClassName + "(" + info.fromGenclazz.getName() + " delegate) {");
        template.addLine("         this.internalAccess= new " + internalAccessClass + "(delegate);");
        template.addLine("    }");
        template.addLine("");
        template.addLine("    // only for usage by " + OldDefaultSecHubClient.class.getSimpleName());
        template.addLine("    " + info.fromGenclazz.getName() + " getDelegate() {");
        template.addLine("         return internalAccess.getDelegate();");
        template.addLine("    }");
        template.addLine("");
        /* other methods */
        generatePublicSetterGetterMethods(info, template);
        template.addLine("");
        generateAdditionalWrapperMethods(info, template);
        template.addLine("");
        template.addLine("}");

        context.getTextFileWriter().writeTextToFile(genFile, template.getCode(), overwritePublicModelFiles);

    }

    private void generatePublicSetterGetterMethods(MapGenInfo info, CodeTemplate template) {
        for (Method method : collectGettersAndSetters(info.fromGenclazz)) {
            context.getSetterGetterSupport().generateMethod(method, template, "public", false, "internalAccess");
        }
    }

    private void generateAdditionalWrapperMethods(MapGenInfo info, CodeTemplate template) {

        Map<String, BeanDataContainer> map = info.getReferenceMap();
        for (String beanName : map.keySet()) {
            BeanDataContainer other = map.get(beanName);
            generateMethodsToReferenceOtherWrapper(beanName, other, template);
        }

    }

    private void generateMethodsToReferenceOtherWrapper(String beanName, BeanDataContainer other, CodeTemplate template) {
        String fieldName = asFieldName(beanName);
        if (other.isAsList()) {

            template.addLine("    public " + other.asTargetTypeResult() + " get" + beanName + "(){");
            template.addLine("         if (" + fieldName + " == null) {");
            template.addLine(
                    "               " + fieldName + " = " + other.targetClassName() + ".fromDelegates(internalAccess.getDelegate().get" + beanName + "());");
            template.addLine("         }");
            template.addLine("         return " + fieldName + ";");
            template.addLine("    }");
            template.addLine("");
            template.addLine("    public void set" + beanName + "(" + other.asTargetTypeResult() + " " + fieldName + "){");
            template.addLine("         this." + fieldName + " = " + fieldName + ";");
            template.addLine("         this.internalAccess.getDelegate().set" + beanName + "(" + other.targetClassName() + ".toDelegates(" + fieldName + "));");
            template.addLine("    }");
            template.addLine("");

        } else {

            template.addLine("    public " + other.asTargetTypeResult() + " get" + beanName + "(){");
            template.addLine("         if (" + fieldName + " == null) {");
            template.addLine(
                    "               " + fieldName + " = new " + other.asTargetTypeInstance() + "(internalAccess.getDelegate().get" + beanName + "());");
            template.addLine("               internalAccess.getDelegate().set" + beanName + "(" + fieldName
                    + ".getDelegate()); // necessary if delegate had no content, but wrapper created one");
            template.addLine("         }");
            template.addLine("         return " + fieldName + ";");
            template.addLine("    }");
            template.addLine("");
            template.addLine("    public void set" + beanName + "(" + other.asTargetTypeResult() + " " + fieldName + "){");
            template.addLine("         this." + fieldName + " = " + fieldName + ";");
            template.addLine("         this.internalAccess.getDelegate().set" + beanName + "(" + fieldName + ".getDelegate());");
            template.addLine("    }");
            template.addLine("");
        }
    }

    private void generateAdditionalWrapperFields(MapGenInfo info, CodeTemplate template) {

        Map<String, BeanDataContainer> map = info.getReferenceMap();
        for (String beanName : map.keySet()) {
            BeanDataContainer other = map.get(beanName);
            generateAdditionalWrapperFieldsForOther(beanName, other, template);
        }

    }

    private void generateAdditionalWrapperFieldsForOther(String beanName, BeanDataContainer other, CodeTemplate template) {
        template.addLine("    private " + other.asTargetTypeResult() + " " + asFieldName(beanName) + ";");
    }
}
