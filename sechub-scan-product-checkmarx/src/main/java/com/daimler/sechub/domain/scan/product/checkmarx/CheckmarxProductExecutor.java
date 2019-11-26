// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product.checkmarx;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.daimler.sechub.adapter.AbstractAdapterConfigBuilder;
import com.daimler.sechub.adapter.checkmarx.CheckmarxAdapter;
import com.daimler.sechub.adapter.checkmarx.CheckmarxAdapterConfig;
import com.daimler.sechub.adapter.checkmarx.CheckmarxConfig;
import com.daimler.sechub.domain.scan.OneInstallSetupConfigBuilderStrategy;
import com.daimler.sechub.domain.scan.TargetRegistry.TargetRegistryInfo;
import com.daimler.sechub.domain.scan.product.AbstractCodeScanProductExecutor;
import com.daimler.sechub.domain.scan.product.ProductIdentifier;
import com.daimler.sechub.domain.scan.product.ProductResult;
import com.daimler.sechub.sharedkernel.MustBeDocumented;
import com.daimler.sechub.sharedkernel.execution.SecHubExecutionContext;
import com.daimler.sechub.sharedkernel.storage.StorageService;
import com.daimler.sechub.storage.core.JobStorage;

@Service
public class CheckmarxProductExecutor extends AbstractCodeScanProductExecutor<CheckmarxInstallSetup> {

	private static final Logger LOG = LoggerFactory.getLogger(CheckmarxProductExecutor.class);

	@Value("${sechub.adapter.checkmarx.scanresultcheck.period.minutes:-1}")
	@MustBeDocumented(AbstractAdapterConfigBuilder.DOCUMENT_INFO_TIMEOUT)
	private int scanResultCheckPeriodInMinutes;

	@Value("${sechub.adapter.checkmarx.scanresultcheck.timeout.minutes:-1}")
	@MustBeDocumented(AbstractAdapterConfigBuilder.DOCUMENT_INFO_TIMEOUT)
	private int scanResultCheckTimeOutInMinutes;

	@Autowired
	CheckmarxAdapter checkmarxAdapter;

	@Autowired
	CheckmarxInstallSetup installSetup;

	@Autowired
	StorageService storageService;

	@Override
	protected List<ProductResult> executeWithAdapter(SecHubExecutionContext context, CheckmarxInstallSetup setup, TargetRegistryInfo data)
			throws Exception {
		LOG.debug("Trigger checkmarx adapter execution");

		UUID jobUUID = context.getSechubJobUUID();
		String projectId = context.getConfiguration().getProjectId();

		JobStorage storage = storageService.getJobStorage(projectId, jobUUID);
		try(InputStream sourceCodeZipFileInputStream = storage.fetch("sourcecode.zip")){

			/* @formatter:off */

			CheckmarxAdapterConfig checkMarxConfig =CheckmarxConfig.builder().
					configure(new OneInstallSetupConfigBuilderStrategy(setup)).
					setTimeToWaitForNextCheckOperationInMinutes(scanResultCheckPeriodInMinutes).
					setScanResultTimeOutInMinutes(scanResultCheckTimeOutInMinutes).
					setFileSystemSourceFolders(data.getCodeUploadFileSystemFolders()).
					setSourceCodeZipFileInputStream(sourceCodeZipFileInputStream).
					setTeamIdForNewProjects(setup.getTeamIdForNewProjects()).
					setProjectId(projectId).
					setTraceID(context.getTraceLogIdAsString()).
					/* TODO Albert Tregnaghi, 2018-10-09:policy id - always default id - what about config.getPoliciyID() ?!?! */
					build();
			/* @formatter:on */

			/* execute checkmarx by adapter and return product result */
			String xml = checkmarxAdapter.start(checkMarxConfig);
			ProductResult result = new ProductResult(context.getSechubJobUUID(),projectId, getIdentifier(), xml);
			return Collections.singletonList(result);
		}

	}



	@Override
	public ProductIdentifier getIdentifier() {
		return ProductIdentifier.CHECKMARX;
	}

	@Override
	protected CheckmarxInstallSetup getInstallSetup() {
		return installSetup;
	}

}
