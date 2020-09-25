// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product;

import java.net.InetAddress;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daimler.sechub.commons.model.ScanType;
import com.daimler.sechub.domain.scan.InstallSetup;
import com.daimler.sechub.domain.scan.Target;
import com.daimler.sechub.domain.scan.TargetRegistry;
import com.daimler.sechub.sharedkernel.UUIDTraceLogID;
import com.daimler.sechub.sharedkernel.configuration.SecHubCodeScanConfiguration;
import com.daimler.sechub.sharedkernel.configuration.SecHubConfiguration;
import com.daimler.sechub.sharedkernel.configuration.SecHubFileSystemConfiguration;

public abstract class AbstractCodeScanProductExecutor<S extends InstallSetup> extends AbstractInstallSetupProductExecutor<S>
		implements CodeScanProductExecutor {

	private static final Logger LOG = LoggerFactory.getLogger(AbstractCodeScanProductExecutor.class);

	protected List<URI> resolveURIsForTarget(SecHubConfiguration config) {
		/* SecHubCodeScanConfiguration does not provide target uris */
		return Collections.emptyList();
	}

	@Override
	protected List<InetAddress> resolveInetAdressForTarget(SecHubConfiguration config) {
		/* SecHubCodeScanConfiguration does not provide target IPs */
		return Collections.emptyList();
	}

	@Override
	protected ScanType getScanType() {
		return ScanType.CODE_SCAN;
	}

	/**
	 * Fetches filesystem folders to scan for - if defined
	 * 
	 * @param context
	 * @return list containing pathes, or empty list, never <code>null</code>
	 */
	protected List<String> fetchFolders(SecHubConfiguration configuration) {
		if (configuration == null) {
			return Collections.emptyList();
		}
		Optional<SecHubCodeScanConfiguration> codeScan = configuration.getCodeScan();
		if (!codeScan.isPresent()) {
			return Collections.emptyList();
		}
		Optional<SecHubFileSystemConfiguration> fileSystem = codeScan.get().getFileSystem();
		if (fileSystem.isPresent()) {
			return fileSystem.get().getFolders();
		}
		return Collections.emptyList();
	}

	@Override
	protected void customRegistration(UUIDTraceLogID traceLogId, S setup, TargetRegistry registry, SecHubConfiguration config) {
		List<String> folders = fetchFolders(config);
		if (folders.isEmpty()) {
			return;
		}
		for (String folder : folders) {
			if (folder == null) {
				continue;
			}
			tryToRegister(traceLogId, setup, registry, folder);
		}

	}

	private void tryToRegister(UUIDTraceLogID traceLogId, S setup, TargetRegistry registry, String folderPath) {
		Target target = targetResolver.resolveTargetForPath(folderPath);
		if (!setup.isAbleToScan(target.getType())) {
			LOG.error("{}: setup not able to scan target {}", getIdentifier(), target);
			return;
		}
		LOG.debug("{} register scan target:{}", traceLogId, target);
		registry.register(target);
	}

}
