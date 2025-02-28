// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.zapwrapper;

import java.io.File;

public class ZapWrapperManualTestUtil {

    private static File baseFolder = new File("./build/manual-test");

    private static File userMessagesFolder = new File(baseFolder, "user-messages");

    private static File eventsFolder = new File(baseFolder, "events");

    private static File tempFolder = new File(baseFolder, "temp");

    static {
        userMessagesFolder.mkdirs();
        eventsFolder.mkdirs();
        tempFolder.mkdirs();
    }

    public static File getUserMessagesFolder() {
        return userMessagesFolder;
    }

    public static File getEventsFolder() {
        return eventsFolder;
    }

    public static File getTempFolder() {
        return tempFolder;
    }
}
