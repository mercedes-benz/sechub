package com.mercedesbenz.sechub.xraywrapper.util;

import java.io.*;

public class InputStreamSaver {
    /**
     * Saves receiving input stream as zip file to filesystem
     *
     * @param filename
     * @param is
     * @throws IOException
     */
    public static void saveInputStreamToZipFile(String filename, InputStream is) throws IOException {
        File file = new File(filename);
        int read;
        byte[] buffer = new byte[1024];
        FileOutputStream fstream = new FileOutputStream(file);
        while ((read = is.read(buffer)) != -1) {
            fstream.write(buffer, 0, read);
        }
        is.close();
    }

    /**
     * Saves receiving input stream as string
     *
     * @param is
     * @return
     * @throws IOException
     */
    public static StringBuilder saveInputStreamToStringBuilder(InputStream is) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        String inputLine;
        StringBuilder content = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();
        is.close();
        return content;
    }
}
