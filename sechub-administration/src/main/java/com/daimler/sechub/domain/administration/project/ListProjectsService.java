package com.daimler.sechub.domain.administration.project;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.security.RolesAllowed;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.daimler.sechub.sharedkernel.RoleConstants;

@Service
@RolesAllowed(RoleConstants.ROLE_SUPERADMIN)
public class ListProjectsService {
    @Autowired
    ProjectRepository repository;
    
    public List<String> listProjects() {
        return repository.findAll().stream().map(Project::getId).collect(Collectors.toList());
    }
}
