// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan;

public enum TargetType {

	/**
	 * Target is an URL/IP available from INTRANET
	 */
	INTRANET(true),

	/**
	 * Target is an URL/IP available from INTERNET
	 */
	INTERNET(true),

	/**
	 * Uploaded code
	 */
	CODE_UPLOAD(true),

	/**
	 * An illegal target, e.g. "localhost", "127.0.0.0"
	 */
	ILLEGAL(false),

	/**
	 * Just an unidentifiable target
	 */
	UNKNOWN(false);

	private boolean valid;

	private TargetType(boolean valid) {
		this.valid=valid;
	}

	/**
	 * @return <code>true</code> when this is a valid target type, means
	 * with this target type a scan can generally be executed (but still
	 * dependent on the product)
	 */
	public boolean isValid() {
		return valid;
	}

	public boolean isIntranet() {
		return TargetType.INTRANET.equals(this);
	}

	public boolean isInternet() {
		return TargetType.INTERNET.equals(this);
	}

	public boolean isCodeUpload() {
		return TargetType.CODE_UPLOAD.equals(this);
	}

	/**
	 * @return an identifier prefix for the target type
	 */
	public String getIdentifierPrefix() {
		return name().toLowerCase()+"://";
	}

}
