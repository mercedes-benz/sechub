// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.api.generator;

import static com.mercedesbenz.sechub.api.generator.BeanGeneratorUtil.*;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.List;

public class SetterGetterGenerationSupport {

    private ApiWrapperGenerationContext context;

    public SetterGetterGenerationSupport(ApiWrapperGenerationContext context) {
        this.context = context;
    }

    public void generateMethod(Method method, Template template, String visibility, boolean handleNull, String callFieldName) {
        List<Parameter> paramList = getParameters(method);

        String methodSignature = createMethodSignature(method, paramList, visibility);
        String call = createCall(method, paramList, callFieldName);

        String returnType = resolveReturnType(method);
        String beanName = resolveBeanName(method);

        if (returnType.startsWith(context.getGenModelPackage())) {
            // just ignored, because this
            // means we have a getter for an internal part - must be replaced by wrapper
            // variant
        } else if (methodSignature.contains(context.getGenModelPackage())) {
            // just ignored, because this
            // means paramter has generated part -> setter - must be replaced by wrapper
        } else {
            if (!method.getReturnType().equals(Void.TYPE)) {
                template.addLine(methodSignature + "{");
                TypeInfo info = new TypeInfo(method);
                if (info.isAsList()) {
                    if (handleNull) {
                        template.addLine("          if (" + callFieldName + ".get" + beanName + "() == null) {");
                        template.addLine("              set" + beanName + "(new ArrayList<>());");
                        template.addLine("          }");
                    }
                }
                template.addLine("          return " + call + ";");
                template.addLine("    }");
                template.addLine("");
            } else {
                /* default variant, simple getter/setter - just generate as is */
                template.addLine(methodSignature + "{");
                template.addLine("          " + call + ";");
                template.addLine("    }");
                template.addLine("");
            }
        }

    }

    private String createCall(Method method, List<Parameter> paramList, String callFieldName) {
        String call = callFieldName + "." + method.getName() + "(";

        Iterator<Parameter> itParam = paramList.iterator();

        while (itParam.hasNext()) {
            call += paramNameForMethod(method, itParam.next());
            if (itParam.hasNext()) {
                call += ", ";
            }
        }

        call += ")";
        return call;
    }

    private String createMethodSignature(Method method, List<Parameter> paramList, String visibility) {
        String methodSignature = "    " + visibility + " " + resolveReturnType(method) + " " + method.getName() + "(";
        Iterator<Parameter> itParam = paramList.iterator();

        while (itParam.hasNext()) {
            Parameter param = itParam.next();
            methodSignature += resolveShortAsPossibleType(param.getParameterizedType().getTypeName());
            methodSignature += " ";
            methodSignature += paramNameForMethod(method, param);
            if (itParam.hasNext()) {
                methodSignature += ", ";
            }
        }
        methodSignature += ")";
        return methodSignature;
    }

    private String paramNameForMethod(Method method, Parameter param) {
        String name = param.getName();
        if (!name.startsWith("arg")) {
            return name;
        }
        return asFieldName(resolveBeanName(method));
    }

    private String resolveReturnType(Method method) {
        Type genericReturnType = method.getGenericReturnType();
        return resolveShortAsPossibleType(genericReturnType.getTypeName());
    }

    private String resolveShortAsPossibleType(String full) {
        String type = full;
        type = type.replaceAll("java\\.lang\\.", "");
        return type;
    }
}
