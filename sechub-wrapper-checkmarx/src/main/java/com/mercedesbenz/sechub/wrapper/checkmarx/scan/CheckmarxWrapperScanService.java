package com.mercedesbenz.sechub.wrapper.checkmarx.scan;

import java.io.File;
import java.nio.file.Files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.adapter.AdapterExecutionResult;
import com.mercedesbenz.sechub.adapter.AdapterMetaDataCallback;
import com.mercedesbenz.sechub.adapter.FileBasedAdapterMetaDataCallback;
import com.mercedesbenz.sechub.adapter.checkmarx.CheckmarxAdapter;
import com.mercedesbenz.sechub.adapter.checkmarx.CheckmarxAdapterConfig;
import com.mercedesbenz.sechub.commons.pds.PDSUserMessageSupport;
import com.mercedesbenz.sechub.wrapper.checkmarx.CheckmarxWrapperEnvironment;

@Service
public class CheckmarxWrapperScanService {

    private static final Logger LOG = LoggerFactory.getLogger(CheckmarxWrapperScanService.class);

    @Autowired
    CheckmarxAdapter adapter;

    @Autowired
    CheckmarxWrapperEnvironment environment;

    public String startScan(CheckmarxAdapterConfig config) throws Exception {
        File metaDataFile;
        String pdsJobMetaDatafile = environment.getPdsJobMetaDatafile();

        if (pdsJobMetaDatafile == null || pdsJobMetaDatafile.isEmpty()) {
            LOG.warn("PDS job meta data file not set. Will create fallback temp file. For local tests okay but not for production!");

            metaDataFile = Files.createTempFile("fallback_pds_job_metadata_file", ".txt").toFile();
            LOG.warn("Temporary PDS job meta data file is now2: {}", metaDataFile);

        } else {
            metaDataFile = new File(pdsJobMetaDatafile);
        }

        AdapterMetaDataCallback adapterMetaDataCallBack = new FileBasedAdapterMetaDataCallback(metaDataFile);

        AdapterExecutionResult adapterResult = adapter.start(config, adapterMetaDataCallBack);

        PDSUserMessageSupport support = new PDSUserMessageSupport(environment.getPdsUserMessagesFolder());
        support.writeMessages(adapterResult.getProductMessages());

        return adapterResult.getProductResult();
    }
}
