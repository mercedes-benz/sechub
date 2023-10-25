package com.mercedesbenz.sechub.wrapper.xray.util;

import java.io.*;

import com.mercedesbenz.sechub.wrapper.xray.cli.XrayWrapperExitCode;
import com.mercedesbenz.sechub.wrapper.xray.cli.XrayWrapperRuntimeException;

public class InputStreamSaver {

    public static void saveInputStreamToZipFile(String filename, InputStream inputStream) throws XrayWrapperRuntimeException {
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
            throw new XrayWrapperRuntimeException("Could not save https input stream to zip file", e, XrayWrapperExitCode.IO_ERROR);
        }
    }

    public static String readInputStreamAsString(InputStream inputStream) throws XrayWrapperRuntimeException {
        BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
        String inputLine;
        StringBuilder content = new StringBuilder();
        try {
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
        } catch (IOException e) {
            throw new XrayWrapperRuntimeException("Could not read https input stream as string", e, XrayWrapperExitCode.IO_ERROR);
        }
        return content.toString();
    }
}
