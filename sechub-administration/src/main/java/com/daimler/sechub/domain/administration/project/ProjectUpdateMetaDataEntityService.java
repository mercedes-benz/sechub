// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.administration.project;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.security.RolesAllowed;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.daimler.sechub.domain.administration.project.ProjectJsonInput.ProjectMetaData;
import com.daimler.sechub.sharedkernel.RoleConstants;
import com.daimler.sechub.sharedkernel.Step;
import com.daimler.sechub.sharedkernel.error.NotFoundException;
import com.daimler.sechub.sharedkernel.logging.AuditLogService;
import com.daimler.sechub.sharedkernel.logging.LogSanitizer;
import com.daimler.sechub.sharedkernel.usecases.admin.project.UseCaseUpdateProjectMetaData;
import com.daimler.sechub.sharedkernel.validation.UserInputAssertion;

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

		assertion.isValidProjectId(projectId);

		Optional<Project> found = repository.findById(projectId);
		if (!found.isPresent()) {
			throw new NotFoundException("Project '" + projectId + "' does not exist.");
		}
		
		Project project = found.get();
		
		// update is currently a replace action
		metaDataRepository.deleteAll(project.getMetaData());
		
		List<ProjectMetaDataEntity> metaDataEntities = metaData.getMetaDataMap().entrySet().stream().map(entry -> new ProjectMetaDataEntity(projectId, entry.getKey(), entry.getValue())).collect(Collectors.toList());
		
		metaDataRepository.saveAll(metaDataEntities);
	}	
}
