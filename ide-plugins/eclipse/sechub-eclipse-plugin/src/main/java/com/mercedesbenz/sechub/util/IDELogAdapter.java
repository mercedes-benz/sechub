package com.mercedesbenz.sechub.util;

/**
 * The class is used to have gradle build running without compile errors:
 * 
 * gradle (src/main/java) does not know anything about eclipse or eclipse
 * dependencies. If we still need IDE logging in parts which will be build by
 * gradle we have to adapt this...
 */
public class IDELogAdapter {

	private static final FallbackIDELogger FALLBACK = new FallbackIDELogger();

	private static IDELogger ideLogger;

	public static void use(IDELogger ideLogger) {
		IDELogAdapter.ideLogger = ideLogger;
	}

	public static void logError(String message, Throwable t) {
		getIdeLogger().logError(message, t);
	}

	private static IDELogger getIdeLogger() {
		IDELogger logger = ideLogger;
		if (logger == null) {
			return FALLBACK;
		}
		return logger;
	}
}
