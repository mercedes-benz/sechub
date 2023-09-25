package com.mercedesbenz.sechub.xraywrapper.util;

import java.io.*;

public class InputStreamSaver {
    /**
     * Saves receiving input stream as zip file to filesystem
     *
     * @param filename zip filename
     * @param is       input stream from http connection
     * @throws IOException
     */
    public static void saveInputStreamToZipFile(String filename, InputStream is) throws IOException {
        int read;
        byte[] buffer = new byte[1024];
        File file = new File(filename);
        FileOutputStream fstream = new FileOutputStream(file);
        while ((read = is.read(buffer)) != -1) {
            fstream.write(buffer, 0, read);
        }
        is.close();
    }

    /**
     * transforms input stream to sting builder
     *
     * @param is input stream from a http connection
     * @return input stream as string
     * @throws IOException when stream can't be read
     */
    public static String saveInputStreamToStringBuilder(InputStream is) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        String inputLine;
        StringBuilder content = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();
        is.close();
        return content.toString();
    }
}
