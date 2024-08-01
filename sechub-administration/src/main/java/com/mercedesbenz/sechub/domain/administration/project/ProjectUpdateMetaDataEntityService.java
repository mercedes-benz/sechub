// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.project;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.domain.administration.project.ProjectJsonInput.ProjectMetaData;
import com.mercedesbenz.sechub.sharedkernel.RoleConstants;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.error.NotFoundException;
import com.mercedesbenz.sechub.sharedkernel.logging.AuditLogService;
import com.mercedesbenz.sechub.sharedkernel.logging.LogSanitizer;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.project.UseCaseUpdateProjectMetaData;
import com.mercedesbenz.sechub.sharedkernel.validation.UserInputAssertion;

import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.constraints.NotNull;

@Service
@RolesAllowed(RoleConstants.ROLE_SUPERADMIN)
public class ProjectUpdateMetaDataEntityService {

    @Autowired
    AuditLogService auditLog;

    @Autowired
    ProjectRepository repository;

    @Autowired
    ProjectMetaDataEntityRepository metaDataRepository;

    @Autowired
    LogSanitizer logSanitizer;

    @Autowired
    UserInputAssertion assertion;

    /* @formatter:off */
	@UseCaseUpdateProjectMetaData(
			@Step(number = 2,
			name = "Update project",
			description = "The service will update the <<section-shared-project, Project metadata>>."))
	/* @formatter:on */
    public void updateProjectMetaData(String projectId, @NotNull ProjectMetaData metaData) {
        auditLog.log("triggers update of metadata for project {}. Updated metadata shall be {}", logSanitizer.sanitize(projectId, 30), metaData);

        assertion.assertIsValidProjectId(projectId);

        Optional<Project> found = repository.findById(projectId);
        if (found.isEmpty()) {
            throw new NotFoundException("Project '" + projectId + "' does not exist.");
        }

        Project project = found.get();

        // update is currently a replace action
        metaDataRepository.deleteAll(project.getMetaData());

        List<ProjectMetaDataEntity> metaDataEntities = metaData.getMetaDataMap().entrySet().stream()
                .map(entry -> new ProjectMetaDataEntity(projectId, entry.getKey(), entry.getValue())).collect(Collectors.toList());

        metaDataRepository.saveAll(metaDataEntities);
    }
}
