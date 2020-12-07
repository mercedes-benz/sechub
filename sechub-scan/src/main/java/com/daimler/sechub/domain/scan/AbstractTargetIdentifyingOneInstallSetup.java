// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan;

import static com.daimler.sechub.commons.core.util.SimpleStringUtils.*;
/**
 * Abstract base class for scans where INTERNET and INTRANET scans will be done by ONE installation.
 * The installation must have the ability to to scan both parts by using an identifier. 
 * @author Albert Tregnaghi
 *
 */
public abstract class AbstractTargetIdentifyingOneInstallSetup extends AbstractInstallSetup implements TargetIdentifiyingOneInstallSetup{
	

	@Override
	protected void init(ScanInfo info) {
		info.canScanIntranet=isNotEmpty(getIdentifierWhenIntranetTarget());
		info.canScanInternet=isNotEmpty(getIdentifierWhenInternetTarget());
	}
	
	@Override
	public final String getIdentifier(TargetType target) {
		if (isIntranet(target)) {
			return getIdentifierWhenIntranetTarget();
		}
		if (isInternet(target)) {
			return getIdentifierWhenInternetTarget();
		}
		throw createUnsupportedTargetTypeException(target);
	}

	protected abstract String getIdentifierWhenInternetTarget();

	protected abstract String getIdentifierWhenIntranetTarget();
	
	

}
