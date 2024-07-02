// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product.pds;

import static com.mercedesbenz.sechub.commons.core.CommonConstants.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModel;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModelSupport;
import com.mercedesbenz.sechub.storage.core.JobStorage;

public class PDSStorageContentProvider {

    private JobStorage storage;
    private boolean sourceRequired;
    private boolean binaryRequired;
    private boolean secHubStorageReused;

    PDSStorageContentProvider(JobStorage storage, boolean sechubStorageReused, ScanType scanType, SecHubConfigurationModelSupport modelSupport,
            SecHubConfigurationModel model) {
        if (!sechubStorageReused) {
            if (storage == null) {
                throw new IllegalStateException("When SecHub storage is not reused, the storage must be not null!");
            }
        }
        this.storage = storage;
        this.secHubStorageReused = sechubStorageReused;

        this.sourceRequired = modelSupport.isSourceRequired(scanType, model);
        this.binaryRequired = modelSupport.isBinaryRequired(scanType, model);
    }

    public boolean isSecHubStorageReused() {
        return secHubStorageReused;
    }

    /**
     * Resolves if the SecHub configuration model needs a source upload for the
     * defined scan type
     *
     * @return <code>true</code> when source upload is necessary otherwise
     *         <code>false</code>
     */
    public boolean isSourceRequired() {
        return sourceRequired;
    }

    /**
     * Resolves if the SecHub configuration model needs a binary upload for the
     * defined scan type
     *
     * @return <code>true</code> when binary upload is necessary otherwise
     *         <code>false</code>
     */
    public boolean isBinaryRequired() {
        return binaryRequired;
    }

    /**
     * If the PDS does reuse SecHub storage or no source is required for this scan
     * this method will return <code>null</code>. In any other case the storage will
     * be called and the underlying input stream fetched.
     *
     * @return stream or <code>null</code>
     * @throws IOException
     */
    public InputStream getSourceZipFileInputStreamOrNull() throws IOException {
        if (storage == null) {
            // in this case no special PDS storage - reuse of sechub storage!
            return null;
        }
        if (!sourceRequired) {
            /* no source required at all... */
            return null;
        }
        return storage.fetch(FILENAME_SOURCECODE_ZIP);
    }

    /**
     * If the PDS does reuse SecHub storage or no source is required for this scan
     * this method will return <code>null</code>. In any other case the storage will
     * be called and the underlying checksum fetched.
     *
     * @param metaDataOrNull
     * @return checksum or <code>null</code>
     * @throws IOException
     */
    public String getSourceZipFileUploadChecksumOrNull() throws IOException {
        if (storage == null) {
            // in this case no special PDS storage - reuse of sechub storage!
            return null;
        }
        if (!sourceRequired) {
            /* no source required at all - means also no checksum... */
            return null;
        }
        try (InputStream inputStream = storage.fetch(FILENAME_SOURCECODE_ZIP_CHECKSUM); Scanner scanner = new Scanner(inputStream)) {
            String result = scanner.hasNext() ? scanner.next() : "";
            return result;
        }

    }

    /**
     * If the PDS does reuse SecHub storage or no source is required for this scan
     * this method will return <code>null</code>. In any other case the storage will
     * be called and the underlying file size information fetched.
     *
     * @param metaDataOrNull
     * @return checksum or <code>null</code>
     * @throws IOException
     */
    public String getSourceZipFileSizeOrNull() throws IOException {
        if (storage == null) {
            // in this case no special PDS storage - reuse of sechub storage!
            return null;
        }
        if (!sourceRequired) {
            /* no source required at all - means also no checksum... */
            return null;
        }
        try (InputStream inputStream = storage.fetch(FILENAME_SOURCECODE_ZIP_FILESIZE); Scanner scanner = new Scanner(inputStream)) {
            String result = scanner.hasNext() ? scanner.next() : "";
            return result;
        }

    }

    /**
     * If the PDS does reuse SecHub storage or no binaries are required for this
     * scan this method will return <code>null</code>. In any other case the storage
     * will be called and the underlying input stream fetched.
     *
     * @param metaDataOrNull
     * @return stream or <code>null</code>
     * @throws IOException
     */
    public InputStream getBinariesTarFileInputStreamOrNull() throws IOException {
        if (storage == null) {
            return null;
        }
        if (!binaryRequired) {
            return null;
        }
        return storage.fetch(FILENAME_BINARIES_TAR);
    }

    /**
     * If the PDS does reuse SecHub storage or no binaries are required for this
     * scan this method will return <code>null</code>. In any other case the storage
     * will be called and the underlying checksum fetched.
     *
     * @param metaDataOrNull
     * @return checksum or <code>null</code>
     * @throws IOException
     */
    public String getBinariesTarFileUploadChecksumOrNull() throws IOException {
        if (storage == null) {
            // in this case no special PDS storage - reuse of sechub storage!
            return null;
        }
        if (!binaryRequired) {
            /* no binaries required at all - means also no checksum... */
            return null;
        }
        try (InputStream inputStream = storage.fetch(FILENAME_BINARIES_TAR_CHECKSUM); Scanner scanner = new Scanner(inputStream)) {
            String result = scanner.hasNext() ? scanner.next() : "";
            return result;
        }

    }

    /**
     * If the PDS does reuse SecHub storage or no binaries are required for this
     * scan this method will return <code>null</code>. In any other case the storage
     * will be called and the underlying file size information fetched
     *
     * @param metaDataOrNull
     * @return checksum or <code>null</code>
     * @throws IOException
     */
    public String getBinariesTarFileSizeOrNull() throws IOException {
        if (storage == null) {
            // in this case no special PDS storage - reuse of sechub storage!
            return null;
        }
        if (!binaryRequired) {
            /* no binaries required at all - means also no checksum... */
            return null;
        }
        try (InputStream inputStream = storage.fetch(FILENAME_BINARIES_TAR_FILESIZE); Scanner scanner = new Scanner(inputStream)) {
            String result = scanner.hasNext() ? scanner.next() : "";
            return result;
        }

    }

    /**
     * Closes resources. After this the provider shall not be used anymore.
     */
    public void close() {
        if (storage == null) {
            return;
        }
        storage.close();
    }
}
