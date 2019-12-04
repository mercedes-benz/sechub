package com.daimler.sechub.sharedkernel.util;

public class StacktraceUtil {

	public static Throwable findRootCause(Throwable throwable) {
		if (throwable==null) {
			return null;
		}
		Throwable rootCause = throwable;
		while (rootCause.getCause() != null && rootCause.getCause() != rootCause) {
			rootCause = rootCause.getCause();
		}
		return rootCause;
	}
}
