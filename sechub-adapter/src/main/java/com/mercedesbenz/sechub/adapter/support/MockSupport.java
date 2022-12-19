// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter.support;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MockSupport {

    /**
     * Try to load resource as string by given path.
     *
     * @param path
     * @return string, never <code>null</code>
     * @throws IllegalStateException when resource cannot be found
     *
     **/
    public String loadResourceString(String path) {
        StringBuilder sb = new StringBuilder();

        try (InputStream stream = createPath(path)) {
            if (stream == null) {
                throw new FileNotFoundException("Stream not found for:" + path);
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(stream));
            String line = null;
            while ((line = br.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
        } catch (IOException e) {
            throw new IllegalStateException("Cannot load mock resource:" + path, e);
        }
        return sb.toString();
    }

    private InputStream createPath(String path) {
        File file = new File(path);
        if (file.exists()) {
            /* simply use file stream */
            try {
                return new FileInputStream(file);
            } catch (FileNotFoundException e) {
                throw new IllegalStateException("File exists but not found?");
            }
        }
        /* seems to be not available per file so try resource loading */
        return getClass().getResourceAsStream(path);
    }
}
