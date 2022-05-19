package com.mercedesbenz.sechub.domain.scan.product.pds;

import static com.mercedesbenz.sechub.commons.core.CommonConstants.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.adapter.AdapterMetaData;
import com.mercedesbenz.sechub.adapter.pds.PDSMetaDataID;
import com.mercedesbenz.sechub.storage.core.JobStorage;

@Service
public class JobStorageContentService {

    public InputStream fetchSourceZipFileInputStreamIfNecessary(JobStorage storage, AdapterMetaData metaData) throws IOException {
        if (metaData != null && metaData.hasValue(PDSMetaDataID.KEY_FILEUPLOAD_DONE, true)) {
            return null;
        }
        return storage.fetch(FILENAME_SOURCECODE_ZIP);
    }

    public String fetchSourceZipFileeUploadChecksumIfNecessary(JobStorage storage, AdapterMetaData metaData) throws IOException {
        if (metaData != null && metaData.hasValue(PDSMetaDataID.KEY_FILEUPLOAD_DONE, true)) {
            return null;
        }
        try (InputStream inputStream = storage.fetch(FILENAME_SOURCECODE_ZIP_CHECKSUM); Scanner scanner = new Scanner(inputStream)) {
            String result = scanner.hasNext() ? scanner.next() : "";
            return result;
        }

    }

    public InputStream fetchBinariesTarFileInputStreamIfNecessary(JobStorage storage, AdapterMetaData metaDataOrNull) {
        // TODO Auto-generated method stub
        return null;
    }
}
