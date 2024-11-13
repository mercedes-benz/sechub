// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.project;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.sharedkernel.security.RoleConstants;

import jakarta.annotation.security.RolesAllowed;

@Service
@RolesAllowed(RoleConstants.ROLE_SUPERADMIN)
public class ListProjectsService {
    @Autowired
    ProjectRepository projectRepository;

    public List<String> listProjects() {
        return projectRepository.findAll().stream().map(Project::getId).collect(Collectors.toList());
    }
}
