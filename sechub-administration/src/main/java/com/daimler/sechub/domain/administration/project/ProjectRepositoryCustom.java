package com.daimler.sechub.domain.administration.project;

public interface ProjectRepositoryCustom {

	public void deleteProjectWithAssociations(String projectId);
}
