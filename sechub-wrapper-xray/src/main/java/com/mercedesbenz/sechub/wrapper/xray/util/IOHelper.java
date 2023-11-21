package com.mercedesbenz.sechub.wrapper.xray.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.mercedesbenz.sechub.wrapper.xray.XrayWrapperException;
import com.mercedesbenz.sechub.wrapper.xray.cli.XrayWrapperExitCode;

public class IOHelper {

    public String readInputStreamAsString(InputStream inputStream) throws XrayWrapperException {
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
