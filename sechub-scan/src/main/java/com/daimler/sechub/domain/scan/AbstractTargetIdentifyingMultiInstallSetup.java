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
		boolean canScanDaimlerIntranet = isNotEmpty(getBaseURLWhenDaimlerIntranetTarget());
		canScanDaimlerIntranet=canScanDaimlerIntranet && isNotEmpty(getUsernameWhenDaimlerIntranetTarget());
		canScanDaimlerIntranet=canScanDaimlerIntranet && isNotEmpty(getPasswordWhenDaimlerIntranetTarget());
		
		info.canScanIntranet=canScanDaimlerIntranet;
		
		boolean canScanInternet = isNotEmpty(getBaseURLWhenInternetTarget());
		canScanInternet=canScanInternet && isNotEmpty(getUsernameWhenInternetTarget());
		canScanInternet=canScanInternet && isNotEmpty(getPasswordWhenInternetTarget());
		
		info.canScanInternet=canScanInternet;
		
	}
	
	
	@Override
	public final String getBaseURL(TargetType type) {
		if (isIntranet(type)) {
			return getBaseURLWhenDaimlerIntranetTarget();
		}
		if (isInternet(type)) {
			return getBaseURLWhenInternetTarget();
		}
		throw createUnsupportedTargetTypeException(type);
	}

	@Override
	public final String getUserId(TargetType type) {
		if (isIntranet(type)) {
			return getUsernameWhenDaimlerIntranetTarget();
		}
		if (isInternet(type)) {
			return getUsernameWhenInternetTarget();
		}
		throw createUnsupportedTargetTypeException(type);
	}

	@Override
	public final String getPassword(TargetType target) {
		if (isIntranet(target)) {
			return getPasswordWhenDaimlerIntranetTarget();
		}
		if (isInternet(target)) {
			return getPasswordWhenInternetTarget();
		}
		throw createUnsupportedTargetTypeException(target);
	}

	protected abstract String getBaseURLWhenInternetTarget();

	protected abstract String getBaseURLWhenDaimlerIntranetTarget();
	
	protected abstract String getUsernameWhenInternetTarget();

	protected abstract String getUsernameWhenDaimlerIntranetTarget();

	protected abstract String getPasswordWhenInternetTarget();

	protected abstract String getPasswordWhenDaimlerIntranetTarget();
	
	

}
