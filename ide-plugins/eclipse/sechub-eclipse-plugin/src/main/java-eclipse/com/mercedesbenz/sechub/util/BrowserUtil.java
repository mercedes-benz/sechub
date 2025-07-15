package com.mercedesbenz.sechub.util;

import java.net.URI;
import java.net.URL;

import org.eclipse.ui.PlatformUI;

public class BrowserUtil {

	public static void openInExternalBrowser(String target) {
		try {
			URL url = new URI(target).toURL();
			PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser().openURL(url);
		} catch (Exception ex) {
			Logging.logError("Was not able to open url in external browser:" + target, ex);
		}
	}
}
