// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.project;

import org.springframework.core.convert.converter.Converter;

public class ProjectAccessLevelConverter implements Converter<String, ProjectAccessLevel> {

    @Override
    public ProjectAccessLevel convert(String source) {
        return ProjectAccessLevel.fromId(source);
    }

}