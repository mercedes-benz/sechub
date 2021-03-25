// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan;

import static com.daimler.sechub.commons.core.util.SimpleStringUtils.*;

/**
 * Abstract base class for scans where INTERNET and INTRANET scans will be done by complete different installations
 * @author Albert Tregnaghi
 *
 */
public abstract class AbstractTargetIdentifyingMultiInstallSetup extends AbstractInstallSetup implements TargetIdentifyingMultiInstallSetup{

	@Override
	protected void init(ScanInfo info) {
		boolean canScanIntranet = isNotEmpty(getBaseURLWhenIntranetTarget());
		canScanIntranet=canScanIntranet && isNotEmpty(getUsernameWhenIntranetTarget());
		canScanIntranet=canScanIntranet && isNotEmpty(getPasswordWhenIntranetTarget());
		
		info.canScanIntranet=canScanIntranet;
		
		boolean canScanInternet = isNotEmpty(getBaseURLWhenInternetTarget());
		canScanInternet=canScanInternet && isNotEmpty(getUsernameWhenInternetTarget());
		canScanInternet=canScanInternet && isNotEmpty(getPasswordWhenInternetTarget());
		
		info.canScanInternet=canScanInternet;
		
	}
	
	
	@Override
	public final String getBaseURL(TargetType type) {
		if (isIntranet(type)) {
			return getBaseURLWhenIntranetTarget();
		}
		if (isInternet(type)) {
			return getBaseURLWhenInternetTarget();
		}
		throw createUnsupportedTargetTypeException(type);
	}

	@Override
	public final String getUserId(TargetType type) {
		if (isIntranet(type)) {
			return getUsernameWhenIntranetTarget();
		}
		if (isInternet(type)) {
			return getUsernameWhenInternetTarget();
		}
		throw createUnsupportedTargetTypeException(type);
	}

	@Override
	public final String getPassword(TargetType target) {
		if (isIntranet(target)) {
			return getPasswordWhenIntranetTarget();
		}
		if (isInternet(target)) {
			return getPasswordWhenInternetTarget();
		}
		throw createUnsupportedTargetTypeException(target);
	}

	protected abstract String getBaseURLWhenInternetTarget();

	protected abstract String getBaseURLWhenIntranetTarget();
	
	protected abstract String getUsernameWhenInternetTarget();

	protected abstract String getUsernameWhenIntranetTarget();

	protected abstract String getPasswordWhenInternetTarget();

	protected abstract String getPasswordWhenIntranetTarget();
	
	

}
