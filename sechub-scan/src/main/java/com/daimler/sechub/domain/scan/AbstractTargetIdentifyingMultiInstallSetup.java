// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan;

import org.springframework.util.StringUtils;

/**
 * Abstract base class for scans where INTERNET and INTRANET scans will be done by complete different installations
 * @author Albert Tregnaghi
 *
 */
public abstract class AbstractTargetIdentifyingMultiInstallSetup extends AbstractInstallSetup implements TargetIdentifyingMultiInstallSetup{

	@Override
	protected void init(ScanInfo info) {
		boolean canScanDaimlerIntranet = notEmpty(getBaseURLWhenDaimlerIntranetTarget());
		canScanDaimlerIntranet=canScanDaimlerIntranet && notEmpty(getUsernameWhenDaimlerIntranetTarget());
		canScanDaimlerIntranet=canScanDaimlerIntranet && notEmpty(getPasswordWhenDaimlerIntranetTarget());
		
		info.canScanDaimlerIntranet=canScanDaimlerIntranet;
		
		boolean canScanInternet = notEmpty(getBaseURLWhenInternetTarget());
		canScanInternet=canScanInternet && notEmpty(getUsernameWhenInternetTarget());
		canScanInternet=canScanInternet && notEmpty(getPasswordWhenInternetTarget());
		
		info.canScanInternet=canScanInternet;
		
	}
	
	private boolean notEmpty(Object object) {
		return ! StringUtils.isEmpty(object);
	}
	
	@Override
	public final String getBaseURL(TargetType type) {
		if (isDaimlerIntranet(type)) {
			return getBaseURLWhenDaimlerIntranetTarget();
		}
		if (isInternet(type)) {
			return getBaseURLWhenInternetTarget();
		}
		throw createUnsupportedTargetTypeException(type);
	}

	@Override
	public final String getUserId(TargetType type) {
		if (isDaimlerIntranet(type)) {
			return getUsernameWhenDaimlerIntranetTarget();
		}
		if (isInternet(type)) {
			return getUsernameWhenInternetTarget();
		}
		throw createUnsupportedTargetTypeException(type);
	}

	@Override
	public final String getPassword(TargetType target) {
		if (isDaimlerIntranet(target)) {
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
