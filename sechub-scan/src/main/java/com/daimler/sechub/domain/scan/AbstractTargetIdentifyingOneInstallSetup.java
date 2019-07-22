// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan;

import org.springframework.util.StringUtils;

/**
 * Abstract base class for scans where INTERNET and INTRANET scans will be done by ONE installation.
 * The installation must have the ability to to scan both parts by using an identifier. 
 * @author Albert Tregnaghi
 *
 */
public abstract class AbstractTargetIdentifyingOneInstallSetup extends AbstractInstallSetup implements TargetIdentifiyingOneInstallSetup{
	

	@Override
	protected void init(ScanInfo info) {
		info.canScanDaimlerIntranet=notEmpty(getIdentifierWhenDaimlerIntranetTarget());
		info.canScanInternet=notEmpty(getIdentifierWhenInternetTarget());
	}
	
	private boolean notEmpty(Object object) {
		return ! StringUtils.isEmpty(object);
	}
	
	@Override
	public final String getIdentifier(TargetType target) {
		if (isDaimlerIntranet(target)) {
			return getIdentifierWhenDaimlerIntranetTarget();
		}
		if (isInternet(target)) {
			return getIdentifierWhenInternetTarget();
		}
		throw createUnsupportedTargetTypeException(target);
	}

	protected abstract String getIdentifierWhenInternetTarget();

	protected abstract String getIdentifierWhenDaimlerIntranetTarget();
	
	

}
