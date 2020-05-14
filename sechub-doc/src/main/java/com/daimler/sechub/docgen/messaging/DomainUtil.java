package com.daimler.sechub.docgen.messaging;

import java.lang.reflect.Method;

public class DomainUtil {
    private static final String COM_DAIMLER_SECHUB_DOMAIN = "com.daimler.sechub.domain";

    static String createDomainPartName(Method method) {
        Class<?> clazz = method.getDeclaringClass();
        return createDomainPartName(clazz);
    }

    static String createDomainPartName(Class<?> clazz) {
        return clazz.getSimpleName();
    }

    /* something like com.daimler.sechub.domain.xyz.abc is represented as xyz only*/
    static String createDomainName(Method method) {
        Class<?> clazz =  method.getDeclaringClass();
        return createDomainName(clazz);
    }

    static String createDomainName(Class<?> clazz) {
        String fullName = clazz.getPackage().getName();
        String name = fullName;
        int index = name.indexOf(COM_DAIMLER_SECHUB_DOMAIN);
        int amountOfcharsToSkip = COM_DAIMLER_SECHUB_DOMAIN.length()+1;
        if (index!=-1 && name.length()>amountOfcharsToSkip) {
            name = name.substring(amountOfcharsToSkip);
        }
        index = name.indexOf('.');
        if (index!=-1) {
            name=name.substring(0, index);
        }
        if (name.trim().isEmpty()) {
            name=fullName;
        }
        return name;
    }
}
