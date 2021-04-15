// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Abstract base class for install setup implementations
 * @author Albert Tregnaghi
 *
 */
public abstract class AbstractInstallSetup implements InstallSetup{
	private ScanInfo info;
	private ReentrantLock lock = new ReentrantLock();

	protected abstract void init(ScanInfo info);

	@Override
	public boolean isAbleToScan(TargetType type) {
		if (type==null) {
			return false;
		}
		ensureInfo();

		if (isIntranet(type)) {
			return info.canScanIntranet;
		}else if (isInternet(type)) {
			return info.canScanInternet;
		}else if (isCode(type)){
			return info.canScanCode;
		}else {
			return false;
		}
	}

	private void ensureInfo() {
		try{
			lock.lock();
			if (info==null) {
				info = new ScanInfo();
				init(info);
			}
		}finally{
			lock.unlock();
		}

	}

	protected boolean isInternet(TargetType type) {
		if (type==null) {
			return false;
		}
		return type.isInternet();
	}

	protected boolean isCode(TargetType type) {
		if (type==null) {
			return false;
		}
		return type.isCodeUpload();
	}


	protected boolean isIntranet(TargetType type) {
		if (type==null) {
			return false;
		}
		return type.isIntranet();
	}

	protected IllegalStateException createUnsupportedTargetTypeException(TargetType type) {
		return new IllegalStateException("target type not supported:"+type);
	}

	protected class ScanInfo{

		protected boolean canScanIntranet;
		protected boolean canScanInternet;
		protected boolean canScanCode;

		
	}

}
