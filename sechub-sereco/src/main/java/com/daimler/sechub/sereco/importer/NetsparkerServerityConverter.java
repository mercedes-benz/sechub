// SPDX-License-Identifier: MIT
package com.daimler.sechub.sereco.importer;

import com.daimler.sechub.sereco.metadata.Severity;

// https://www.netsparker.com/support/vulnerability-severity-levels-netsparker/
//Critical
//High
//Medium
//Low
// funny: these are not the real values in xml
//https://www.netsparker.com/blog/docs-and-faqs/sample-xml-report-vulnerability-mapping-scanner/
public enum NetsparkerServerityConverter {

		/* FIXME Albert Tregnaghi, 2019-04-09: hmm.. this must be checked .. see SECHUB-396 */

		NONE(Severity.INFO),

		LOW(Severity.LOW),

		MEDIUM(Severity.MEDIUM),

		HIGH(Severity.HIGH),

		IMPORTANT(Severity.CRITICAL),

		;

		private Severity severity;


		private NetsparkerServerityConverter(Severity severity) {
			this.severity=severity;
		}


		public static Severity convert(String severity) {
			if (severity==null) {
				return Severity.UNCLASSIFIED;
			}
			String upperCased = severity.toUpperCase();
			for (NetsparkerServerityConverter netsparkerSeverity: values()) {
				if (netsparkerSeverity.name().contentEquals(upperCased)) {
					return netsparkerSeverity.severity;
				}
			}
			return Severity.UNCLASSIFIED;
		}
}
