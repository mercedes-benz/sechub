// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.docgen;

import java.lang.reflect.Constructor;

import org.springframework.boot.context.properties.ConfigurationProperties;

public class ConfigurationPropertiesData {

    public ConfigurationProperties properties;
    public Class<?> propertiesClass;
    public Constructor<?> constructor;
}
