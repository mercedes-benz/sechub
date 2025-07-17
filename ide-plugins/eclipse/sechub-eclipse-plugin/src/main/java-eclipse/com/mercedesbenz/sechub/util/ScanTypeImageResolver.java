// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.util;

import org.eclipse.swt.graphics.Image;

import com.mercedesbenz.sechub.api.internal.gen.model.ScanType;

public class ScanTypeImageResolver {

	public static Image resolveImage(ScanType scanType) {
		String path = "icons/scantype_unknown.png";

		if (scanType != null) {
			switch (scanType) {
			case CODE_SCAN:
				path = "icons/scantype_code.png";
				break;
			case IAC_SCAN:
				path = "icons/scantype_iac.png";
				break;
			case INFRA_SCAN:
				path = "icons/scantype_infra.png";
				break;
			case LICENSE_SCAN:
				path ="icons/scantype_license.png";
				break;
			case SECRET_SCAN:
				path ="icons/scantype_secret.png";
				break;
			case WEB_SCAN:
				path ="icons/scantype_web.png";
				break;
			default:
				return null;
			}

		}
		return EclipseUtil.getImage(path);
	}
}
