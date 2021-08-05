// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.project;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

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