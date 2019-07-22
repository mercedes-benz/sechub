package com.daimler.sechub.docgen.util;

import java.io.File;

public class LicenseHeaderProvider {

	public static final String LICENSE_SPDX_IDENTIFIER="SPDX-License-Identifier: MIT";

	public String getLicenseHeader(String text, File file) {
		if (file==null) {
			return null;
		}
		if (text==null) {
			return null;
		}
		String name = file.getName();
		if (isAsciidoc(name)) {
			return "// "+LICENSE_SPDX_IDENTIFIER;
		}else if (isPlantuml(name)) {
			if (text.trim().startsWith("@startuml")){
				return "// "+LICENSE_SPDX_IDENTIFIER;
			}
			else return "' "+LICENSE_SPDX_IDENTIFIER;
		}
		return null;
	}

	private boolean isPlantuml(String name) {
		return name.endsWith(".puml")|| name.endsWith(".plantuml");
	}

	private boolean isAsciidoc(String name) {
		return name.endsWith(".adoc")|| name.endsWith(".asciidoc");
	}

}
