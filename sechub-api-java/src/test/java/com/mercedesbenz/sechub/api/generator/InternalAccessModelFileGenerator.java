package com.mercedesbenz.sechub.api.generator;

import static com.mercedesbenz.sechub.api.generator.BeanGeneratorUtil.*;

import java.io.File;
import java.lang.reflect.Method;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.api.OldDefaultSecHubClient;

public class InternalAccessModelFileGenerator {

    private static final Logger LOG = LoggerFactory.getLogger(InternalAccessModelFileGenerator.class);

    private ApiWrapperGenerationContext context;

    public InternalAccessModelFileGenerator(ApiWrapperGenerationContext context) {
        this.context = context;
    }

    public void generate() throws Exception {
        for (MapGenInfo info : context.getInfoList()) {
            generateAbstractModel(info);
        }
    }

    private void generateAbstractModel(MapGenInfo info) throws Exception {
        Class<?> fromGenclazz = info.fromGenclazz;
        if (info.ignored) {
            LOG.debug("Ignored: {}", fromGenclazz.getName());
            return;
        }

        LOG.info("Generate: {}", info.targetInternalAccessClassName);
        LOG.debug("-".repeat(120));
        LOG.debug("From    : {}", fromGenclazz);
        File genFile = new File(context.getAbstractModelTargetPath() + "/" + info.targetInternalAccessClassName + ".java");
        LOG.debug("To      : {}", genFile);
        LOG.debug("");

        Template template = new Template();
        template.addLine("// SPDX-License-Identifier: MIT");
        template.addLine("package " + context.getTargetAbstractModelPackage() + ";");
        template.addLine("");
        template.addLine("import java.util.ArrayList;");
        template.addLine("import java.util.List;");
        template.addLine("");
        template.addLine("/**");
        template.addLine(" * " + info.targetInternalAccessClassName + " is a model class for " + OldDefaultSecHubClient.class.getSimpleName()
                + ". It uses internally the generated class");
        template.addLine(" * " + fromGenclazz.getName() + ".<br>");
        template.addLine(" * <br>");
        template.addLine(" * The internal access wrapper class was generated from a developer with");
        template.addLine(" * " + getClass().getName());
        template.addLine(" * and is not intended to be changed manually!");
        template.addLine(" */");
        template.addLine("public class " + info.targetInternalAccessClassName + " {");
        template.addLine("");
        template.addLine("    protected " + info.fromGenclazz.getName() + " delegate;");
        template.addLine("    ");
        template.addLine("    protected " + info.targetInternalAccessClassName + "() {");
        template.addLine("        this(null);");
        template.addLine("    }");
        template.addLine("");
        template.addLine("    public " + info.targetInternalAccessClassName + "(" + info.fromGenclazz.getName() + " delegate) {");
        template.addLine("         if (delegate==null) {");
        template.addLine("             this.delegate = new " + info.fromGenclazz.getName() + "();");
        template.addLine("             initDelegateWithDefaults();");
        template.addLine("         } else {");
        template.addLine("             this.delegate=delegate;");
        template.addLine("         }");
        template.addLine("    }");
        template.addLine("");
        template.addLine("    protected void initDelegateWithDefaults() {");
        template.addLine("        /* child classes can override this */");
        template.addLine("    }");
        template.addLine("");

        generateMethods(template, collectGettersAndSetters(fromGenclazz));
        template.addLine("");
        template.addLine("    public " + fromGenclazz.getName() + " getDelegate(){");
        template.addLine("          return delegate;");
        template.addLine("    }");
        template.addLine("");
        template.addLine("    public boolean equals(Object object) {");
        template.addLine("        if (object instanceof " + info.targetInternalAccessClassName + ") {");
        template.addLine("            " + info.fromGenclazz.getName() + " other = (" + info.fromGenclazz.getName() + ") object;");
        template.addLine("            return delegate.equals(other);");
        template.addLine("        }");
        template.addLine("        return false;");
        template.addLine("    }");
        template.addLine("");
        template.addLine("    public int hashCode() {");
        template.addLine("        return delegate.hashCode();");
        template.addLine("    }");
        template.addLine("}");

        context.getTextFileWriter().writeTextToFile(genFile, template.getCode(), true);

    }

    private void generateMethods(Template template, List<Method> methods) {
        for (Method method : methods) {
            context.getSetterGetterSupport().generateMethod(method, template, "public", true, "delegate");
        }

    }

}
