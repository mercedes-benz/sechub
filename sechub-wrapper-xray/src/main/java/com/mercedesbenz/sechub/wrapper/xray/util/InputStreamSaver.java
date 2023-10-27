package com.mercedesbenz.sechub.wrapper.xray.util;

import java.io.*;

import com.mercedesbenz.sechub.wrapper.xray.XrayWrapperException;
import com.mercedesbenz.sechub.wrapper.xray.cli.XrayWrapperExitCode;

public class InputStreamSaver {

    public static void saveInputStreamToZipFile(String filename, InputStream inputStream) throws XrayWrapperException {
        int read;
        byte[] buffer = new byte[1024];
        File file = new File(filename);
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

    public static String readInputStreamAsString(InputStream inputStream) throws XrayWrapperException {
        BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
        String inputLine;
        StringBuilder content = new StringBuilder();
        try {
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
        } catch (IOException e) {
            throw new XrayWrapperException("Could not read https input stream as string", XrayWrapperExitCode.IO_ERROR, e);
        }
        return content.toString();
    }
}
