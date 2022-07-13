package com.mercedesbenz.sechub.wrapper.checkmarx.scan;

import java.io.File;
import java.nio.file.Files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.adapter.AdapterException;
import com.mercedesbenz.sechub.adapter.AdapterMetaDataCallback;
import com.mercedesbenz.sechub.adapter.FileBasedAdapterMetaDataCallback;
import com.mercedesbenz.sechub.adapter.checkmarx.CheckmarxAdapter;
import com.mercedesbenz.sechub.adapter.checkmarx.CheckmarxAdapterConfig;

@Service
public class CheckmarxWrapperScanService {

    private static final Logger LOG = LoggerFactory.getLogger(CheckmarxWrapperScanService.class);

    @Value("${pds.job.metadata.file:}") // This is normally injected by PDS, look at PDS documentation!
    String pdsJobMetaDatafile;

    @Autowired
    CheckmarxAdapter adapter;

    public String startScan(CheckmarxAdapterConfig config) throws Exception {
        File metaDataFile;
        if (pdsJobMetaDatafile == null || pdsJobMetaDatafile.isEmpty()) {
            LOG.warn("PDS job meta data file not set. Will create fallback temp file. For local tests okay but not for production!");

            metaDataFile = Files.createTempFile("fallback_pds_job_metadata_file", ".txt").toFile();
            LOG.warn("Temporary PDS job meta data file is now2: {}", metaDataFile);

        } else {
            metaDataFile = new File(pdsJobMetaDatafile);
        }

        AdapterMetaDataCallback adapterMetaDataCallBack = new FileBasedAdapterMetaDataCallback(metaDataFile);

        try {

            adapter.start(config, adapterMetaDataCallBack);
        } catch (AdapterException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }
}
