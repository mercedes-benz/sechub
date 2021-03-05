// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.admin;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.springframework.util.StreamUtils;

import com.daimler.sechub.domain.scan.log.ProjectScanLog;

public class FullScanDataToZipOutputSupport {

	public void writeScanData(FullScanData data, OutputStream outputStream) throws IOException{
		List<String> fileNamesAlreadyUsed = new ArrayList<>();

		try (ZipOutputStream zippedOut = new ZipOutputStream(outputStream)) {
	    	for (ProjectScanLog log: data.allScanLogs) {
	    		writeStringAsZipFileEntry(zippedOut, log.toString(), "log_"+log.getUUID(),fileNamesAlreadyUsed);
	    	}
	    	for (ScanData scanData: data.allScanData) {
	    		writeStringAsZipFileEntry(zippedOut, scanData.result, createFilenamePart(scanData),fileNamesAlreadyUsed);
	    		String metaData = scanData.metaData;
	    		if (metaData==null) {
	    		    metaData="{ \"message\" : \"no meta data available\" }";
	    		}
	    		writeStringAsZipFileEntry(zippedOut, metaData, "metadata_"+createFilenamePart(scanData),fileNamesAlreadyUsed);
	    	}
	    	zippedOut.closeEntry();
	    	zippedOut.finish();
	    }
	}
	
	private String createFilenamePart(ScanData data) {
	    if (data.executorConfigUUID==null) {
	        return data.productId;
	    }
	    return data.productId+"_"+data.executorConfigUUID;
	}

	private void writeStringAsZipFileEntry(ZipOutputStream zippedOut, String string, String wantedFileName, List<String> fileNamesAlreadyUsed) throws UnsupportedEncodingException, IOException {
		/* prevent duplicated filenames*/
		String fileName=wantedFileName;
		int index=0;
		while (fileNamesAlreadyUsed.contains(fileName)) {
			index++;
			fileName=wantedFileName+"["+index+"]";
		}
		fileNamesAlreadyUsed.add(fileName);

		String fileEnding="txt";
		if (string.startsWith("{")) {
			fileEnding="json";
		}else if (string.startsWith("<")) {
			fileEnding="xml";
		}
		byte[] asArray = string.getBytes("UTF-8");
		ByteArrayInputStream bais = new ByteArrayInputStream(asArray);

		ZipEntry e = new ZipEntry(fileName+"."+fileEnding);
		e.setSize(asArray.length);
		e.setTime(System.currentTimeMillis());
		zippedOut.putNextEntry(e);

		StreamUtils.copy(bais, zippedOut);
	}
}
