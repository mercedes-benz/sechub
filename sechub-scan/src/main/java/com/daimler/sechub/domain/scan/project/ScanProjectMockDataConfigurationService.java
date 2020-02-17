package com.daimler.sechub.domain.scan.project;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.daimler.sechub.sharedkernel.Step;
import com.daimler.sechub.sharedkernel.usecases.user.UseCaseUserDefinesProjectMockdata;
import com.daimler.sechub.sharedkernel.usecases.user.UseCaseUserRetrievesProjectMockdata;

@Service
public class ScanProjectMockDataConfigurationService {
	
	@Autowired
	ScanProjectConfigService configService;

	@UseCaseUserRetrievesProjectMockdata(@Step(number=2,name="Service call to get JSON data"))
	public ScanProjectMockDataConfiguration retrieveProjectMockDataConfiguration(String projectId) {
		ScanProjectConfig config = configService.get(projectId,ScanProjectConfigID.MOCK_CONFIGURATION);
		if (config==null) {
			return null;
		}
		String json = config.getData();
		if (json==null) {
			return null;
		}
		return ScanProjectMockDataConfiguration.fromString(json);
	}

	@UseCaseUserDefinesProjectMockdata(@Step(number=2,name="Service call to store mock configuration"))
	public void defineProjectMockDataConfiguration(String projectId, ScanProjectMockDataConfiguration configuration) {
		configService.set(projectId,ScanProjectConfigID.MOCK_CONFIGURATION, configuration.toJSON());
	}

}
