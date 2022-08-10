package com.mercedesbenz.sechub.commons.pds;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.commons.TextFileWriter;
import com.mercedesbenz.sechub.commons.model.SecHubMessage;
import com.mercedesbenz.sechub.commons.model.SecHubMessageType;
import com.mercedesbenz.sechub.test.TestUtil;

class PDSUserMessageSupportTest {

    @Test
    void support_writes_multiple_message_files_to_non_existing_parentfolder() throws Exception {
        /* prepare */
        Path dir = TestUtil.createTempDirectoryInBuildFolder("product_message_support");

        File nonExistingParent = new File(dir.toFile(), "parent-not-existing");
        assertFalse(nonExistingParent.exists());

        PDSUserMessageSupport support = new PDSUserMessageSupport(nonExistingParent.getAbsolutePath(), new TextFileWriter());

        List<SecHubMessage> list = new ArrayList<>();
        list.add(new SecHubMessage(SecHubMessageType.INFO, "info text"));

        list.add(new SecHubMessage(SecHubMessageType.WARNING, "warning text"));
        list.add(new SecHubMessage(SecHubMessageType.WARNING, "warning text"));

        list.add(new SecHubMessage(SecHubMessageType.ERROR, "error text"));
        list.add(new SecHubMessage(SecHubMessageType.ERROR, "error text"));
        list.add(new SecHubMessage(SecHubMessageType.ERROR, "error text"));

        list.add(new SecHubMessage(null, "null text"));
        list.add(new SecHubMessage(null, "null text"));
        list.add(new SecHubMessage(null, "null text"));
        list.add(new SecHubMessage(null, "null text"));

        /* execute */
        support.writeMessages(list);

        /* test */
        assertTrue(nonExistingParent.exists());

        File[] files = nonExistingParent.listFiles();
        assertEquals(list.size(), files.length, "Every message has got its own file");

        int countErrors = 0;
        int countNull = 0;
        int countWarn = 0;
        int countInfo = 0;

        System.out.println("Start inspection of created message files");
        for (File file : files) {
            String name = file.getName();
            System.out.println("> " + name);
            if (name.startsWith("INFO_message")) {
                countInfo++;
            } else if (name.startsWith("WARNING_message")) {
                countWarn++;
            } else if (name.startsWith("ERROR_message")) {
                countErrors++;
            } else if (name.startsWith("message")) {
                countNull++;
            } else {
                fail("Such name was not expected:" + name);
            }
        }
        assertEquals(1, countInfo);
        assertEquals(2, countWarn);
        assertEquals(3, countErrors);
        assertEquals(4, countNull);
    }

}
