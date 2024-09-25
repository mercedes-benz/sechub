// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter;

import java.io.File;
import java.io.IOException;

import com.mercedesbenz.sechub.commons.TextFileReader;
import com.mercedesbenz.sechub.commons.TextFileWriter;
import com.mercedesbenz.sechub.commons.model.JSONConverter;

/**
 * A file based meta data callback implementation
 *
 * @author Albert Tregnaghi
 *
 */
public class FileBasedAdapterMetaDataCallback implements AdapterMetaDataCallback {

    private File file;
    private static TextFileWriter writer = new TextFileWriter();
    private static TextFileReader reader = new TextFileReader();

    public FileBasedAdapterMetaDataCallback(File file) {
        if (file == null) {
            throw new IllegalArgumentException("file may not be null!");
        }
        this.file = file;
    }

    @Override
    public void persist(AdapterMetaData metaData) {
        String metaDataJson = JSONConverter.get().toJSON(metaData);
        try {
            writer.writeTextToFile(file, metaDataJson, true);
        } catch (IOException e) {
            throw new IllegalStateException("Was not able to store meta data!", e);
        }

    }

    @Override
    public AdapterMetaData getMetaDataOrNull() {
        if (!file.exists()) {
            return null;
        }
        try {

            String data = reader.readTextFromFile(file);
            if (data == null || data.isEmpty()) {
                return null;
            }
            AdapterMetaData result = JSONConverter.get().fromJSON(AdapterMetaData.class, data);
            return result;

        } catch (IOException e) {
            throw new IllegalStateException("Was not able to load meta data from " + file, e);

        }
    }
}
