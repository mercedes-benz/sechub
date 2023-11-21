package com.mercedesbenz.sechub.wrapper.xray.util;

import java.io.*;

import com.mercedesbenz.sechub.wrapper.xray.XrayWrapperException;
import com.mercedesbenz.sechub.wrapper.xray.cli.XrayWrapperExitCode;

public class ZipFileCreator {
    public void zip(File file, InputStream inputStream) throws XrayWrapperException {
        int read;
        byte[] buffer = new byte[1024];
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(file);
            while ((read = inputStream.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, read);
            }
            inputStream.close();
        } catch (IOException e) {
            throw new XrayWrapperException("Could not save https input stream to zip file", XrayWrapperExitCode.IO_ERROR, e);
        }
    }
}
