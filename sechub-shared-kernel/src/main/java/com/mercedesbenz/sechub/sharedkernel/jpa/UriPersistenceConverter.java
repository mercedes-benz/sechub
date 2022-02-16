// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.jpa;

import java.net.URI;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.springframework.util.StringUtils;

@Converter(autoApply = true)
public class UriPersistenceConverter implements AttributeConverter<URI, String> {

    @Override
    public String convertToDatabaseColumn(URI entityValue) {
        return (entityValue == null) ? null : entityValue.toString();
    }

    @Override
    public URI convertToEntityAttribute(String databaseValue) {
        return (StringUtils.hasLength(databaseValue) ? URI.create(databaseValue.trim()) : null);
    }
}