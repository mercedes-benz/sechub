// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.authorization;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

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