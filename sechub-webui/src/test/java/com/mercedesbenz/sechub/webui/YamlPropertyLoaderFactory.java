// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.webui;

import java.io.IOException;
import java.util.List;

import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.DefaultPropertySourceFactory;
import org.springframework.core.io.support.EncodedResource;

/**
 * Spring Boot's default property source factory does not support YAML files
 * directly in `@TestPropertySource`. By using this custom factory, we can load
 * properties from YAML files, which is often more convenient and readable for
 * complex configurations.
 *
 * @author hamidonos
 */
public class YamlPropertyLoaderFactory extends DefaultPropertySourceFactory {

    @Override
    @SuppressWarnings("NullableProblems")
    public PropertySource<?> createPropertySource(String name, EncodedResource resource) throws IOException {
        Resource actualResource = resource.getResource();
        if (actualResource.exists()) {
            YamlPropertySourceLoader loader = new YamlPropertySourceLoader();
            List<PropertySource<?>> propertySources = loader.load(name != null ? name : actualResource.getFilename(), actualResource);
            return propertySources.isEmpty() ? null : propertySources.get(0);
        }
        return super.createPropertySource(name, resource);
    }
}