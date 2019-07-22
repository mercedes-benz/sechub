// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.admin;

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

import com.daimler.sechub.domain.scan.log.ProjectScanLog;

public class FullScanDataToZipOutputSupportTest {

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

		File file = File.createTempFile("log_fullscan", ".zip");

		/* execute */
		try(FileOutputStream fos = new FileOutputStream(file)){
			supportToTest.writeScanData(fullScanData, fos );
		}

		/* test */

		List<Data> list = readZipfile(file);
	    assertEquals(3,list.size());
	    Data data1 = assertFile("product1.json",list);
	    Data data2 = assertFile("product2.xml",list);
	    Data data3 = assertFile("log_null.txt",list); // null because log is not persisted and has no UUID

	    assertTrue(data1.content.contains("OK"));
	    assertTrue(data2.content.contains("NOT-OK"));
	    assertTrue(data3.content.contains("'heavy'"));

	}

	@Test
	public void writeScanDataWhichContainsSameProductWillIncrementFilenames() throws Exception {
		/* prepare */
		FullScanData fullScanData = createFullScanDataTwoProductsOneLogEntry();
		FullScanData fullScanData2 = createFullScanDataTwoProductsOneLogEntry();
		fullScanData.allScanData.addAll(fullScanData2.allScanData);

		File file = File.createTempFile("log_fullscan", ".zip");

		/* execute */
		try(FileOutputStream fos = new FileOutputStream(file)){
			supportToTest.writeScanData(fullScanData, fos );
		}

		/* test */

		List<Data> list = readZipfile(file);
	    assertEquals(5,list.size());
	    assertFile("product1.json",list);
	    assertFile("product1[1].json",list);
	    assertFile("product2.xml",list);
	    assertFile("product2[1].xml",list);
	    assertFile("log_null.txt",list); // null because log is not persisted and has no UUID

	}

	private Data assertFile(String name, List<Data> list ) {
		for (Data data: list) {
			if (name.contentEquals(data.name)) {
				return data;
			}
		}
		fail("Did not founda file :"+name);
		return null;
	}

	private FullScanData createFullScanDataTwoProductsOneLogEntry() {
		FullScanData fullScanData = new FullScanData();

		ScanData data1 = new ScanData();
		data1.productId="product1";
		data1.result="{ 'result' : 'OK'}";

		ScanData data2 = new ScanData();
		data2.productId="product2";
		data2.result="<?xml version='1.0'>\n<result>NOT-OK</result>}";

		fullScanData.allScanData.add(data1);
		fullScanData.allScanData.add(data2);

		ProjectScanLog log1 = new ProjectScanLog("project1",sechubJobUUID,"executor1","{'config':'heavy'}");
		fullScanData.allScanLogs.add(log1);
		return fullScanData;
	}

	private List<Data> readZipfile(File file) throws ZipException, IOException {
		List<Data> list = new ArrayList<>();
		try(ZipFile zipFile = new ZipFile(file)){
			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			while(entries.hasMoreElements()){
				ZipEntry entry = entries.nextElement();
				InputStream stream = zipFile.getInputStream(entry);
				try(BufferedReader br = new BufferedReader(new InputStreamReader(stream))){
					String line = null;
					StringBuilder sb = new StringBuilder();
					while ( (line=br.readLine())!=null) {
						sb.append(line);
						sb.append("\n");
					}
					Data d = new Data();
					d.name = entry.getName();
					d.content=sb.toString();
					list.add(d);
				}
			}
		};
		return list;
	}

	private class Data{
		String name;
		String content;
	}

}
