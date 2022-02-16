// SPDX-License-Identifier: MIT
package com.daimler.sechub.docgen.util;

import static com.daimler.sechub.docgen.GeneratorConstants.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Proxy;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DocReflectionUtil {

    private static final Logger LOG = LoggerFactory.getLogger(DocReflectionUtil.class);

    @SuppressWarnings("unchecked")
    public static Class<? extends Annotation> resolveUnproxiedClass(Class<? extends Annotation> clazz) {
        if (Proxy.isProxyClass(clazz)) {
            Class<?>[] interfaces = clazz.getInterfaces();
            if (DEBUG) {
                LOG.info("Found proxy {} has interfaces:{}", clazz, Arrays.asList(interfaces));
            }
            clazz = (Class<? extends Annotation>) interfaces[0];
        }
        return clazz;
    }
}
