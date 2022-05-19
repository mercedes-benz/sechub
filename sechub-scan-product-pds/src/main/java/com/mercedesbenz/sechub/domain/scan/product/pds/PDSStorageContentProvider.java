package com.mercedesbenz.sechub.domain.scan.product.pds;

import static com.mercedesbenz.sechub.commons.core.CommonConstants.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import com.mercedesbenz.sechub.adapter.AdapterMetaData;
import com.mercedesbenz.sechub.adapter.pds.PDSMetaDataID;
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

    public boolean isSourceRequired() {
        return sourceRequired;
    }

    public boolean isBinaryRequired() {
        return binaryRequired;
    }

    /**
     * If the PDS does reuse SecHub storage it will always return <code>null</code>.
     * When not reused but the files have already been uploaded to PDS (so called
     * again) the stream will also be <code>null</code>. In any other case the
     * storage will be called and the underlying input stream fetched.
     *
     * @param metaDataOrNull
     * @return stream or <code>null</code>
     * @throws IOException
     */
    public InputStream getSourceZipFileInputStreamOrNull(AdapterMetaData metaDataOrNull) throws IOException {
        if (storage == null) {
            return null;
        }
        if (!sourceRequired) {
            return null;
        }
        if (metaDataOrNull != null && metaDataOrNull.hasValue(PDSMetaDataID.KEY_FILEUPLOAD_DONE, true)) {
            return null;
        }
        return storage.fetch(FILENAME_SOURCECODE_ZIP);
    }

    /**
     * If the PDS does reuse SecHub storage it will always return <code>null</code>.
     * When not reused but the files have already been uploaded to PDS (so called
     * again) the stream will also be <code>null</code>. In any other case the
     * storage will be called and the underlying existing checksum will be returned
     *
     * @param metaDataOrNull
     * @return stream or <code>null</code>
     * @throws IOException
     */
    public String getSourceZipFileUploadChecksumOrNull(AdapterMetaData metaDataOrNull) throws IOException {
        if (storage == null) {
            return null;
        }
        if (!sourceRequired) {
            return null;
        }
        if (metaDataOrNull != null && metaDataOrNull.hasValue(PDSMetaDataID.KEY_FILEUPLOAD_DONE, true)) {
            return null;
        }
        try (InputStream inputStream = storage.fetch(FILENAME_SOURCECODE_ZIP_CHECKSUM); Scanner scanner = new Scanner(inputStream)) {
            String result = scanner.hasNext() ? scanner.next() : "";
            return result;
        }

    }

    /**
     * If the PDS does reuse SecHub storage it will always return <code>null</code>.
     * When not reused but the files have already been uploaded to PDS (so called
     * again) the stream will also be <code>null</code>. In any other case the
     * storage will be called and the underlying input stream fetched.
     *
     * @param metaDataOrNull
     * @return stream or <code>null</code>
     * @throws IOException
     */
    public InputStream getBinariesTarFileInputStreamOrNull(AdapterMetaData metaDataOrNull) throws IOException {
        if (storage == null) {
            return null;
        }
        if (!binaryRequired) {
            return null;
        }
        if (metaDataOrNull != null && metaDataOrNull.hasValue(PDSMetaDataID.KEY_FILEUPLOAD_DONE, true)) {
            return null;
        }
        return storage.fetch(FILENAME_BINARIES_TAR);
    }
}
