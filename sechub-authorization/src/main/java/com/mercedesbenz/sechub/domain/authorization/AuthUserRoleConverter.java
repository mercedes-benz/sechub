// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.authorization;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class AuthUserRoleConverter implements AttributeConverter<AuthUserRole, String> {

    @Override
    public String convertToDatabaseColumn(AuthUserRole attribute) {
        return attribute.getId();
    }

    @Override
    public AuthUserRole convertToEntityAttribute(String dbData) {
        return AuthUserRole.fromId(dbData);
    }

}