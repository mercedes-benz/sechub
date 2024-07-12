// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.admin;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.junit.Before;
import org.junit.Test;

import com.mercedesbenz.sechub.domain.scan.log.ProjectScanLog;
import com.mercedesbenz.sechub.test.TestUtil;

public class FullScanDataToZipOutputSupportTest {

    private static final String EXECUTOR1 = "executor1";
    private static final String PROJECT1 = "project1";
    private FullScanDataToZipOutputSupport supportToTest;
    private UUID sechubJobUUID;

    @Before
    public void before() {
        supportToTest = new FullScanDataToZipOutputSupport();
        sechubJobUUID = UUID.randomUUID();
    }

    @Test
    public void writeScanDataContainsDataAsExpected() throws Exception {
        /* prepare */
        FullScanData fullScanData = createFullScanDataTwoProductsOneLogEntry();

        File file = TestUtil.createTempFileInBuildFolder("log_fullscan", "zip").toFile();

        /* execute */
        try (FileOutputStream fos = new FileOutputStream(file)) {
            supportToTest.writeScanData(fullScanData, fos);
        }

        /* test */

        List<Data> list = readZipfile(file);
        assertEquals(5, list.size());
        Data data1 = assertFile("product1.json", list);
        Data data2 = assertFile("product2.xml", list);
        Data data3 = assertFile("log_null.txt", list); // null because log is not persisted and has no UUID

        Data data4 = assertFile("metadata_product1.json", list);
        Data data5 = assertFile("metadata_product2.json", list);

        assertTrue(data1.content.contains("OK"));
        assertTrue(data2.content.contains("NOT-OK"));
        assertTrue(data3.content.contains("sechubJobUUID=" + sechubJobUUID));
        assertTrue(data3.content.contains("projectId=project1"));
        assertTrue(data3.content.contains("executedBy=" + EXECUTOR1));

        assertTrue(data4.content.contains("metadata"));
        assertTrue(data4.content.contains("product1"));

        assertTrue(data5.content.contains("metadata"));
        assertTrue(data5.content.contains("product2"));

    }

    @Test
    public void writeScanDataWhichContainsSameProductWillIncrementFilenames() throws Exception {
        /* prepare */
        FullScanData fullScanData = createFullScanDataTwoProductsOneLogEntry();
        FullScanData fullScanData2 = createFullScanDataTwoProductsOneLogEntry();
        fullScanData.allScanData.addAll(fullScanData2.allScanData);

        File file = TestUtil.createTempFileInBuildFolder("log_fullscan", "zip").toFile();

        /* execute */
        try (FileOutputStream fos = new FileOutputStream(file)) {
            supportToTest.writeScanData(fullScanData, fos);
        }

        /* test */

        List<Data> list = readZipfile(file);
        assertEquals(9, list.size());
        assertFile("product1.json", list);
        assertFile("product1[1].json", list);
        assertFile("product2.xml", list);
        assertFile("product2[1].xml", list);
        assertFile("log_null.txt", list); // null because log is not persisted and has no UUID

        assertFile("metadata_product1.json", list);
        assertFile("metadata_product1[1].json", list);
        assertFile("metadata_product2.json", list);
        assertFile("metadata_product2[1].json", list);

    }

    private Data assertFile(String name, List<Data> list) {
        for (Data data : list) {
            if (name.contentEquals(data.name)) {
                return data;
            }
        }
        fail("Did not founda file :" + name);
        return null;
    }

    private FullScanData createFullScanDataTwoProductsOneLogEntry() {
        FullScanData fullScanData = new FullScanData();

        ScanData data1 = new ScanData();
        data1.productId = "product1";
        data1.result = "{ 'result' : 'OK'}";
        data1.metaData = "{ \"metadata\" : \"product1\" }";

        ScanData data2 = new ScanData();
        data2.productId = "product2";
        data2.result = "<?xml version='1.0'>\n<result>NOT-OK</result>}";
        data2.metaData = "{ \"metadata\" : \"product2\" }";

        fullScanData.allScanData.add(data1);
        fullScanData.allScanData.add(data2);

        ProjectScanLog log1 = new ProjectScanLog(PROJECT1, sechubJobUUID, EXECUTOR1);
        fullScanData.allScanLogs.add(log1);
        return fullScanData;
    }

    private List<Data> readZipfile(File file) throws ZipException, IOException {
        List<Data> list = new ArrayList<>();
        try (ZipFile zipFile = new ZipFile(file)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                InputStream stream = zipFile.getInputStream(entry);
                try (BufferedReader br = new BufferedReader(new InputStreamReader(stream))) {
                    String line = null;
                    StringBuilder sb = new StringBuilder();
                    while ((line = br.readLine()) != null) {
                        sb.append(line);
                        sb.append("\n");
                    }
                    Data d = new Data();
                    d.name = entry.getName();
                    d.content = sb.toString();
                    list.add(d);
                }
            }
        }
        ;
        return list;
    }

    private class Data {
        String name;
        String content;
    }

}
