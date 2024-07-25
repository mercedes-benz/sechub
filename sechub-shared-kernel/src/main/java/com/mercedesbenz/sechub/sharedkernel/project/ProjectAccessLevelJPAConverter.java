// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.project;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ProjectAccessLevelJPAConverter implements AttributeConverter<ProjectAccessLevel, String> {

    @Override
    public String convertToDatabaseColumn(ProjectAccessLevel attribute) {
        return attribute.getId();
    }

    @Override
    public ProjectAccessLevel convertToEntityAttribute(String dbData) {
        return ProjectAccessLevel.fromId(dbData);
    }

}